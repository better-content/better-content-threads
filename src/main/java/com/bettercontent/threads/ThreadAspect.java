package com.bettercontent.threads;

import java.util.Arrays;

public enum ThreadAspect {
    IMPACT("impact", 0xE4717D),
    TEMPO("tempo", 0xAA652B),
    WORK("work", 0xCAA903),
    MOBILITY("mobility", 0xC0E304),
    ENDURANCE("endurance", 0x35BBD0),
    ROBUSTNESS("robustness", 0x1175FC),
    RENEWAL("renewal", 0x6FEDBA),
    CONTROL("control", 0x8A6CB2);

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
