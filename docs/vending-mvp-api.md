# Vending Management MVP - API

## Overview

New modules:

- `catalog`: products and pricing
- `inventory`: stock position and movements
- `orders`: internal and external normalized orders
- `integrations`: marketplace clients (`iFood`, `99Food`) and payload mapper
- `sync`: manual order synchronization from marketplace clients

All endpoints below follow JSON request/response.

## 1) Catalog

### POST `/api/catalog/products`

Create a product.

Request:

```json
{
  "sku": "PIZZA-CALABRESA",
  "name": "Pizza Calabresa",
  "category": "PIZZA",
  "unitPrice": 59.90,
  "active": true
}
```

Response `201 Created`:

```json
{
  "id": "f67f5ba0-cc8b-4f05-bf7f-60b111e615ad",
  "sku": "PIZZA-CALABRESA",
  "name": "Pizza Calabresa",
  "category": "PIZZA",
  "unitPrice": 59.90,
  "active": true,
  "createdAt": "2026-02-26T13:10:11.094860100Z",
  "updatedAt": "2026-02-26T13:10:11.094860100Z"
}
```

### GET `/api/catalog/products`

List products.

Response `200 OK`:

```json
[
  {
    "id": "f67f5ba0-cc8b-4f05-bf7f-60b111e615ad",
    "sku": "PIZZA-CALABRESA",
    "name": "Pizza Calabresa",
    "category": "PIZZA",
    "unitPrice": 59.90,
    "active": true,
    "createdAt": "2026-02-26T13:10:11.094860100Z",
    "updatedAt": "2026-02-26T13:10:11.094860100Z"
  }
]
```

## 2) Inventory

### POST `/api/inventory/movements`

Register stock movement.

Request:

```json
{
  "productId": "f67f5ba0-cc8b-4f05-bf7f-60b111e615ad",
  "type": "IN",
  "quantity": 30,
  "reason": "INITIAL_STOCK",
  "minimumLevel": 5
}
```

Response `201 Created`:

```json
{
  "id": "fd873669-b838-4af1-8cc4-6aad1bf45b1d",
  "productId": "f67f5ba0-cc8b-4f05-bf7f-60b111e615ad",
  "type": "IN",
  "quantity": 30,
  "reason": "INITIAL_STOCK",
  "referenceOrderId": null,
  "createdAt": "2026-02-26T13:15:11.024Z"
}
```

### GET `/api/inventory/{productId}`

Get inventory position.

Response `200 OK`:

```json
{
  "productId": "f67f5ba0-cc8b-4f05-bf7f-60b111e615ad",
  "quantityAvailable": 30,
  "minimumLevel": 5,
  "updatedAt": "2026-02-26T13:15:11.024Z"
}
```

## 3) Orders

### POST `/api/orders`

Create order (internal or external normalized).

Note: for external orders (`source != INTERNAL`), `merchantId` and `externalId` are required.

Request:

```json
{
  "source": "INTERNAL",
  "customerName": "Cliente Balcao",
  "items": [
    {
      "productId": "f67f5ba0-cc8b-4f05-bf7f-60b111e615ad",
      "nameSnapshot": "Pizza Calabresa",
      "quantity": 2,
      "unitPrice": 59.90
    }
  ]
}
```

Response `201 Created`:

```json
{
  "id": "de358f98-09c5-4e29-b250-73028fef2228",
  "source": "INTERNAL",
  "merchantId": null,
  "externalId": null,
  "customerName": "Cliente Balcao",
  "items": [
    {
      "productId": "f67f5ba0-cc8b-4f05-bf7f-60b111e615ad",
      "nameSnapshot": "Pizza Calabresa",
      "quantity": 2,
      "unitPrice": 59.90
    }
  ],
  "totalAmount": 119.80,
  "status": "RECEIVED",
  "createdAt": "2026-02-26T13:20:11.024Z",
  "updatedAt": "2026-02-26T13:20:11.024Z",
  "stockAllocated": false,
  "allocationError": null,
  "message": "Order created successfully"
}
```

### GET `/api/orders/{id}`

Get order by id.

### POST `/api/orders/{id}/confirm`

Confirm order and allocate stock. Idempotent for duplicate confirmation.

Success response `200 OK`:

```json
{
  "id": "de358f98-09c5-4e29-b250-73028fef2228",
  "status": "CONFIRMED",
  "stockAllocated": true,
  "message": "Order confirmed"
}
```

Insufficient stock response `409 Conflict`:

```json
{
  "timestamp": "2026-02-26T13:22:11.024Z",
  "path": "/api/orders/de358f98-09c5-4e29-b250-73028fef2228/confirm",
  "message": "Insufficient stock for product f67f5ba0-cc8b-4f05-bf7f-60b111e615ad: available=1, requested=2"
}
```

### POST `/api/orders/{id}/cancel`

Cancel order and restock when order was previously allocated.

### POST `/api/orders/{id}/prepare`

Move order from `CONFIRMED` to `PREPARING`.

### POST `/api/orders/{id}/dispatch`

Move order from `PREPARING` to `DISPATCHED`.

### POST `/api/orders/{id}/complete`

Move order from `DISPATCHED` to `COMPLETED`.

## 4) Integrations sync

### POST `/api/integrations/sync/orders`

Manual import from configured marketplace clients.

Response `200 OK`:

```json
{
  "importedCount": 2,
  "duplicateCount": 0,
  "failedCount": 0,
  "orderIds": [
    "18527e37-ae39-4db7-a4f3-fb4fb933fcf4",
    "e81119f6-5801-430e-bab0-28dbde43dfe7"
  ],
  "errors": []
}
```

### POST `/api/integrations/ifood/webhook`

iFood webhook endpoint (principal channel). Receives event array payload and processes `PLC` events.
When `IFOOD_WEBHOOK_SECRET` is configured, requests must include `x-ifood-signature`.

### POST `/api/integrations/ifood/reconcile`

Manual reconciliation from iFood polling (fallback channel).

## Integration notes (MVP)

- `MarketplaceClient` exposes:
  - `fetchNewOrders()`
  - `ackOrder(merchantId, externalOrderId)`
  - `updateOrderStatus(merchantId, externalOrderId, status)`
- `IfoodMarketplaceClient` now uses real iFood API calls for:
  - OAuth (`client_credentials`) with in-memory token cache
  - Polling (`/events/v1.0/events:polling`)
  - Acknowledgment (`/events/v1.0/events/acknowledgment`)
  - Order details (`/order/v1.0/orders/{id}`) for payload normalization
  - Outbound status commands:
    - `CONFIRMED` -> `/order/v1.0/orders/{id}/confirm`
    - `PREPARING` -> `/order/v1.0/orders/{id}/startPreparation`
    - `DISPATCHED` -> `/order/v1.0/orders/{id}/dispatch`
    - `CANCELED` -> `/order/v1.0/orders/{id}/cancellationReasons` + `/order/v1.0/orders/{id}/requestCancellation`
- `NineNineMarketplaceClient` remains a stub.
- Payload normalization is done by `MarketplaceOrderMapper`.
- Mapper resolves products by `productId` or by `sku` from catalog.
- External order idempotency/dedup key is `source + merchantId + externalOrderId`.
- Webhook channel is available for iFood and polling can be enabled for reconciliation.
- Webhook dedupe can be durably persisted in DynamoDB (`merchantId + eventId`) when event ledger is enabled.

Required env vars for iFood:

- `IFOOD_ENABLED=true`
- `IFOOD_CLIENT_ID=...`
- `IFOOD_CLIENT_SECRET=...`
- `IFOOD_MERCHANT_IDS=<merchant-uuid[,merchant-uuid]>`
- Optional:
  - `IFOOD_WEBHOOK_ENABLED=true`
  - `IFOOD_WEBHOOK_SECRET=...`
  - `IFOOD_EVENT_LEDGER_ENABLED=true`
  - `IFOOD_EVENT_LEDGER_TTL_DAYS=7`
  - `DYNAMODB_TABLE_IFOOD_EVENT_LEDGER=<table-name>`
  - `IFOOD_RECONCILIATION_ENABLED=true`
  - `IFOOD_RECONCILIATION_INTERVAL_SECONDS=300`
  - `IFOOD_EVENT_TYPES=PLC` (default)
  - `IFOOD_BASE_URL`, `IFOOD_PREFERRED_CANCELLATION_CODE`, `IFOOD_CANCELLATION_REASON`, `IFOOD_REQUEST_TIMEOUT_SECONDS`, `IFOOD_TOKEN_REFRESH_SKEW_SECONDS`
