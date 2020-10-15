export interface ItemsEnumeratedDto {
    readonly topic: "items-enumerated"
    readonly items: Array<EnumeratedItemDto>
}

export interface EnumeratedItemDto {
    readonly item: string
    readonly quantity: number
}

export interface ItemAddedDto {
    readonly topic: "item-added"
    readonly item: string
    readonly quantity: number
}

export interface ItemRemovedDto {
    readonly topic: "item-removed"
    readonly item: string
}

export interface QuantityAdjustedDto {
    readonly topic: "quantity-adjusted"
    readonly item: string
    readonly quantity: number
}
