package com.bettercontent.threads;

import net.minecraft.resources.ResourceLocation;

final class ThreadPacketValidation {
    private ThreadPacketValidation() {}

    static void id(String value) {
        if (value == null || !value.matches("[a-z0-9_]{1,48}")) throw new IllegalArgumentException("invalid thread id");
        if (!ThreadArt.IDS.contains(value)) throw new IllegalArgumentException("unknown thread id");
    }

    static void title(String value) {
        text(value, 64, "title");
    }

    static void text(String value, int max, String field) {
        if (value == null || value.isBlank() || value.length() > max) throw new IllegalArgumentException("invalid thread " + field);
    }

    static void resource(String value, String field) {
        if (value == null || value.length() > 128 || ResourceLocation.tryParse(value) == null)
            throw new IllegalArgumentException("invalid thread " + field);
    }

}
