import { Item } from "/@/gateway/inventory-gateway-service"
import { useGateway } from "/@/gateway/use-gateway"
import { defineComponent, Ref, ref } from "vue"

const InventoryEmptyItemList = defineComponent({
    setup() {
        return () => (
            <h1 class="text-2xl">No items in the inventory</h1>
        )
    },
})

const InventoryItem = defineComponent({
    props: {
        item: Object as () => Item,
    },
    setup(props) {
        const gatewayService = useGateway()
        
        function decrementQuantity() {
            gatewayService.adjustQuantity(
                props.item!.name,
                props.item!.currentQuantity - 1,
            )
        }
        
        function incrementQuantity() {
            gatewayService.adjustQuantity(
                props.item!.name,
                props.item!.currentQuantity + 1,
            )
        }
        
        function removeItem() {
            gatewayService.removeItem(props.item!.name)
        }
        
        return () => (
            <li class="my-2">
                <span class="font-bold mr-3">{props.item!.name}:</span>
                <span>{props.item!.currentQuantity}</span>
                <button class="ml-6 bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-full" onClick={() => decrementQuantity()}>-1</button>
                <button class="ml-2 bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-full" onClick={() => incrementQuantity()}>+1</button>
                <button class="ml-2 bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded-full" onClick={() => removeItem()}>Remove</button>
            </li>
        )
    },
})

const ItemAdder = defineComponent({
    setup() {
        const gatewayService = useGateway()
        
        let newItemName: Ref<string> = ref("")
        
        function submitItem() {
            gatewayService.submitItem(newItemName.value, 0)
            newItemName.value = ""
        }
        
        return () => (
            <div>
                <input
                    class="shadow appearance-none border border-gray-500 rounded py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:shadow-outline"
                    type="text" placeholder="New item" onInput={e => newItemName.value = (e.target as any).value}
                    value={newItemName.value}
                    onKeydown={e => { if (e.key === "Enter") { submitItem() }}}/>
                <button class="ml-2 bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-full" onClick={() => submitItem()}>Add</button>
            </div>
        )
    },
})

export const InventoryItemList = defineComponent({
    props: {
        items: Array as () => Array<Item>,
    },
    setup(props) {
        function makeList() {
            if (props.items!.length === 0) {
                return (
                    <div>
                        <InventoryEmptyItemList/>
                        <ItemAdder class="mt-4"/>
                    </div>
                )
            } else {
                return (
                    <div>
                        <h1 class="text-2xl">Items</h1>
                        <ul>
                            {props.items!
                                .sort((a, b) => a.name.localeCompare(b.name))
                                .map(item => <InventoryItem item={item}/>)}
                        </ul>
                        <ItemAdder class="mt-4"/>
                    </div>
                )
            }
        }
        
        return () => makeList()
    },
})
