package com.bettercontent.threads;

import java.util.Arrays;

public enum ThreadSuit {
    WORLD("world", 0x66704C), WORKS("works", 0x956044),
    POWERS("powers", 0x6B6380), FRAGILITY("fragility", 0x805356);

    private final String id;
    private final int color;
    ThreadSuit(String id, int color) { this.id=id; this.color=color; }
    public String id(){return id;} public int color(){return color;}
    public static ThreadSuit parse(String id){return Arrays.stream(values()).filter(s->s.id.equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("unknown thread suit "+id));}
}
