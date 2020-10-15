import { useGateway } from "/@/gateway/use-gateway"
import { InventoryItemList } from "/@/InventoryItemList"
import { defineComponent } from "vue"

export const InventoryContainer = defineComponent({
    setup() {
        const gatewayService = useGateway()
        const items = gatewayService.getItems()
        
        function makeContainer() {
            if (items.status === "unrequested" || items.status === "pending") {
                return (
                    <h1 class="text-2xl">Loading the inventory...</h1>
                )
            } else if (items.status === "failed") {
                return (
                    <h1 class="text-2xl">Something is not quite right: {items.errorMessage!}</h1>
                )
            } else if (items.status === "succeeded") {
                return (
                    <div>
                        <InventoryItemList items={items.value!}/>
                    </div>
                )
            }
        }
        
        return () => makeContainer()
    },
})
