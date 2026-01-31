package com.oryzem.backend.core.events;

public class NoOpEventPublisher implements EventPublisher {
    @Override
    public void publish(DomainEvent event) {
        // intentionally no-op
    }
}
