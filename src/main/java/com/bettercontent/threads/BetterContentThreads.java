package com.bettercontent.threads;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(BetterContentThreads.MOD_ID)
public final class BetterContentThreads {
    public static final String MOD_ID = "better_content_threads";

    public BetterContentThreads() {
        ThreadRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ThreadNetwork.register();
        MinecraftForge.EVENT_BUS.register(ThreadEvents.class);
    }
}
