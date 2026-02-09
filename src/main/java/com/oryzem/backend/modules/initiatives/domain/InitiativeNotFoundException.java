package com.oryzem.backend.modules.initiatives.domain;

import com.oryzem.backend.shared.exceptions.NotFoundException;

public class InitiativeNotFoundException extends NotFoundException {
    public InitiativeNotFoundException(String initiativeId) {
        super(String.format("Initiative %s not found", initiativeId));
    }
}
