package com.thitsaworks.mojaloop.coreconnector.component.event;

public interface DomainEventPublisher {

    <E extends DomainEvent> void publish(E event);

}
