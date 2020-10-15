import { RemoteInventoryGatewayService } from "/@/gateway/remote-inventory-gateway-service"

export const useGateway = () => {
    const gatewayService = new RemoteInventoryGatewayService()
    
    gatewayService.start()
    gatewayService.enumerateItems()
    
    return gatewayService
}
