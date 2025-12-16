# Objetivo deste arquivo
# Rgistrar aprendizados, conceitos, dúvidas e decisões técnicas
# facilitando consultas futuras

### DTO (Data Transfer Object)
**O que é:**  
Usado para transportar dados entre camadas (Controller ↔ Service ↔ Repository).

**Por que usar:**
- Evita expor entidades
- Facilita versionamento de API
- Melhora organização

**Exemplo:**

```java
public class UserDTO {
    private String name;
    private int age;
}
```
