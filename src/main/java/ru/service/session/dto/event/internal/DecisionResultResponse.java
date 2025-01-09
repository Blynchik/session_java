package ru.service.session.dto.event.internal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DecisionResultResponse {

    @NotNull
    private String resultDescr;
}
