package com.oryzem.backend.aws.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oryzem.backend.domain.aws.item.ItemController;
import com.oryzem.backend.domain.aws.item.ItemRequest;
import com.oryzem.backend.domain.aws.item.ItemResponse;
import com.oryzem.backend.domain.aws.item.ItemService;
import com.oryzem.backend.domain.aws.item.exception.ItemNotFoundException;
import com.oryzem.backend.config.aws.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ✅ Mock do Service (obrigatório em @WebMvcTest)
    @MockitoBean
    private ItemService itemService;

    /* =========================================================
       POST /api/v1/items
       ========================================================= */

    @Test
    void shouldCreateItemSuccessfully() throws Exception {

        ItemRequest request = new ItemRequest();
        request.setPartNumberID("PN123");
        request.setSupplierID("SUP456");

        ItemResponse response = ItemResponse.builder()
                .partNumberID("PN123")
                .supplierID("SUP456")
                .createdAt("2025-12-16T20:30:00Z")
                .message("Item criado com sucesso")
                .build();

        when(itemService.createItem(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/items")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partNumberID").value("PN123"))
                .andExpect(jsonPath("$.supplierID").value("SUP456"))
                .andExpect(jsonPath("$.message").value("Item criado com sucesso"));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {

        // partNumberID vazio → viola @NotBlank
        ItemRequest request = new ItemRequest();
        request.setPartNumberID("");
        request.setSupplierID("SUP456");

        mockMvc.perform(post("/api/v1/items")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409WhenItemAlreadyExists() throws Exception {

        ItemRequest request = new ItemRequest();
        request.setPartNumberID("PN123");
        request.setSupplierID("SUP456");

        when(itemService.createItem(any()))
                .thenThrow(new IllegalStateException("Item já existe"));

        mockMvc.perform(post("/api/v1/items")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    /* =========================================================
       GET /api/v1/items/{partNumberID}/{supplierID}
       ========================================================= */

    @Test
    void shouldGetItemSuccessfully() throws Exception {

        ItemResponse response = ItemResponse.builder()
                .partNumberID("PN123")
                .supplierID("SUP456")
                .createdAt("2025-12-16T20:30:00Z")
                .message("Item encontrado")
                .build();

        when(itemService.getItem("PN123", "SUP456"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/items/{partNumberID}/{supplierID}",
                        "PN123", "SUP456")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partNumberID").value("PN123"))
                .andExpect(jsonPath("$.supplierID").value("SUP456"))
                .andExpect(jsonPath("$.message").value("Item encontrado"));
    }

    @Test
    void shouldReturn404WhenItemNotFound() throws Exception {

        when(itemService.getItem(eq("PN999"), eq("SUP999")))
                .thenThrow(new ItemNotFoundException("Item nao encontrado"));

        mockMvc.perform(get("/api/v1/items/{partNumberID}/{supplierID}",
                        "PN999", "SUP999")
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }
}
