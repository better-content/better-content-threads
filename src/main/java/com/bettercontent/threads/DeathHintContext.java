package com.bettercontent.threads;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Pure classification and downed-episode memory, kept independently of Thread evidence. */
public final class DeathHintContext {
    static final Set<String> CATEGORIES = Set.of("general", "combat", "projectile", "explosion", "fall",
        "fire", "drowning", "cold", "heat", "thirst", "hunger", "suffocation", "magic", "revival", "door_locked", "door_healable", "injury_cure", "injury_trauma", "injury_uncured", "sugar_crash", "alcohol", "nutrition", "campaign", "campaign_approaching", "campaign_active", "campaign_recovery", "frozen_food", "low_health");
    private final Map<UUID, String> downed = new HashMap<>();

    public static String classify(String type, boolean fire, boolean fall, boolean projectile,
                                  boolean explosion, boolean freezing, boolean attacker) {
        if (type.equals("thirst:dehydrate")) return "thirst";
        if ((type.equals("cold_sweat:cold") || type.equals("cold_sweat:cold_scaling"))) return "cold";
        if ((type.equals("cold_sweat:hot") || type.equals("cold_sweat:hot_scaling"))) return "heat";
        if (type.equals("minecraft:drown")) return "drowning";
        if (type.equals("minecraft:starve")) return "hunger";
        if (type.equals("minecraft:in_wall") || type.equals("minecraft:cramming")) return "suffocation";
        if (type.equals("minecraft:magic") || type.equals("minecraft:indirect_magic") || type.equals("minecraft:wither")) return "magic";
        if (fire) return "fire";
        if (freezing) return "cold";
        if (fall) return "fall";
        if (explosion) return "explosion";
        if (projectile) return "projectile";
        return attacker ? "combat" : "general";
    }

    public void downed(UUID player, String context) { downed.put(player, context); }
    public void clear(UUID player) { downed.remove(player); }
    public void clear() { downed.clear(); }
    public String death(UUID player, String terminal) {
        downed.remove(player);
        return terminal;
    }
}
