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

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private static final DateTimeFormatter INPUT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu/MM/dd")
                    .withResolverStyle(ResolverStyle.STRICT);

    private final ItemRepository itemRepository;

    public ItemResponse createItem(ItemRequest request) {
        log.info("Criando item: {} / {}",
                request.getSupplierID(), request.getPartNumber());

        String partNumberVersion = buildNextPartNumberVersion(
                request.getSupplierID(),
                request.getPartNumber()
        );

        // Regra de negocio: item nao pode existir
        validateItemDoesNotExist(
                request.getSupplierID(),
                partNumberVersion
        );

        // Converte DTO -> Dominio
        Item item = ItemMapper.toDomain(request);
        item.setPartNumberVersion(partNumberVersion);
        item.setTbtVffDate(normalizeDate(request.getTbtVffDate(), "TbtVffDate"));
        item.setTbtPvsDate(normalizeDate(request.getTbtPvsDate(), "TbtPvsDate"));
        item.setTbt0sDate(normalizeDate(request.getTbt0sDate(), "Tbt0sDate"));
        item.setSopDate(normalizeDate(request.getSopDate(), "SopDate"));
        item.setStatus(ItemStatus.SAVED);
        item.setUpdatedAt(Instant.now());

        // Persiste
        Item savedItem = itemRepository.save(item);

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

    private void validateItemDoesNotExist(
            String supplierID,
            String partNumberVersion
    ) {
        Optional<Item> existingItem =
                itemRepository.findById(supplierID, partNumberVersion);

        if (existingItem.isPresent()) {
            throw new IllegalStateException(
                    String.format(
                            "Item %s/%s ja existe",
                            supplierID,
                            partNumberVersion
                    )
            );
        }
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


