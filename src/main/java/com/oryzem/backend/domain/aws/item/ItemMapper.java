package com.oryzem.backend.domain.aws.item;

public class ItemMapper {

    private ItemMapper() {
        // impede instanciacao
    }

    public static Item toDomain(ItemRequest request) {
        return Item.builder()
                .supplierID(request.getSupplierID())
                .processNumber(request.getProcessNumber())
                .partDescription(request.getPartDescription())
                .tbtVffDate(request.getTbtVffDate())
                .tbtPvsDate(request.getTbtPvsDate())
                .tbt0sDate(request.getTbt0sDate())
                .sopDate(request.getSopDate())
                .build();
    }

    public static ItemResponse toResponse(Item item, String message) {
        return ItemResponse.builder()
                .partNumberVersion(item.getPartNumberVersion())
                .supplierID(item.getSupplierID())
                .processNumber(item.getProcessNumber())
                .partDescription(item.getPartDescription())
                .tbtVffDate(item.getTbtVffDate())
                .tbtPvsDate(item.getTbtPvsDate())
                .tbt0sDate(item.getTbt0sDate())
                .sopDate(item.getSopDate())
                .createdAt(
                        item.getCreatedAt() != null
                                ? item.getCreatedAt().toString()
                                : null
                )
                .updatedAt(
                        item.getUpdatedAt() != null
                                ? item.getUpdatedAt().toString()
                                : null
                )
                .status(
                        item.getStatus() != null
                                ? item.getStatus().name()
                                : null
                )
                .message(message)
                .build();
    }
}

