import { Fetchable } from "/@/gateway/fetchable"

export interface InventoryGatewayService {
    start(): void
    
    enumerateItems(): void
    
    submitItem(item: string, quantity: number): void
    
    removeItem(item: string): void
    
    adjustQuantity(item: string, quantity: number): void
    
    getItems(): Fetchable<Array<Item>>
}

export interface Item {
    readonly name: string
    readonly currentQuantity: number
}
