package com.oryzem.backend.domain.aws.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j // Para logging automático
@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemResponse createItem(ItemRequest request) {
        log.info("Criando item: {} / {}",
                request.getPartNumberID(), request.getSupplierID());

        // Validação
        if (request.getPartNumberID() == null || request.getPartNumberID().trim().isEmpty()) {
            throw new IllegalArgumentException("PartNumberID é obrigatório");
        }

        if (request.getSupplierID() == null || request.getSupplierID().trim().isEmpty()) {
            throw new IllegalArgumentException("SupplierID é obrigatório");
        }

        // Verifica se já existe
        if (itemRepository.exists(request.getPartNumberID(), request.getSupplierID())) {
            throw new IllegalStateException(
                    String.format("Item %s/%s já existe",
                            request.getPartNumberID(), request.getSupplierID())
            );
        }

        // Cria e salva
        Item item = Item.builder()
                .partNumberID(request.getPartNumberID())
                .supplierID(request.getSupplierID())
                .build();

        Item savedItem = itemRepository.save(item);

        log.info("Item criado com sucesso: {} / {}",
                savedItem.getPartNumberID(), savedItem.getSupplierID());

        return ItemResponse.builder()
                .partNumberID(savedItem.getPartNumberID())
                .supplierID(savedItem.getSupplierID())
                .createdAt(savedItem.getCreatedAt())
                .message("Item criado com sucesso")
                .build();
    }

    public ItemResponse getItem(String partNumberID, String supplierID) {
        log.info("Buscando item: {} / {}", partNumberID, supplierID);

        Item item = itemRepository.findById(partNumberID, supplierID);

        if (item == null) {
            throw new RuntimeException(
                    String.format("Item %s/%s não encontrado", partNumberID, supplierID)
            );
        }

        return ItemResponse.builder()
                .partNumberID(item.getPartNumberID())
                .supplierID(item.getSupplierID())
                .createdAt(item.getCreatedAt())
                .message("Item encontrado")
                .build();
    }
}
