package com.bettercontent.threads;

import java.util.Arrays;

public enum ThreadAspect {
    IMPACT("impact", 0xFF4055),
    TEMPO("tempo", 0x00A985),
    WORK("work", 0xF0E2C5),
    MOBILITY("mobility", 0xE0B01F),
    ENDURANCE("endurance", 0x52606A),
    ROBUSTNESS("robustness", 0xAF6A2F),
    RENEWAL("renewal", 0x6CCAF0),
    CONTROL("control", 0x8E5BB7);

    private final String id;
    private final int color;

    ThreadAspect(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public int color() {
        return color;
    }

    public static ThreadAspect parse(String id) {
        return Arrays.stream(values()).filter(aspect -> aspect.id.equals(id)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("unknown systemic salience aspect " + id));
    }
}
