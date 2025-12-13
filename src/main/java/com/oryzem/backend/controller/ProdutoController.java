package com.oryzem.backend.controller;

import com.oryzem.backend.model.Produto;
import com.oryzem.backend.services.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<Produto> criar(@Valid @RequestBody Produto produto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(produtoService.criarProduto(produto));
    }

    @GetMapping("/{id}/{sku}")
    public ResponseEntity<Produto> buscarPorId(
            @PathVariable String id,
            @PathVariable String sku) {
        return ResponseEntity.ok(produtoService.buscarPorId(id, sku));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Produto>> buscarPorCategoria(
            @PathVariable String categoria) {
        return ResponseEntity.ok(produtoService.buscarPorCategoria(categoria));
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(produtoService.listarProdutosPaginados(page, size));
    }

    @PutMapping("/{id}/{sku}/estoque")
    public ResponseEntity<Produto> atualizarEstoque(
            @PathVariable String id,
            @PathVariable String sku,
            @RequestParam Integer quantidade) {
        return ResponseEntity.ok(
                produtoService.atualizarEstoque(id, sku, quantidade));
    }

    @DeleteMapping("/{id}/{sku}")
    public ResponseEntity<Void> desativar(
            @PathVariable String id,
            @PathVariable String sku) {
        produtoService.desativarProduto(id, sku);
        return ResponseEntity.noContent().build();
    }
}
