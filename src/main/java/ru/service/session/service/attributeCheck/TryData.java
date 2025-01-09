package ru.service.session.service.attributeCheck;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TryData {
    private boolean result;
    private int difficulty;
    private int value;
}
