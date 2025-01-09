package ru.service.session.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.service.session.dto.event.internal.DecisionResponse;
import ru.service.session.dto.event.internal.EventResponse;
import ru.service.session.dto.hero.HeroResponse;

import java.util.Date;

@AllArgsConstructor
@Data
public class EventSession {

    private EventResponse event;
    private HeroResponse hero;
    private DecisionResponse decision;
    private Date registeredAt;
}