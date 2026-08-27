package com.bettercontent.threads;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

@Mod(BetterContentThreads.MOD_ID)
public final class BetterContentThreads {
    public static final String MOD_ID = "better_content_threads";

    public BetterContentThreads() {
        ThreadRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ThreadNetwork.register();
        MinecraftForge.EVENT_BUS.register(ThreadEvents.class);
        MinecraftForge.EVENT_BUS.register(NativeAdvancementThreads.class);
        MinecraftForge.EVENT_BUS.register(PackActionThreads.class);
        if (ModList.get().isLoaded("dynamictrees")) {
            try {
                MinecraftForge.EVENT_BUS.register(Class.forName("com.bettercontent.threads.compat.DynamicTreeThreads"));
            } catch (ClassNotFoundException failure) {
                throw new IllegalStateException("Dynamic Trees compatibility was not packaged", failure);
            }
        }
        if (ModList.get().isLoaded("sereneseasons")) {
            try {
                MinecraftForge.EVENT_BUS.register(Class.forName("com.bettercontent.threads.compat.SereneSeasonsThreads"));
            } catch (ClassNotFoundException failure) {
                throw new IllegalStateException("Serene Seasons compatibility was not packaged", failure);
            }
        }
        if (ModList.get().isLoaded("weather2")) {
            try {
                MinecraftForge.EVENT_BUS.register(Class.forName("com.bettercontent.threads.compat.WeatherTwoThreads"));
            } catch (ClassNotFoundException failure) {
                throw new IllegalStateException("Weather2 compatibility was not packaged", failure);
            }
        }
        if (ModList.get().isLoaded("pneumaticcraft")) {
            try {
                MinecraftForge.EVENT_BUS.register(Class.forName("com.bettercontent.threads.compat.PneumaticThreads"));
            } catch (ClassNotFoundException failure) {
                throw new IllegalStateException("PneumaticCraft compatibility was not packaged", failure);
            }
        }
        if (ModList.get().isLoaded("creatingspace")) {
            try {
                MinecraftForge.EVENT_BUS.register(Class.forName("com.bettercontent.threads.compat.CreatingSpaceThreads"));
            } catch (ClassNotFoundException failure) {
                throw new IllegalStateException("Creating Space compatibility was not packaged", failure);
            }
        }
        if (ModList.get().isLoaded("ars_nouveau")) {
            try {
                MinecraftForge.EVENT_BUS.register(Class.forName("com.bettercontent.threads.compat.ArsNouveauThreads"));
            } catch (ClassNotFoundException failure) {
                throw new IllegalStateException("Ars Nouveau compatibility was not packaged", failure);
            }
        }
        if (ModList.get().isLoaded("bloodmagic")) {
            try {
                MinecraftForge.EVENT_BUS.register(Class.forName("com.bettercontent.threads.compat.BloodMagicThreads"));
            } catch (ClassNotFoundException failure) {
                throw new IllegalStateException("Blood Magic compatibility was not packaged", failure);
            }
        }
        if (ModList.get().isLoaded("goety")) {
            try {
                MinecraftForge.EVENT_BUS.register(Class.forName("com.bettercontent.threads.compat.GoetyThreads"));
            } catch (ClassNotFoundException failure) {
                throw new IllegalStateException("Goety compatibility was not packaged", failure);
            }
        }
        if (ModList.get().isLoaded("tconstruct")) {
            try {
                MinecraftForge.EVENT_BUS.register(Class.forName("com.bettercontent.threads.compat.TConstructThreads"));
            } catch (ClassNotFoundException failure) {
                throw new IllegalStateException("Tinkers' Construct compatibility was not packaged", failure);
            }
        }
        if (ModList.get().isLoaded("relics")) {
            try {
                MinecraftForge.EVENT_BUS.register(Class.forName("com.bettercontent.threads.compat.RelicsThreads"));
            } catch (ClassNotFoundException failure) {
                throw new IllegalStateException("Relics compatibility was not packaged", failure);
            }
        }
    }
}
