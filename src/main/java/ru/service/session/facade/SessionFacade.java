package ru.service.session.facade;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.service.session.client.EventApi;
import ru.service.session.client.HeroApi;
import ru.service.session.config.common.SessionVariables;
import ru.service.session.config.websocket.WebSocketProxy;
import ru.service.session.dto.event.internal.DecisionResponse;
import ru.service.session.dto.event.internal.EventResponse;
import ru.service.session.dto.event.ws.DecisionResultWsResponse;
import ru.service.session.dto.event.ws.EventWsResponse;
import ru.service.session.dto.hero.HeroResponse;
import ru.service.session.model.EventSession;
import ru.service.session.service.EventProcessingService;
import ru.service.session.service.EventSessionService;
import ru.service.session.util.exception.EntityNotFoundException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SessionFacade {

    private final EventApi eventApi;
    private final HeroApi heroApi;
    private final WebSocketProxy webSocketProxy;
    private final EventSessionService eventSessionService;
    private final EventProcessingService eventProcessingService;
    private final ScheduledExecutorService scheduler;
    private final SessionVariables sessionVariables;

    @Autowired
    public SessionFacade(EventApi eventApi,
                         HeroApi heroApi,
                         WebSocketProxy webSocketProxy,
                         EventSessionService eventSessionService,
                         EventProcessingService eventProcessingService,
                         SessionVariables sessionVariables) {
        this.eventApi = eventApi;
        this.heroApi = heroApi;
        this.webSocketProxy = webSocketProxy;
        this.eventSessionService = eventSessionService;
        this.eventProcessingService = eventProcessingService;
        this.scheduler = Executors.newScheduledThreadPool(10);
        this.sessionVariables = sessionVariables;
    }

    public void setChosenDecisionInCurrentEvent(String userId, String chosenDecisionId) {
        log.info("Beginning to process chosen decision id: {} for user id: {}", chosenDecisionId, userId);
        EventSession actualSession = eventSessionService.get(userId);
        if (actualSession == null || actualSession.getEvent() == null) {
            throw new EntityNotFoundException("Current event did not found");
        }
        if (actualSession.getDecision() != null) {
            throw new IllegalStateException("Decision already selected");
        }
        DecisionResponse decision = eventProcessingService.findChosenDecisionInCurrentEvent(
                actualSession.getEvent(), Long.parseLong(chosenDecisionId));
        actualSession.setDecision(decision);
        eventSessionService.put(userId, actualSession);
    }

    public void processRandomEvent(String userId, String token) throws ExecutionException, InterruptedException {
        log.info("Searching random event for user's id: {} hero", userId);
        eventSessionService.checkUnfinishedEvent(userId, sessionVariables.getLifetime());
        ResponseEntity<HeroResponse> heroResponse = heroApi.getOwn("Bearer " + token);
        HeroResponse hero = heroResponse.getBody();
        ResponseEntity<EventResponse> eventResponse = eventApi.getRandom("Bearer " + token);
        EventResponse event = eventResponse.getBody();
        EventSession eventSession = eventSessionService.create(userId, event, hero);
        webSocketProxy.sendMessage(userId, "/topic/event", new EventWsResponse(eventSession.getEvent()));
        DecisionResultWsResponse decisionResultWsResponse =
                scheduler.schedule(() -> {
                    EventSession actualSession = eventSessionService.get(userId);
                    return eventProcessingService.processEvent(actualSession.getDecision(), actualSession.getEvent(), actualSession.getHero());
                }, sessionVariables.getDuration(), TimeUnit.MILLISECONDS).get();
        eventSessionService.remove(userId);
        webSocketProxy.sendMessage(userId, "/topic/event", decisionResultWsResponse);
    }
}
