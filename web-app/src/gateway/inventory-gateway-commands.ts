export interface EnumerateItemsDto {
    readonly topic: "enumerate-items"
}

export interface SubmitItemDto {
    readonly topic: "submit-item"
    readonly item: string
    readonly quantity: number
}

export interface RemoveItemDto {
    readonly topic: "remove-item"
    readonly item: string
}

export interface AdjustQuantityDto {
    readonly topic: "adjust-quantity"
    readonly item: string
    readonly quantity: number
}
