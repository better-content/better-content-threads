package com.bettercontent.betterdiscoveryguides;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** A personal learning cue: recipe inspection is observable only by the client UI. */
@Mod.EventBusSubscriber(modid = LearningSurfaces.MOD_ID, value = Dist.CLIENT)
public final class EmiInspectionClient {
    private static boolean recipeOpen;
    private static String target = "unknown";

    private EmiInspectionClient() {}

    @SubscribeEvent public static void opening(ScreenEvent.Opening event) {
        boolean recipe = isRecipe(event.getNewScreen());
        if (recipeOpen && !recipe) completed();
        if (recipe) {
            recipeOpen = true;
            target = EmiRecipeClient.target();
        }
    }

    @SubscribeEvent public static void closing(ScreenEvent.Closing event) {
        if (recipeOpen && isRecipe(event.getScreen())) completed();
    }

    private static boolean isRecipe(net.minecraft.client.gui.screens.Screen screen) {
        return screen != null && screen.getClass().getName().equals("dev.emi.emi.screen.RecipeScreen");
    }

    private static void completed() {
        String latest = EmiRecipeClient.target();
        if (!latest.equals("unknown")) target = latest;
        ThreadNetwork.request("emi", target);
        recipeOpen = false;
        target = "unknown";
    }
}
