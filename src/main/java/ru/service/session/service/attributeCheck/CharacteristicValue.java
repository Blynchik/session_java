package ru.service.session.service.attributeCheck;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.service.session.dto.event.internal.DecisionType;

@AllArgsConstructor
@Data
public class CharacteristicValue {

    private DecisionType decisionType;
    private Integer value;
}
