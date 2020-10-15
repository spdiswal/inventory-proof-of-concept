import { InventoryContainer } from "/@/InventoryContainer"
import { createApp, defineComponent } from "vue"
import "./main.css"

const AppRoot = defineComponent({
    setup() {
        return () => (
            <div class="w-full h-full min-h-screen flex flex-row justify-center bg-gray-200 font-sans">
                <div class="flex flex-col w-full md:w-4/5 lg:w-3/5">
                    <InventoryContainer class="mt-4"/>
                </div>
            </div>
        )
    },
})

createApp(AppRoot).mount("#app")
