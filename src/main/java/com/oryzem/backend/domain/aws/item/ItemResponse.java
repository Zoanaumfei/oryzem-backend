/**
 * ItemResponse – DTO de Saída (Data Transfer Object)
 *
 * <p>Responsável por modelar os dados retornados do backend para o frontend
 * após operações relacionadas a Item.</p>
 *
 * <h3>Responsabilidades</h3>
 * <ul>
 *   <li>Formatar a resposta da API</li>
 *   <li>Adicionar metadados de retorno (mensagens, status)</li>
 *   <li>Ocultar dados sensíveis e detalhes internos</li>
 *   <li>Garantir padrão consistente de respostas</li>
 * </ul>
 *
 * <h3>Boas práticas</h3>
 * <ul>
 *   <li>Não conter regra de negócio</li>
 *   <li>Não expor entidades do domínio</li>
 *   <li>Servir apenas como contrato de saída</li>
 * </ul>
 *
 * <p><strong>Analogia:</strong> Funciona como um recibo da operação,
 * confirmando o que foi processado sem expor detalhes internos.</p>
 */


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
    private String processNumber;
    private String partDescription;
    private String tbtVffDate;
    private String tbtPvsDate;
    private String tbt0sDate;
    private String sopDate;
    private String createdAt;
    private String updatedAt;
    private String status;
    private String message;
}
