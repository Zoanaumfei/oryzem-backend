package com.oryzem.backend.modules.vehicles;

import com.oryzem.backend.modules.vehicles.domain.VehicleProject;
import com.oryzem.backend.modules.vehicles.dto.VehicleProjectResponse;
import com.oryzem.backend.modules.vehicles.dto.VehicleProjectUpsertRequest;
import com.oryzem.backend.modules.vehicles.repository.VehicleProjectRepository;
import com.oryzem.backend.modules.vehicles.service.VehicleProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleProjectServiceTest {

    @Mock
    private VehicleProjectRepository repository;

    @InjectMocks
    private VehicleProjectService service;

    @Test
    void shouldUseJwtEmailForUpdatedBy() {
        VehicleProjectUpsertRequest request = validRequest();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("subject-123")
                .claim("email", "user@oryzem.com")
                .build();

        when(repository.save(any(VehicleProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VehicleProjectResponse response = service.upsert("VW247_1", "ALS1", request, jwt);

        assertThat(response.getUpdatedBy()).isEqualTo("email:user@oryzem.com");
        assertThat(response.getUpdatedAt()).isNotBlank();

        ArgumentCaptor<VehicleProject> captor = ArgumentCaptor.forClass(VehicleProject.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUpdatedBy()).isEqualTo("email:user@oryzem.com");
    }

    @Test
    void shouldFailWhenJwtHasNoEmail() {
        VehicleProjectUpsertRequest request = validRequest();
        Jwt jwtWithoutEmail = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("subject-123")
                .build();

        assertThatThrownBy(() -> service.upsert("VW247_1", "ALS1", request, jwtWithoutEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authenticated user email is required");

        verify(repository, never()).save(any(VehicleProject.class));
    }

    private VehicleProjectUpsertRequest validRequest() {
        return VehicleProjectUpsertRequest.builder()
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
                .build();
    }
}
