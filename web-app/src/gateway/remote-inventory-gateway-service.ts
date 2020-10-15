import { Fetchable } from "/@/gateway/fetchable"
import { AdjustQuantityDto, EnumerateItemsDto, RemoveItemDto, SubmitItemDto } from "/@/gateway/inventory-gateway-commands"
import { ItemAddedDto, ItemRemovedDto, ItemsEnumeratedDto, QuantityAdjustedDto } from "/@/gateway/inventory-gateway-events"
import { InventoryGatewayService, Item } from "/@/gateway/inventory-gateway-service"
import { reactive } from "@vue/reactivity"

const endpoint = "http://localhost:8382/messages"

export class RemoteInventoryGatewayService implements InventoryGatewayService {
    private items: Fetchable<Array<Item>> = reactive({
        status: "unrequested",
        value: null,
        errorMessage: null,
    })
    
    private latestOperation: Fetchable<void> = reactive({
        status: "unrequested",
        value: null,
        errorMessage: null,
    })
    
    start() {
        const source = new EventSource(endpoint)
        
        window.onbeforeunload = () => {
            source.close()
        }
        
        source.addEventListener("items-enumerated", event => {
            const { items } = parseEvent<ItemsEnumeratedDto>(event)
            
            this.setSucceededItems(items.map(({ item, quantity }) => ({
                name: item,
                currentQuantity: quantity,
            })))
            this.setUnrequestedLatestOperation()
        })
        
        source.addEventListener("item-added", event => {
            const { item, quantity } = parseEvent<ItemAddedDto>(event)
            
            const addedItem: Item = {
                name: item,
                currentQuantity: quantity,
            }
            
            this.setSucceededItems(this.getExistingItems()
                .concat([addedItem]),
            )
            this.setUnrequestedLatestOperation()
        })
        
        source.addEventListener("item-removed", event => {
            const { item } = parseEvent<ItemRemovedDto>(event)
            
            this.setSucceededItems(this.getExistingItems()
                .filter(({ name }) => name !== item),
            )
            this.setUnrequestedLatestOperation()
        })
        
        source.addEventListener("quantity-adjusted", event => {
            const { item, quantity } = parseEvent<QuantityAdjustedDto>(event)
            
            const adjustedItem: Item = {
                name: item,
                currentQuantity: quantity,
            }
            
            this.setSucceededItems(this.getExistingItems()
                .filter(({ name }) => name !== adjustedItem.name)
                .concat([adjustedItem]),
            )
            this.setUnrequestedLatestOperation()
        })
        
        source.addEventListener("error", event => {
            // if (source.readyState !== EventSource.CLOSED) {
            this.setFailedLatestOperation(JSON.stringify(event))
            // }
        })
    }
    
    private getExistingItems(): Array<Item> {
        return this.items.value ?? []
    }
    
    enumerateItems() {
        this.setPendingItems()
        
        publish<EnumerateItemsDto>({
            topic: "enumerate-items",
        }).catch((e: Error) => {
            this.setFailedItems(e.message)
        })
    }
    
    submitItem(item: string, quantity: number) {
        this.setPendingLatestOperation()
        
        publish<SubmitItemDto>({
            topic: "submit-item",
            item,
            quantity,
        }).catch((e: Error) => this.setFailedLatestOperation(e.message))
    }
    
    removeItem(item: string) {
        this.setPendingLatestOperation()
        
        publish<RemoveItemDto>({
            topic: "remove-item",
            item,
        }).catch((e: Error) => this.setFailedLatestOperation(e.message))
    }
    
    adjustQuantity(item: string, quantity: number) {
        this.setPendingLatestOperation()
        
        publish<AdjustQuantityDto>({
            topic: "adjust-quantity",
            item,
            quantity,
        }).catch((e: Error) => this.setFailedLatestOperation(e.message))
    }
    
    getItems(): Fetchable<Array<Item>> {
        return this.items
    }
    
    private setPendingItems() {
        this.items.status = "pending"
        this.items.value = null
        this.items.errorMessage = null
    }
    
    private setSucceededItems(items: Array<Item>) {
        this.items.status = "succeeded"
        this.items.value = items
        this.items.errorMessage = null
    }
    
    private setFailedItems(errorMessage: string) {
        this.items.status = "failed"
        this.items.value = null
        this.items.errorMessage = errorMessage
    }
    
    private setUnrequestedLatestOperation() {
        this.latestOperation.status = "unrequested"
        this.latestOperation.value = null
        this.latestOperation.errorMessage = null
    }
    
    private setPendingLatestOperation() {
        this.latestOperation.status = "pending"
        this.latestOperation.value = null
        this.latestOperation.errorMessage = null
    }
    
    private setFailedLatestOperation(errorMessage: string) {
        this.latestOperation.status = "failed"
        this.latestOperation.value = null
        this.latestOperation.errorMessage = errorMessage
    }
}

function parseEvent<Topic>(event: any): Topic {
    return JSON.parse((event as unknown as MessageEvent).data).payload as Topic
}

function publish<Topic>(message: Topic): Promise<Response> {
    return fetch(endpoint, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(message),
    })
}
