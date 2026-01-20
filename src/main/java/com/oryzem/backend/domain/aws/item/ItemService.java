package com.oryzem.backend.domain.aws.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import com.oryzem.backend.domain.aws.item.exception.ItemNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private static final DateTimeFormatter INPUT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final ItemRepository itemRepository;

    public ItemResponse createItem(ItemRequest request) {
        log.info("Criando item: {} / {}",
                request.getSupplierID(), request.getPartNumberVersion());

        // Regra de negocio: item nao pode existir
        validateItemDoesNotExist(
                request.getSupplierID(),
                request.getPartNumberVersion()
        );

        // Converte DTO -> Dominio
        Item item = ItemMapper.toDomain(request);
        item.setTbtVffDate(normalizeDate(request.getTbtVffDate()));
        item.setTbtPvsDate(normalizeDate(request.getTbtPvsDate()));
        item.setTbt0sDate(normalizeDate(request.getTbt0sDate()));
        item.setSopDate(normalizeDate(request.getSopDate()));
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
                .toList();
    }

    public List<ItemResponse> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .map(item -> ItemMapper.toResponse(item, "Item listado"))
                .toList();
    }

    // ===============================
    // Metodos privados (regras)
    // ===============================

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

    private String normalizeDate(String value) {
        LocalDate date = LocalDate.parse(value, INPUT_DATE_FORMAT);
        return date.format(INPUT_DATE_FORMAT);
    }
}










