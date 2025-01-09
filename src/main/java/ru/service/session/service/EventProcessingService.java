package ru.service.session.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.service.session.dto.event.internal.DecisionResponse;
import ru.service.session.dto.event.internal.DecisionResultResponse;
import ru.service.session.dto.event.internal.EventResponse;
import ru.service.session.dto.event.ws.DecisionResultWsResponse;
import ru.service.session.dto.hero.HeroResponse;
import ru.service.session.service.attributeCheck.AttributeCheckService;
import ru.service.session.service.attributeCheck.CharacteristicValue;
import ru.service.session.service.attributeCheck.TryData;
import ru.service.session.util.exception.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static ru.service.session.dto.event.internal.DecisionType.TEXT;


@Service
@Slf4j
public class EventProcessingService {

    private final AttributeCheckService attributeCheckService;

    @Autowired
    public EventProcessingService(AttributeCheckService attributeCheckService) {
        this.attributeCheckService = attributeCheckService;
    }

    public DecisionResultWsResponse processEvent(DecisionResponse decision, EventResponse event, HeroResponse hero) {
        log.info("Process event for user's id: {} hero", hero.getUserId());
        DecisionResponse actualDecision = decision == null ? autoChooseDecision(event, hero) : decision;
        return processDecision(hero, actualDecision);
    }

    public DecisionResponse findChosenDecisionInCurrentEvent(EventResponse event, Long decisionId) {
        log.info("Processing decision id: {}", decisionId);
        return event.getDecisions().stream()
                .filter(d -> d.getId().equals(decisionId))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("No decisions find"));
    }

    private DecisionResponse autoChooseDecision(EventResponse event, HeroResponse hero) {
        log.info("The user id: {} did not choose event's id: {} decision, auto-decision starting",
                hero.getUserId(), event.getId());
        List<CharacteristicValue> ratedCharacteristics = attributeCheckService.getHeroRatedCharacteristicsMaxMin(hero);
        // оставляем только те, которые встречаются в событии
        List<CharacteristicValue> possibleRatedOnEvent = new ArrayList<>();
        for (DecisionResponse d : event.getDecisions()) {
            for (CharacteristicValue cv : ratedCharacteristics) {
                if (d.getDecisionType().name().equals(cv.getDecisionType().name())) {
                    possibleRatedOnEvent.add(cv);
                }
            }
        }
        // выбираем самое простое решение
        Optional<DecisionResponse> decisionResponse = Optional.empty();
        if (!possibleRatedOnEvent.isEmpty()) {
            decisionResponse = event.getDecisions().stream()
                    .filter(d -> d.getDecisionType().name().equals(possibleRatedOnEvent.get(0).getDecisionType().name()))
                    .min(Comparator.comparingInt(DecisionResponse::getDifficulty));
        }
        // если такого нет, то выбираем текстовое
        return decisionResponse
                .orElseGet(() -> event.getDecisions().stream()
                        .filter(d -> d.getDecisionType().name().equals(TEXT.name()))
                        .findFirst()
                        .orElseThrow(() ->
                                new EntityNotFoundException("Proper decision not found")));
    }

    private DecisionResultWsResponse processDecision(HeroResponse hero, DecisionResponse decision) {
        log.info("Process decision: {} of event : {} for user's id: {} hero",
                decision.getId(), decision.getEventTitle(), hero.getUserId());
        DecisionResultResponse decisionResult;
        TryData tryData = attributeCheckService.checkTry(hero, decision);
        if (decision.getDifficulty() > 0) {
            decisionResult = tryData.isResult() ?
                    decision.getResults().get(true) : decision.getResults().get(false);
        } else {
            decisionResult = decision.getResults().get(true);
        }
        return new DecisionResultWsResponse(decision, decisionResult, tryData);
    }
}
