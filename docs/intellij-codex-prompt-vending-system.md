# Prompt estruturado para IntelliJ + Codex

Use o prompt abaixo no IntelliJ (Codex) para iniciar a implementação do seu sistema de gestão de vendas e estoque, com integração de marketplaces (iFood e 99Food).

## Prompt sugerido (copiar e colar)

```text
Contexto do projeto
- Stack: Java 17 + Spring Boot 3.5.8 + Maven + DynamoDB + S3.
- Repositório backend já existente, com padrão por módulos em `src/main/java/com/oryzem/backend/modules/...`.
- Quero evoluir esse backend para um sistema de gestão de vendas para pequenos negócios (ex.: pizzaria delivery).
- O sistema deve integrar pedidos de agregadores como iFood e 99Food e sincronizar estoque local.

Objetivo
Implementar um MVP de "Vending Management System" com:
1) Gestão de produtos e estoque local.
2) Gestão de pedidos internos e pedidos vindos de integrações externas.
3) Normalização dos pedidos externos para um modelo interno único.
4) Regras de baixa de estoque por pedido confirmado.
5) Base pronta para expansão (financeiro, entregas, relatórios).

Restrições técnicas
- Seguir a arquitetura já usada no repositório (controller/service/repository/dto/domain).
- Reutilizar padrões de módulos existentes (`items`, `projects`, etc.) como referência de estilo.
- Não quebrar endpoints existentes.
- Criar testes unitários para regras críticas de negócio.
- Documentar endpoints novos.

Estrutura alvo (novos módulos)
Criar módulos em `src/main/java/com/oryzem/backend/modules`:
- `catalog`: produto, preço, status, categoria.
- `inventory`: estoque atual, movimentações (entrada, saída, ajuste), motivo.
- `orders`: pedido interno com itens, status, origem e totais.
- `integrations`: conectores iFood/99Food (inicialmente adaptadores mockáveis).
- `sync`: jobs/serviços de sincronização de pedidos e atualização de status.

Modelo de domínio inicial
- Product: id, sku, name, category, unitPrice, active.
- InventoryItem: productId, quantityAvailable, minimumLevel, updatedAt.
- InventoryMovement: id, productId, type(IN/OUT/ADJUSTMENT), quantity, reason, referenceOrderId, createdAt.
- Order: id, source(INTERNAL, IFOOD, NINENINE), externalId, customerName, items, totalAmount, status(RECEIVED, CONFIRMED, PREPARING, DISPATCHED, COMPLETED, CANCELED), createdAt.
- OrderItem: productId, nameSnapshot, quantity, unitPrice.

Fluxos principais
1) Pedido recebido (interno ou integração externa) -> validar -> persistir.
2) Pedido CONFIRMED -> baixar estoque item a item de forma transacional/idempotente.
3) Falta de estoque -> marcar pedido com erro de alocação e registrar evento/auditoria.
4) Cancelamento -> estornar estoque quando aplicável.

Integrações (fase MVP)
- Criar interface `MarketplaceClient` com operações:
  - `fetchNewOrders()`
  - `ackOrder(externalOrderId)`
  - `updateOrderStatus(externalOrderId, status)`
- Implementar:
  - `IfoodMarketplaceClient` (stub com TODO para API real).
  - `NineNineMarketplaceClient` (stub com TODO para API real).
- Criar mapper de payload externo -> `Order` interno.

API (MVP)
- `POST /api/catalog/products`
- `GET /api/catalog/products`
- `POST /api/inventory/movements`
- `GET /api/inventory/{productId}`
- `POST /api/orders`
- `GET /api/orders/{id}`
- `POST /api/orders/{id}/confirm`
- `POST /api/orders/{id}/cancel`
- `POST /api/integrations/sync/orders` (disparo manual de sincronização)

Critérios de aceite
- Consegue criar produto, registrar estoque, criar pedido e confirmar com baixa de estoque.
- Consegue importar pedido de stub iFood/99Food e normalizar para o modelo interno.
- Testes cobrindo:
  - baixa de estoque em confirmação;
  - bloqueio por estoque insuficiente;
  - idempotência em confirmação duplicada.
- Endpoints documentados com exemplos de request/response.

Plano de execução (entregas incrementais)
1) Criar domínio e DTOs de `catalog`, `inventory`, `orders`.
2) Implementar services + repositories e regras de negócio.
3) Implementar controllers REST.
4) Implementar camada `integrations` com stubs e mapper.
5) Implementar sincronização manual + testes.
6) Atualizar documentação técnica.

Formato de resposta esperado do Codex no IntelliJ
- Sempre listar:
  1) arquivos criados/alterados,
  2) resumo do que foi implementado,
  3) comandos para rodar testes,
  4) próximos passos sugeridos.
```

## Como adaptar rapidamente

- **Se quiser começar menor**: peça apenas `catalog + inventory` na primeira sprint.
- **Se quiser integração real já no início**: adicione credenciais via `application-dev.yml` e variáveis de ambiente, mantendo interfaces para mock em teste.
- **Se quiser multitenancy por loja**: incluir `tenantId` nas entidades e no contexto da requisição.

## Observações sobre a estrutura atual do projeto

A estrutura existente já favorece evolução modular, porque segue um padrão consistente por domínio (`controller`, `service`, `repository`, `dto`, `domain`) dentro de `modules`, o que facilita adicionar os novos módulos sugeridos sem refatorações grandes.
