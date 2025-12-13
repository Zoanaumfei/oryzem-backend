package com.oryzem.backend.model;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@DynamoDbBean
public class Pedido {

    private String pedidoId;
    private String clienteId;
    private LocalDateTime dataPedido;
    private StatusPedido status;
    private BigDecimal valorTotal;
    private EnderecoEntrega enderecoEntrega;
    private List<ItemPedido> itens;
    private String metodoPagamento;
    private String observacoes;

    // Partition Key: CLIENTE#ID
    // Sort Key: PEDIDO#ID
    public String getPk() {
        return "CLIENTE#" + clienteId;
    }

    public String getSk() {
        return "PEDIDO#" + pedidoId;
    }

    @DynamoDbPartitionKey
    public String pk() {
        return getPk();
    }

    @DynamoDbSortKey
    public String sk() {
        return getSk();
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "StatusIndex")
    public String getStatus() {
        return status.toString();
    }
}

@Data
@DynamoDbBean
class ItemPedido {
    private String produtoId;
    private String sku;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
}

@Data
@DynamoDbBean
class EnderecoEntrega {
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
}

enum StatusPedido {
    PENDENTE, PROCESSANDO, ENVIADO, ENTREGUE, CANCELADO
}
