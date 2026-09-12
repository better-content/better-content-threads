package com.bettercontent.threads;
import java.util.Arrays;
/** Reader topics have no fixed cardinality or gameplay meaning. */
public enum ThreadTopic {
 WORLD("world",0x66704C), BODY("body",0x805356), MATERIALS("materials",0x956044), INDUSTRY("industry",0x887349), MAGIC("magic",0x6B6380), TRAVEL("travel",0x507C89), LINEAGE("lineage",0xAA8751);
 private final String id; private final int color;
 ThreadTopic(String id,int color){this.id=id;this.color=color;}
 public String id(){return id;} public int color(){return color;}
 public static ThreadTopic parse(String id){return Arrays.stream(values()).filter(t->t.id.equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("unknown thread topic "+id));}
}
