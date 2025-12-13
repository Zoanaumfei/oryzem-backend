package com.oryzem.backend.repository;

import com.oryzem.backend.model.Produto;
import lombok.Getter;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProdutoRepository {

    // Método getter para a tabela (se necessário para outros serviços)
    @Getter
    private final DynamoDbTable<Produto> produtoTable;
    private final DynamoDbEnhancedClient enhancedClient;

    public ProdutoRepository(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
        this.produtoTable = enhancedClient.table("Produtos",
                TableSchema.fromBean(Produto.class));
    }

    public Produto salvar(Produto produto) {
        produtoTable.putItem(produto);
        return produto;
    }

    public Optional<Produto> buscarPorId(String id, String sku) {
        Key key = Key.builder()
                .partitionValue(id)
                .sortValue(sku)
                .build();

        return Optional.ofNullable(produtoTable.getItem(key));
    }

    public List<Produto> buscarPorCategoria(String categoria) {
        return produtoTable.index("CategoriaIndex")
                .query(QueryConditional.keyEqualTo(Key.builder()
                        .partitionValue(categoria)
                        .build()))
                .stream()                           // Obter stream de páginas
                .flatMap(page -> page.items().stream()) // Extrair itens de cada página
                .collect(Collectors.toList());
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoTable.scan(r -> r.filterExpression(
                Expression.builder()
                        .expression("contains(nome, :nome)")
                        .putExpressionValue(":nome", AttributeValue.fromS(nome))
                        .build()
        )).items().stream().collect(Collectors.toList());
    }

    public List<Produto> buscarPorFaixaDePreco(String categoria,
                                               BigDecimal precoMin,
                                               BigDecimal precoMax) {
        try {
            // Primeiro faz a query pela categoria
            Iterator<Page<Produto>> pages = produtoTable.index("PrecoIndex")
                    .query(QueryConditional.keyEqualTo(
                            Key.builder().partitionValue(categoria).build()
                    )).iterator();

            List<Produto> resultados = new ArrayList<>();

            while (pages.hasNext()) {
                Page<Produto> page = pages.next();
                for (Produto produto : page.items()) {
                    // Filtra localmente pelo preço
                    if (produto.getPreco().compareTo(precoMin) >= 0 &&
                            produto.getPreco().compareTo(precoMax) <= 0) {
                        resultados.add(produto);
                    }
                }
            }

            return resultados;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar produtos por faixa de preço", e);
        }
    }

    public void deletar(String id, String sku) {
        Key key = Key.builder()
                .partitionValue(id)
                .sortValue(sku)
                .build();
        produtoTable.deleteItem(key);
    }

    public List<Produto> listarTodos() {
        return produtoTable.scan().items().stream()
                .collect(Collectors.toList());
    }

    public Produto atualizar(Produto produto) {
        return produtoTable.updateItem(produto);
    }

    // Busca paginada
    public PageIterable<Produto> listarPaginado(int limit, String lastEvaluatedKey) {
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .limit(limit)
                .build();

        return produtoTable.scan(request);
    }

}
