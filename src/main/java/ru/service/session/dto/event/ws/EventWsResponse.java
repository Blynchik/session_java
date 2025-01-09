package ru.service.session.dto.event.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.service.session.dto.event.internal.EventResponse;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventWsResponse {

    private Long id;
    private String title;
    private String description;
    private List<DecisionWsResponse> decisions;

    public EventWsResponse(EventResponse event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.decisions = event.getDecisions().stream()
                .map(DecisionWsResponse::new)
                .toList();
    }
}
