package com.oryzem.backend.modules.vehicles.domain;

import com.oryzem.backend.shared.exceptions.NotFoundException;

public class VehicleProjectNotFoundException extends NotFoundException {
    public VehicleProjectNotFoundException(String projectId, String als) {
        super(String.format(
                "Vehicle project %s/%s not found",
                projectId,
                als
        ));
    }
}
