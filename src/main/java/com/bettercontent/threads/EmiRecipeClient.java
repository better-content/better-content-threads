package com.bettercontent.threads;

import dev.emi.emi.screen.RecipeScreen;

/** Typed EMI access, loaded only after an EMI RecipeScreen is present. */
final class EmiRecipeClient {
    private EmiRecipeClient() {}

    static String target() {
        var ingredient = RecipeScreen.resolve;
        if (ingredient == null || ingredient.getEmiStacks().isEmpty()) return "unknown";
        var id = ingredient.getEmiStacks().get(0).getId();
        if (id == null) return "unknown";
        String value = id.toString();
        return value.length() <= 48 ? value : "unknown";
    }
}
