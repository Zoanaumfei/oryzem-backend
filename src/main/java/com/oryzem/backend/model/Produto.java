package com.oryzem.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Produto {

    private String id; // Partition Key
    private String sku; // Sort Key
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer quantidadeEstoque;
    private String categoria;
    private String marca;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private Boolean ativo;
    private DetalhesProduto detalhes;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    @DynamoDbSortKey
    public String getSku() {
        return sku;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "CategoriaIndex")
    public String getCategoria() {
        return categoria;
    }

    @DynamoDbSecondarySortKey(indexNames = "PrecoIndex")
    public BigDecimal getPreco() {
        return preco;
    }
}

@Data
@DynamoDbBean
class DetalhesProduto {
    private String fabricante;
    private String modelo;
    private String dimensoes;
    private Double peso;
    private String garantia;
}