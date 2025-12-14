// ItemResponse.java - DTO de Saída (Data Transfer Object)
// Função: Modela os dados que voltam para o frontend.
// Responsabilidades:
// 1. FORMATAR RESPOSTA: Campos que o frontend precisa
// 2. ADICIONAR METADADOS: message, status, timestamps
// 3. OCULTAR DADOS SENSÍVEIS: Nunca expor dados internos
// 4. PADRÃO CONSISTENTE: Todas respostas seguem mesmo formato
// Exemplo:
// public class ItemResponse
//    private String partNumberID
//    private String supplierID
//    private String createdAt  // Já formatada
//    private String message    // "Item criado com sucesso"
// Analogia: É o recibo da compra - mostra o que foi processado, com confirmação.

package com.oryzem.backend.domain.aws.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemResponse {
    private String partNumberID;
    private String supplierID;
    private String createdAt;
    private String message;
}
