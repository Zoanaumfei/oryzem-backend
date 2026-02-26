package com.oryzem.backend.modules.vehicles;

import com.oryzem.backend.modules.vehicles.controller.VehicleProjectController;
import com.oryzem.backend.modules.vehicles.dto.VehicleProjectResponse;
import com.oryzem.backend.modules.vehicles.dto.VehicleProjectUpsertRequest;
import com.oryzem.backend.modules.vehicles.service.VehicleProjectService;
import com.oryzem.backend.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleProjectController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class VehicleProjectControllerTest {

    private static final String VALID_REQUEST = """
            {
              "customer": "Sao Jose dos Pinhais",
              "projectName": "Udara",
              "description": "DC 1.6 MPI MQ Domestic & Paraguay",
              "rgTemplate": "Standart",
              "status": "Active",
              "progress": 0,
              "ME": "2026-02-24",
              "PVS": "2026-02-24",
              "S0": "2026-02-24",
              "SOP": "2026-02-24",
              "TPPA": "2026-02-24",
              "VFF": "2026-02-24",
              "bodyFrame_PVS": "2026-02-24",
              "bodyFrame_S0": "2026-02-24",
              "bodyFrame_VFF": "2026-02-24",
              "PVS_ELET_TBT": "2026-02-24",
              "S0_ELET_TBT": "2026-02-24",
              "VFF_ELET_TBT": "2026-02-24",
              "PVS_M100": "2026-02-24",
              "S0_M100": "2026-02-24",
              "VFF_M100": "2026-02-24",
              "PVS_ZP5_TBT": "2026-02-24",
              "S0_ZP5_TBT": "2026-02-24",
              "VFF_ZP5_TBT": "2026-02-24",
              "PVS_ZP7_TBT": "2026-02-24",
              "S0_ZP7_TBT": "2026-02-24",
              "VFF_ZP7_TBT": "2026-02-24"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleProjectService service;

    @Test
    void shouldUpsertVehicleProjectSuccessfully() throws Exception {
        VehicleProjectResponse response = sampleResponse();
        when(service.upsert(eq("VW247_1"), eq("ALS1"), any(VehicleProjectUpsertRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/projects/{projectId}/als/{als}", "VW247_1", "ALS1")
                        .with(internalUserJwt())
                        .contentType("application/json")
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value("VW247_1"))
                .andExpect(jsonPath("$.als").value("ALS1"))
                .andExpect(jsonPath("$.customer").value("Sao Jose dos Pinhais"))
                .andExpect(jsonPath("$.ME").value("2026-02-24"))
                .andExpect(jsonPath("$.updatedBy").value("email:internal.user@oryzem.com"));
    }

    @Test
    void shouldReturn400WhenPayloadIsInvalid() throws Exception {
        String invalidRequest = "{\"customer\":\"Only customer\"}";

        mockMvc.perform(put("/api/v1/projects/{projectId}/als/{als}", "VW247_1", "ALS1")
                        .with(internalUserJwt())
                        .contentType("application/json")
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenDateIsNotCalendarValid() throws Exception {
        String invalidDateRequest = VALID_REQUEST.replace("\"ME\": \"2026-02-24\"", "\"ME\": \"2026-02-30\"");

        mockMvc.perform(put("/api/v1/projects/{projectId}/als/{als}", "VW247_1", "ALS1")
                        .with(internalUserJwt())
                        .contentType("application/json")
                        .content(invalidDateRequest))
                .andExpect(status().isBadRequest());

        verify(service, never()).upsert(any(), any(), any(), any());
    }

    @Test
    void shouldListVehicleProjectsByProjectId() throws Exception {
        when(service.listByProjectId("VW247_1"))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/projects/{projectId}/als", "VW247_1")
                        .with(internalUserJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").value("VW247_1"))
                .andExpect(jsonPath("$[0].als").value("ALS1"));
    }

    @Test
    void shouldGetSingleVehicleProject() throws Exception {
        when(service.getById("VW247_1", "ALS1"))
                .thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/projects/{projectId}/als/{als}", "VW247_1", "ALS1")
                        .with(internalUserJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value("VW247_1"))
                .andExpect(jsonPath("$.als").value("ALS1"));
    }

    @Test
    void shouldReturn403ForExternalUser() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/als", "VW247_1")
                        .with(externalUserJwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/als", "VW247_1"))
                .andExpect(status().isUnauthorized());
    }

    private RequestPostProcessor internalUserJwt() {
        return jwt()
                .jwt(token -> token.claim("email", "internal.user@oryzem.com"))
                .authorities(new SimpleGrantedAuthority("Internal-User"));
    }

    private RequestPostProcessor externalUserJwt() {
        return jwt()
                .jwt(token -> token.claim("email", "external.user@oryzem.com"))
                .authorities(new SimpleGrantedAuthority("External-User"));
    }

    private VehicleProjectResponse sampleResponse() {
        return VehicleProjectResponse.builder()
                .projectId("VW247_1")
                .als("ALS1")
                .customer("Sao Jose dos Pinhais")
                .projectName("Udara")
                .description("DC 1.6 MPI MQ Domestic & Paraguay")
                .rgTemplate("Standart")
                .status("Active")
                .progress(0)
                .me("2026-02-24")
                .pvs("2026-02-24")
                .s0("2026-02-24")
                .sop("2026-02-24")
                .tppa("2026-02-24")
                .vff("2026-02-24")
                .bodyFramePvs("2026-02-24")
                .bodyFrameS0("2026-02-24")
                .bodyFrameVff("2026-02-24")
                .pvsEletTbt("2026-02-24")
                .s0EletTbt("2026-02-24")
                .vffEletTbt("2026-02-24")
                .pvsM100("2026-02-24")
                .s0M100("2026-02-24")
                .vffM100("2026-02-24")
                .pvsZp5Tbt("2026-02-24")
                .s0Zp5Tbt("2026-02-24")
                .vffZp5Tbt("2026-02-24")
                .pvsZp7Tbt("2026-02-24")
                .s0Zp7Tbt("2026-02-24")
                .vffZp7Tbt("2026-02-24")
                .updatedAt("2026-02-24T19:10:00Z")
                .updatedBy("email:internal.user@oryzem.com")
                .build();
    }
}
