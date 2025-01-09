package ru.service.session.dto.event.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.service.session.dto.event.internal.DecisionResponse;
import ru.service.session.dto.event.internal.DecisionType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecisionWsResponse {

    private Long id;
    private DecisionType decisionType;
    private String description;
    private String decisionLog;
    private Integer difficulty;
    private String eventTitle;

    public DecisionWsResponse(DecisionResponse decision) {
        this.id = decision.getId();
        this.decisionType = decision.getDecisionType();
        this.description = decision.getDecisionDescr();
        this.decisionLog = decision.getDecisionLog();
        this.eventTitle = decision.getEventTitle();
        this.difficulty = decision.getDifficulty();
    }
}
