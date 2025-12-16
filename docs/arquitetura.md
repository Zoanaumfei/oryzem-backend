### Fluxo de dados IDA — PASSO A PASSO

**1 Frontend**
-> A página web faz um "POST" para uma URL
```url
http:oryzem/api/v1/items
```
-> Envia um JSON no corpo da requisição
```json
{
"partNumberID": "123",
"supplierID": "ABC"
}
```
**2 Item Controller**
-> O Spring (ItemController) identifica a URL e o método HTTP
```java
java
@PostMapping("/items")
```
-> O JSON é convertido automaticamente em:
```java
ItemRequest request
```
**3 ItemController → ItemService**
O Controller não processa dados
Ele apenas chama o Service
```java
return itemService.create(request);
```
**4 ItemService**
Recebe o ItemRequest
-> Converte para objeto de domínio (Item)
```java
Item item = ItemMapper.toDomain(request);
```
-> Aplica regra de negócio
-> Decide salvar

**5 ItemService → ItemRepository**
O Service chama o Repository
```java
itemRepository.save(item);
```

**6 ItemRepository**
-> Converte Item para formato do banco
-> Salva no Banco de Dados
Aqui termina o fluxo de ida

### Fluxo de VOLTA (resposta)

**7 Banco de Dados**
-> Retorna o Item salvo (ou confirmação)

**8 ItemRepository → ItemService**
-> O Repository devolve o Item salvo

**9 ItemService**
-> Converte Item para DTO de saída
```java
ItemResponse response =
ItemMapper.toResponse(item, "Item criado com sucesso");
```
**10 ItemService → ItemController**
-> Retorna o ItemResponse

**11 ItemController → Frontend**
-> O Controller devolve o DTO
-> Spring converte para JSON, O JSON é convertido automaticamente.
```json
{
  "partNumberID": "123",
  "supplierID": "ABC",
  "createdAt": "2025-01-10",
  "message": "Item criado com sucesso"
}
```
### O DTO de Entrada (ItemRequest)
* Ele representa os dados que vem do Frontend
* Ele e usado pelo Controller para receber dados do Frontend

Frontend
↓ JSON
ItemRequest (DTO)
↓
ItemController

### O DTO de Saida (Item Response)
* Ele e usado pelo Controller para responder
* Ele representa o que o Frontend vai receber

ItemController
↓
ItemResponse
↓ JSON
Frontend

### O ItemResponse:
* É construído no Service
* retornado pelo Controller
* consumido pelo Frontend

Resumo final (bem objetivo)
✔ DTO de entrada: vem do Frontend, entra pelo Controller
✔ DTO de saída: sai pelo Controller, vai para o Frontend
✔ Controller é o guardião do contrato
✔ DTO não é regra de negócio

### Quem converte o JSON em ItemRequest?
O Spring Boot, usando a biblioteca Jackson.
Você não escreve código para isso.
* Onde isso acontece?
Quando você tem algo assim no Controller:

```java
  @PostMapping("/items")
  public ItemResponse create(@RequestBody ItemRequest request) {
  }
/* Esse @RequestBody diz ao Spring:
```
“Pegue o JSON da requisição
e transforme em um objeto Java”
