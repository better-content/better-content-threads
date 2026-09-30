package com.bettercontent.betterdiscoveryguides;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(LearningSurfaces.MOD_ID)
public final class LearningSurfaces {
    public static final String MOD_ID = "better_discovery_guides";

    public LearningSurfaces() {
        ThreadRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ThreadNetwork.register();
        MinecraftForge.EVENT_BUS.register(ThreadEvents.class);
        MinecraftForge.EVENT_BUS.register(FirstUseCues.class);
        MinecraftForge.EVENT_BUS.register(OperationOwners.class);
        ProviderHooks.register();
    }
}
