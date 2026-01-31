package com.oryzem.backend.core.settings;

import java.util.Optional;

public interface GlobalSettings {
    Optional<String> get(String key);
}
