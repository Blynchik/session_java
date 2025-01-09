package ru.service.session.dto.event.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.service.session.dto.event.internal.DecisionResponse;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private List<DecisionResponse> decisions;
}
