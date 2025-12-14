// DTO de Entrada (Data Transfer Object)
// Função: Modela os dados que chegam do frontend.
// Responsabilidades:
// 1. DEFINIR CONTRATO: Quais campos o frontend deve enviar
// 2. VALIDAÇÃO: @NotBlank, @Size, @Pattern
// 3. SEGURANÇA: Separa dados da API da entidade do banco
// 4. SIMPLIFICAÇÃO: Pode ter menos campos que a entidade

//  Exemplo:
// public class ItemRequest
// @NotBlank
// private String partNumberID;  // Obrigatório
//
// @NotBlank
// private String supplierID;    // Obrigatório
//
// NÃO tem createdAt - é gerado no servidor!
// Analogia: É o formulário de cadastro - define o que o usuário precisa preencher.

package com.oryzem.backend.domain.aws.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {
    @NotBlank(message = "PartNumberID é obrigatório")
    @Size(max = 50, message = "PartNumberID deve ter no máximo 50 caracteres")
    private String partNumberID;

    @NotBlank(message = "SupplierID é obrigatório")
    @Size(max = 50, message = "SupplierID deve ter no máximo 50 caracteres")
    private String supplierID;
}

