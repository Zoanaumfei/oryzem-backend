package com.oryzem.backend.repository;

import com.oryzem.backend.model.Pedido;
import com.oryzem.backend.model.Produto;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PedidoRepository {

    private final DynamoDbTable<Pedido> pedidoTable;
    private final DynamoDbEnhancedClient enhancedClient;

    public PedidoRepository(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
        this.pedidoTable = enhancedClient.table("Pedidos",
                TableSchema.fromBean(Pedido.class));
    }

    // Transação para criar pedido e atualizar estoque
    public Pedido criarPedidoComTransacao(Pedido pedido,
                                          List<AtualizacaoEstoque> atualizacoesEstoque) {

        enhancedClient.transactWriteItems(r -> r
                .addPutItem(pedidoTable, pedido)
                .addUpdateItem(updateEstoque(atualizacoesEstoque.get(0)))
                .addUpdateItem(updateEstoque(atualizacoesEstoque.get(1)))
        );

        return pedido;
    }

    private TransactUpdateItemEnhancedRequest<Produto> updateEstoque(
            AtualizacaoEstoque atualizacao) {

        DynamoDbTable<Produto> produtoTable = enhancedClient.table("Produtos",
                TableSchema.fromBean(Produto.class));

        Key key = Key.builder()
                .partitionValue(atualizacao.getProdutoId())
                .sortValue(atualizacao.getSku())
                .build();

        return TransactUpdateItemEnhancedRequest.builder(Produto.class)
                .table(produtoTable)
                .key(key)
                .item(produto -> {
                    produto.setQuantidadeEstoque(
                            produto.getQuantidadeEstoque() - atualizacao.getQuantidade()
                    );
                    return produto;
                })
                .build();
    }

    // Query com chave composta
    public List<Pedido> buscarPedidosPorCliente(String clienteId) {
        return pedidoTable.query(QueryConditional.keyEqualTo(
                Key.builder().partitionValue("CLIENTE#" + clienteId).build()
        )).items().stream().collect(Collectors.toList());
    }

    // Busca por status usando GSI
    public List<Pedido> buscarPedidosPorStatus(String status) {
        return pedidoTable.index("StatusIndex")
                .query(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(status).build()
                ))
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    // Batch write
    public void salvarPedidosEmLote(List<Pedido> pedidos) {
        WriteBatch batch = WriteBatch.builder(Pedido.class)
                .mappedTableResource(pedidoTable)
                .addPutItem(pedidos.get(0))
                .addPutItem(pedidos.get(1))
                .build();

        enhancedClient.batchWriteItem(r -> r.addWriteBatch(batch));
    }
}