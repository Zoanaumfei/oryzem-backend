//Camada de Acesso a Dados (DAL)

// Função: Interface entre sua aplicação e o banco de dados
// Responsabilidades:
// 1. Traduzir operações Java para comandos DynamoDB
// 2. Esconder a complexidade do DynamoDB SDK
// 3. Fornecer métodos CRUD simples para o Service usar

// Métodos típicos:
// - save()      → Salva/Atualiza um item
// - findById()  → Busca por chave composta
// - exists()    → Verifica existência
// - delete()    → Remove um item

//Analogia: É o garçom do restaurante - leva seus pedidos (queries) até a cozinha (banco de dados)

package com.oryzem.backend.domain.aws.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    // Nome da sua tabela DynamoDB
    private static final String TABLE_NAME = "VW216-TCROSSPA2";

    private DynamoDbTable<Item> getItemTable() {
        return enhancedClient.table(TABLE_NAME, TableSchema.fromBean(Item.class));
    }

    public Item save(Item item) {
        log.info("Salvando item: {}/{}", item.getSupplierID(), item.getPartNumberVersion());
        getItemTable().putItem(item);
        return item;
    }

    public Optional<Item> findById(String supplierID, String partNumberVersion) {
        log.info("Buscando item: {}/{}", supplierID, partNumberVersion);

        Key key = Key.builder()
                .partitionValue(supplierID)
                .sortValue(partNumberVersion)
                .build();

        Item item = getItemTable().getItem(key);
        return Optional.ofNullable(item);
    }

    public boolean exists(String supplierID, String partNumberVersion) {
        return findById(supplierID, partNumberVersion).isPresent();
    }

    public void delete(String supplierID, String partNumberVersion) {
        log.info("Deletando item: {}/{}", supplierID, partNumberVersion);

        Key key = Key.builder()
                .partitionValue(supplierID)
                .sortValue(partNumberVersion)
                .build();

        getItemTable().deleteItem(key);
    }

    public List<Item> findAllByStatus(ItemStatus status) {
        log.info("Listando itens com status: {}", status);
        Expression filter = Expression.builder()
                .expression("#status = :status")
                .putExpressionName("#status", "Status")
                .putExpressionValue(":status", AttributeValue.builder().s(status.name()).build())
                .build();

        List<Item> items = new ArrayList<>();
        getItemTable().scan(r -> r.filterExpression(filter))
                .items()
                .forEach(items::add);
        return items;
    }

    public List<Item> findAll() {
        log.info("Listando todos os itens");
        List<Item> items = new ArrayList<>();
        getItemTable().scan()
                .items()
                .forEach(items::add);
        return items;
    }
}




