package ru.service.session.service.attributeCheck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.service.session.dto.event.internal.DecisionResponse;
import ru.service.session.dto.event.internal.DecisionType;
import ru.service.session.dto.hero.HeroResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static ru.service.session.dto.event.internal.DecisionType.*;

@Service
@Slf4j
public class AttributeCheckService {

    private final Random random;

    @Autowired
    public AttributeCheckService() {
        this.random = new Random();
    }

    public int rollAttribute(HeroResponse hero, DecisionType decisionType) {
        int result = switch (decisionType) {
            case STR -> random.nextInt(0, hero.getStr() + 1);
            case DEX -> random.nextInt(0, hero.getDex() + 1);
            case CON -> random.nextInt(0, hero.getCon() + 1);
            case INT -> random.nextInt(0, hero.getIntl() + 1);
            case WIS -> random.nextInt(0, hero.getWis() + 1);
            case CHA -> random.nextInt(0, hero.getCha() + 1);
            default -> 0;
        };
        log.info("Try {} on {}", decisionType.name(), result);
        return result;
    }

    public List<CharacteristicValue> getHeroRatedCharacteristicsMaxMin(HeroResponse hero) {
        log.info("Sorting characteristics for user's id: {} hero descending order", hero.getUserId());
        return Stream.of(
                        new CharacteristicValue(STR, hero.getStr()),
                        new CharacteristicValue(DEX, hero.getDex()),
                        new CharacteristicValue(CON, hero.getCon()),
                        new CharacteristicValue(INT, hero.getIntl()),
                        new CharacteristicValue(WIS, hero.getWis()),
                        new CharacteristicValue(CHA, hero.getCha()))
                .sorted(Comparator.comparingInt(CharacteristicValue::getValue).reversed())
                .toList();
    }

    public TryData checkTry(HeroResponse hero, DecisionResponse decision) {
        int value = rollAttribute(hero, decision.getDecisionType());
        int difficulty = decision.getDifficulty();
        boolean result = difficulty <= 0 || value > difficulty;
        log.info("Decision of event: {} with difficulty {} {} and try: {} result: {}", decision.getEventTitle(),
                decision.getDecisionType(), difficulty,
                value, result);
        return new TryData(result, difficulty, value);
    }
}
