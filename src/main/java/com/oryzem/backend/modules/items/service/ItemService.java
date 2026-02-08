package com.oryzem.backend.modules.items.service;

import com.oryzem.backend.modules.items.domain.Item;
import com.oryzem.backend.modules.items.domain.ItemNotFoundException;
import com.oryzem.backend.modules.items.domain.ItemStatus;
import com.oryzem.backend.modules.items.dto.ItemRequest;
import com.oryzem.backend.modules.items.dto.ItemResponse;
import com.oryzem.backend.modules.items.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private static final DateTimeFormatter INPUT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu/MM/dd")
                    .withResolverStyle(ResolverStyle.STRICT);
    private static final int MAX_CREATE_RETRIES = 5;

    private final ItemRepository itemRepository;

    public ItemResponse createItem(ItemRequest request) {
        log.info("Criando item: {} / {}",
                request.getSupplierID(), request.getPartNumber());

        Item savedItem = null;
        for (int attempt = 1; attempt <= MAX_CREATE_RETRIES; attempt++) {
            String partNumberVersion = buildNextPartNumberVersion(
                    request.getSupplierID(),
                    request.getPartNumber()
            );

            Item item = ItemMapper.toDomain(request);
            item.setPartNumberVersion(partNumberVersion);
            item.setTbtVffDate(normalizeDate(request.getTbtVffDate(), "TbtVffDate"));
            item.setTbtPvsDate(normalizeDate(request.getTbtPvsDate(), "TbtPvsDate"));
            item.setTbt0sDate(normalizeDate(request.getTbt0sDate(), "Tbt0sDate"));
            item.setSopDate(normalizeDate(request.getSopDate(), "SopDate"));
            item.setStatus(ItemStatus.SAVED);
            item.setUpdatedAt(Instant.now());

            try {
                savedItem = itemRepository.saveIfAbsent(item);
                break;
            } catch (ConditionalCheckFailedException ex) {
                log.warn("Conflito de versao ao criar item {}/{} (tentativa {}/{})",
                        request.getSupplierID(),
                        request.getPartNumber(),
                        attempt,
                        MAX_CREATE_RETRIES);
            }
        }

        if (savedItem == null) {
            throw new IllegalStateException("Nao foi possivel criar uma versao unica do item. Tente novamente.");
        }

        log.info("Item criado com sucesso: {} / {}",
                savedItem.getSupplierID(),
                savedItem.getPartNumberVersion());

        // Converte Dominio -> DTO
        return ItemMapper.toResponse(
                savedItem,
                "Item criado com sucesso"
        );
    }

    public ItemResponse getItem(String supplierID, String partNumberVersion) {
        log.info("Buscando item: {} / {}", supplierID, partNumberVersion);

        Item item = itemRepository
                .findById(supplierID, partNumberVersion)
                .orElseThrow(() ->
                        new ItemNotFoundException(
                                supplierID,
                                partNumberVersion
                        )
                );

        return ItemMapper.toResponse(item, "Item encontrado");
    }

    public List<ItemResponse> getItemsByStatus(ItemStatus status) {
        List<Item> items = itemRepository.findAllByStatus(status);
        return items.stream()
                .map(item -> ItemMapper.toResponse(item, "Item listado"))
                .collect(Collectors.toList());
    }

    public List<ItemResponse> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .map(item -> ItemMapper.toResponse(item, "Item listado"))
                .collect(Collectors.toList());
    }

        // ===============================
    // Metodos privados (regras)
    // ===============================

    private String buildNextPartNumberVersion(String supplierID, String partNumber) {
        int nextVersion = itemRepository.findNextVersionNumber(supplierID, partNumber);
        return partNumber + String.format("#ver%05d", nextVersion);
    }

    private String normalizeDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        try {
            LocalDate date = LocalDate.parse(value, INPUT_DATE_FORMAT);
            return date.format(INPUT_DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    fieldName + " must be a valid date in YYYY/MM/DD",
                    ex
            );
        }
    }
}


