package com.oryzem.backend.modules.items.repository;

//Camada de Acesso a Dados (DAL)

// Funcao: Interface entre sua aplicacao e o banco de dados
// Responsabilidades:
// 1. Traduzir operacoes Java para comandos DynamoDB
// 2. Esconder a complexidade do DynamoDB SDK
// 3. Fornecer metodos CRUD simples para o Service usar

// Metodos tipicos:
// - save()      -> Salva/Atualiza um item
// - findById()  -> Busca por chave composta
// - exists()    -> Verifica existencia
// - delete()    -> Remove um item

//Analogia: E o garcom do restaurante - leva seus pedidos (queries) ate a cozinha (banco de dados)

import com.oryzem.backend.core.tenant.TenantKeyCodec;
import com.oryzem.backend.core.tenant.TenantScope;
import com.oryzem.backend.modules.items.domain.Item;
import com.oryzem.backend.modules.items.domain.ItemStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Repository
public class ItemRepository {

    private static final Pattern VERSION_PATTERN = Pattern.compile("#ver(\\d{5})$");

    private final DynamoDbEnhancedClient enhancedClient;
    private final String tableName;

    public ItemRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${app.dynamodb.tables.items:VW216-TCROSSPA2}") String tableName
    ) {
        this.enhancedClient = enhancedClient;
        this.tableName = tableName;
    }

    private DynamoDbTable<Item> getItemTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(Item.class));
    }

    public Item save(Item item) {
        log.info("Salvando item: {}/{}", item.getSupplierID(), item.getPartNumberVersion());
        Item persisted = toPersisted(item);
        getItemTable().putItem(persisted);
        return item;
    }

    public Item saveIfAbsent(Item item) {
        log.info("Salvando item (condicional): {}/{}", item.getSupplierID(), item.getPartNumberVersion());

        Expression condition = Expression.builder()
                .expression("attribute_not_exists(#pk) AND attribute_not_exists(#sk)")
                .expressionNames(Map.of(
                        "#pk", "SupplierID",
                        "#sk", "PartNumber#Version"
                ))
                .build();

        PutItemEnhancedRequest<Item> request = PutItemEnhancedRequest.builder(Item.class)
                .item(toPersisted(item))
                .conditionExpression(condition)
                .build();

        getItemTable().putItem(request);
        return item;
    }

    public Optional<Item> findById(String supplierID, String partNumberVersion) {
        log.info("Buscando item: {}/{}", supplierID, partNumberVersion);

        Key key = Key.builder()
                .partitionValue(TenantKeyCodec.encode(supplierID))
                .sortValue(TenantKeyCodec.encode(partNumberVersion))
                .build();

        Item item = getItemTable().getItem(key);
        return Optional.ofNullable(item).map(this::toDomain);
    }

    public boolean exists(String supplierID, String partNumberVersion) {
        return findById(supplierID, partNumberVersion).isPresent();
    }

    public void delete(String supplierID, String partNumberVersion) {
        log.info("Deletando item: {}/{}", supplierID, partNumberVersion);

        Key key = Key.builder()
                .partitionValue(TenantKeyCodec.encode(supplierID))
                .sortValue(TenantKeyCodec.encode(partNumberVersion))
                .build();

        getItemTable().deleteItem(key);
    }

    public int findNextVersionNumber(String supplierID, String partNumber) {
        String prefix = partNumber + "#ver";
        QueryConditional conditional = QueryConditional.sortBeginsWith(
                Key.builder()
                        .partitionValue(TenantKeyCodec.encode(supplierID))
                        .sortValue(TenantKeyCodec.encode(prefix))
                        .build()
        );

        int maxVersion = -1;
        for (Item item : getItemTable().query(r -> r.queryConditional(conditional)).items()) {
            int version = extractVersion(toDomain(item).getPartNumberVersion());
            if (version > maxVersion) {
                maxVersion = version;
            }
        }

        return maxVersion + 1;
    }

    private int extractVersion(String partNumberVersion) {
        if (partNumberVersion == null) {
            return -1;
        }
        Matcher matcher = VERSION_PATTERN.matcher(partNumberVersion);
        if (!matcher.find()) {
            return -1;
        }
        return Integer.parseInt(matcher.group(1));
    }

    public List<Item> findAllByStatus(ItemStatus status) {
        log.info("Listando itens com status: {}", status);
        DynamoDbIndex<Item> index = getItemTable().index("FindByStatus");

        QueryConditional conditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(TenantKeyCodec.encode(status.name()))
                        .build()
        );

        List<Item> items = new ArrayList<>();
        for (var page : index.query(r -> r.queryConditional(conditional))) {
            for (Item item : page.items()) {
                items.add(toDomain(item));
            }
        }
        return items;
    }

    public List<Item> findAll() {
        log.info("Listando todos os itens");
        List<Item> items = new ArrayList<>();
        getItemTable().scan()
                .items()
                .forEach(item -> {
                    if (TenantScope.current().equals(item.getTenantId())) {
                        items.add(toDomain(item));
                    }
                });
        return items;
    }

    private Item toPersisted(Item item) {
        Item copy = copy(item);
        copy.setTenantId(TenantScope.current());
        copy.setSupplierKey(TenantKeyCodec.encode(copy.getSupplierID()));
        copy.setPartNumberVersionKey(TenantKeyCodec.encode(copy.getPartNumberVersion()));
        copy.setStatusKey(copy.getStatus() == null ? null : TenantKeyCodec.encode(copy.getStatus().name()));
        return copy;
    }

    private Item toDomain(Item item) {
        Item copy = copy(item);
        copy.setSupplierID(item.getSupplierID());
        copy.setPartNumberVersion(item.getPartNumberVersion());
        copy.setStatus(item.getStatus());
        copy.setTenantId(item.getTenantId());
        return copy;
    }

    private Item copy(Item item) {
        return Item.builder()
                .partNumberVersion(item.getPartNumberVersion())
                .partNumberVersionKey(item.getPartNumberVersionKey())
                .supplierID(item.getSupplierID())
                .supplierKey(item.getSupplierKey())
                .processNumber(item.getProcessNumber())
                .partDescription(item.getPartDescription())
                .tbtVffDate(item.getTbtVffDate())
                .tbtPvsDate(item.getTbtPvsDate())
                .tbt0sDate(item.getTbt0sDate())
                .sopDate(item.getSopDate())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .status(item.getStatus())
                .statusKey(item.getStatusKey())
                .tenantId(item.getTenantId())
                .build();
    }
}




