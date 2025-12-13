package com.oryzem.backend.services;

import com.oryzem.backend.model.Produto;
import com.oryzem.backend.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public Produto criarProduto(Produto produto) {
        produto.setId(UUID.randomUUID().toString());
        produto.setDataCriacao(LocalDateTime.now());
        produto.setDataAtualizacao(LocalDateTime.now());
        produto.setAtivo(true);

        return produtoRepository.salvar(produto);
    }

    public Produto buscarPorId(String id, String sku) {
        return produtoRepository.buscarPorId(id, sku)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public List<Produto> buscarPorCategoria(String categoria) {
        return produtoRepository.buscarPorCategoria(categoria);
    }

    public List<Produto> buscarPorNome(String nome) {
        // Delega toda a lógica para o repositório
        return produtoRepository.buscarPorNome(nome);
    }

    public Produto atualizarEstoque(String id, String sku, Integer quantidade) {
        Produto produto = buscarPorId(id, sku);

        // Otimistic Locking com versão
        produto.setQuantidadeEstoque(quantidade);
        produto.setDataAtualizacao(LocalDateTime.now());

        return produtoRepository.atualizar(produto);
    }

    public void desativarProduto(String id, String sku) {
        Produto produto = buscarPorId(id, sku);
        produto.setAtivo(false);
        produto.setDataAtualizacao(LocalDateTime.now());
        produtoRepository.salvar(produto);
    }

    public List<Produto> listarProdutosPaginados(int page, int size) {
        return produtoRepository.listarPaginado(size, null)
                .items()
                .stream()
                .skip(page * size)
                .limit(size)
                .collect(Collectors.toList());
    }
}
