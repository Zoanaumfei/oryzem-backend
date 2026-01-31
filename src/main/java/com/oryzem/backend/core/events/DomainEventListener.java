package com.oryzem.backend.core.events;

public interface DomainEventListener<T extends DomainEvent> {
    void onEvent(T event);
}
