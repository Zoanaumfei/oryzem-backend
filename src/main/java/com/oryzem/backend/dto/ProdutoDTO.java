package com.oryzem.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Data
public class ProdutoDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal preco;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    private Integer quantidadeEstoque;

    @NotBlank(message = "Categoria é obrigatória")
    private String categoria;

    private String marca;
    private DetalhesProdutoDTO detalhes;
}

@Data
class DetalhesProdutoDTO {
    private String fabricante;
    private String modelo;
    private String dimensoes;
    private Double peso;
    private String garantia;
}
