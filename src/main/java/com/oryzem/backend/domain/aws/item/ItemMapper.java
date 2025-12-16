package com.oryzem.backend.domain.aws.item;

public class ItemMapper {

    private ItemMapper() {
        // impede instanciação
    }

    public static Item toDomain(ItemRequest request) {
        return Item.builder()
                .partNumberID(request.getPartNumberID())
                .supplierID(request.getSupplierID())
                .build();
    }

    public static ItemResponse toResponse(Item item, String message) {
        return ItemResponse.builder()
                .partNumberID(item.getPartNumberID())
                .supplierID(item.getSupplierID())
                .createdAt(
                        item.getCreatedAt() != null
                                ? item.getCreatedAt().toString()
                                : null
                )
                .message(message)
                .build();
    }
}
