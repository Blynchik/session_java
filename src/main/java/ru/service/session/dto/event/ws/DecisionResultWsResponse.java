package ru.service.session.dto.event.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.service.session.dto.event.internal.DecisionResponse;
import ru.service.session.dto.event.internal.DecisionResultResponse;
import ru.service.session.dto.event.internal.DecisionType;
import ru.service.session.service.attributeCheck.TryData;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DecisionResultWsResponse {

    private DecisionType decisionType;
    private String decisionDescr;
    private Integer difficulty;
    private String eventTitle;
    private String resultDescr;
    private Boolean result;
    private Integer value;

    public DecisionResultWsResponse(DecisionResponse decision, DecisionResultResponse decisionResult, TryData tryData) {
        this.decisionType = decision.getDecisionType();
        this.decisionDescr = decision.getDecisionDescr();
        this.difficulty = decision.getDifficulty();
        this.eventTitle = decision.getEventTitle();
        this.resultDescr = decisionResult.getResultDescr();
        this.result = tryData.isResult();
        this.value = tryData.getValue();
    }
}