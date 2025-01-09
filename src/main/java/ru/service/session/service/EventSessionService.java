package ru.service.session.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.service.session.dto.event.internal.EventResponse;
import ru.service.session.dto.hero.HeroResponse;
import ru.service.session.model.EventSession;
import ru.service.session.util.exception.EntityNotFoundException;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class EventSessionService {

    //todo
    // можно поставить Spring session и Redis
    private final Map<String, EventSession> eventSessions = new ConcurrentHashMap<>();

    public EventSession create(String userId, EventResponse event, HeroResponse hero) {
        log.info("Starting event: {} for user's id: {} hero", event.getTitle(), userId);
        EventSession eventSession = new EventSession(event, hero, null, new Date(System.currentTimeMillis()));
        put(userId, eventSession);
        return get(userId);
    }

    public EventSession put(String userId, EventSession eventSession) {
        log.info("Put event session: {} for user's id: {} hero", eventSession.getEvent().getTitle(), userId);
        eventSessions.put(userId, eventSession);
        return get(userId);
    }

    public void remove(String userId) {
        log.info("Remove solving session for user's id: {} hero", userId);
        eventSessions.remove(userId);
    }

    public EventSession get(String userId) {
        log.info("Getting event for user's id: {} hero", userId);
        return Optional.of(eventSessions.get(userId))
                .orElseThrow(
                        () -> new EntityNotFoundException("Session not found"));
    }

    public Optional<EventSession> getOptional(String userId) {
        log.info("Getting event for user's id: {} hero", userId);
        return Optional.of(eventSessions.get(userId));
    }

    public void checkUnfinishedEvent(final String userId, long sessionLifetime) {
        if (eventSessions.containsKey(userId) &&
                (System.currentTimeMillis() - eventSessions.get(userId).getRegisteredAt().getTime() < sessionLifetime)) {
            throw new IllegalStateException("The current event is not finished yet");
        }
    }
}
