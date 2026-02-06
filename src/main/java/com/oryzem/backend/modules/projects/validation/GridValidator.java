package com.oryzem.backend.modules.projects.validation;

import com.oryzem.backend.modules.projects.domain.Gate;
import com.oryzem.backend.modules.projects.domain.Phase;
import com.oryzem.backend.modules.projects.dto.Grid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GridValidator implements ConstraintValidator<ValidGrid, Grid> {

    private static final Set<Integer> REQUIRED_ALS =
            IntStream.rangeClosed(1, 8).boxed().collect(Collectors.toSet());
    private static final Set<Gate> REQUIRED_GATES =
            EnumSet.of(Gate.ZP5, Gate.ELET, Gate.ZP7);
    private static final Set<Phase> REQUIRED_PHASES =
            EnumSet.of(Phase.VFF, Phase.PVS, Phase.SO, Phase.TPPA, Phase.SOP);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    @Override
    public boolean isValid(Grid grid, ConstraintValidatorContext context) {
        if (grid == null || grid.dates() == null || grid.alsDescriptions() == null) {
            return true;
        }

        Map<Integer, String> alsDescriptions = grid.alsDescriptions();
        if (alsDescriptions.isEmpty()) {
            return false;
        }
        if (!REQUIRED_ALS.containsAll(alsDescriptions.keySet())) {
            return false;
        }
        for (Integer als : REQUIRED_ALS) {
            if (!alsDescriptions.containsKey(als)) {
                continue;
            }
            String description = alsDescriptions.get(als);
            if (description == null || description.isBlank()) {
                return false;
            }
        }

        Map<Integer, Map<Gate, Map<Phase, String>>> dates = grid.dates();
        if (!REQUIRED_ALS.containsAll(dates.keySet())) {
            return false;
        }
        if (!alsDescriptions.keySet().equals(dates.keySet())) {
            return false;
        }

        for (Map<Gate, Map<Phase, String>> gateMap : dates.values()) {
            if (gateMap == null) {
                continue;
            }
            if (!REQUIRED_GATES.containsAll(gateMap.keySet())) {
                return false;
            }
            for (Map<Phase, String> phaseMap : gateMap.values()) {
                if (phaseMap == null) {
                    continue;
                }
                if (!REQUIRED_PHASES.containsAll(phaseMap.keySet())) {
                    return false;
                }
                for (String value : phaseMap.values()) {
                    if (!isValidDateOrBlank(value)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean isValidDateOrBlank(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            LocalDate.parse(value, DATE_FORMAT);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
