package com.oryzem.backend.domain.aws.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
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

        // Verifica se já existe (agora retorna Optional)
        Optional<Item> existingItem = itemRepository.findById(
                request.getPartNumberID(),
                request.getSupplierID()
        );

        if (existingItem.isPresent()) {
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
                .createdAt(savedItem.getCreatedAt().toString()) // Convert Instant para String
                .message("Item criado com sucesso")
                .build();
    }

    public ItemResponse getItem(String partNumberID, String supplierID) {
        log.info("Buscando item: {} / {}", partNumberID, supplierID);

        Optional<Item> itemOptional = itemRepository.findById(partNumberID, supplierID);

        if (itemOptional.isEmpty()) {
            throw new RuntimeException(
                    String.format("Item %s/%s não encontrado", partNumberID, supplierID)
            );
        }

        Item item = itemOptional.get();

        return ItemResponse.builder()
                .partNumberID(item.getPartNumberID())
                .supplierID(item.getSupplierID())
                .createdAt(item.getCreatedAt().toString()) // Convert Instant para String
                .message("Item encontrado")
                .build();
    }
}