package com.bettercontent.threads;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraftforge.fml.ModList;

final class ThreadDoorways {
    private ThreadDoorways() {}
    static boolean available(ThreadNetwork.Card card) {
        if (!card.known()) return false;
        return switch (card.doorwayType()) {
            case "body" -> ModList.get().isLoaded("downed_player_revival");
            case "emi" -> ModList.get().isLoaded("emi") && !targetStack(card.doorwayTarget()).isEmpty();
            case "ponder" -> ModList.get().isLoaded("ponder") && !targetStack(card.doorwayTarget()).isEmpty();
            default -> nativeKey(card.doorwayType()) != null;
        };
    }

    static Component label(ThreadNetwork.Card card) {
        return Component.literal(switch (card.doorwayType()) {
            case "body" -> "Open Body";
            case "emi" -> "View recipes";
            case "ponder" -> "Ponder apparatus";
            case "diet" -> "Open nutrition";
            case "rpg" -> "Open life stats";
            case "trace_sight" -> "Use Trace Sight";
            default -> "Look closer";
        });
    }

    private static KeyMapping nativeKey(String type) {
        String name = switch (type) {
            case "diet" -> "key.diet.open.desc";
            case "rpg" -> "key.rpg_stats.open_stats";
            case "trace_sight" -> "key.traces.reveal";
            default -> "";
        };
        if (name.isEmpty()) return null;
        for (KeyMapping key : Minecraft.getInstance().options.keyMappings)
            if (key.getName().equals(name) && !key.isUnbound()) return key;
        return null;
    }

    static void open(ThreadNetwork.Card card) {
        if (!available(card)) return;
        var mc = Minecraft.getInstance();
        if (card.doorwayType().equals("body")) { BodyDoorway.open(); return; }
        if (card.doorwayType().equals("emi")) { EmiDoorway.open(card.doorwayTarget()); return; }
        if (card.doorwayType().equals("ponder")) { PonderDoorway.open(mc, card.doorwayTarget()); return; }
        var key = nativeKey(card.doorwayType());
        if (key != null) { mc.setScreen(null); KeyMapping.click(key.getKey()); }
    }

    // Keep optional API types out of the outer class verifier when a mod is absent.
    private static final class BodyDoorway {
        static void open() { com.bettercontent.downedplayerrevival.client.ClientRevivalInput.openOwnBody(); }
    }
    private static final class EmiDoorway {
        static void open(String target) {
            EmiApi.displayRecipes(EmiStack.of(targetStack(target)));
        }
    }

    private static final class PonderDoorway {
        static void open(Minecraft minecraft, String target) {
            minecraft.setScreen(PonderUI.of(targetStack(target)));
        }
    }

    private static ItemStack targetStack(String target) {
        ResourceLocation id = ResourceLocation.tryParse(target);
        if (id == null) return ItemStack.EMPTY;
        var item = BuiltInRegistries.ITEM.get(id);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }
}
