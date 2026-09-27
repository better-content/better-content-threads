package com.bettercontent.learningsurfaces;

import com.bettercontent.learningsurfaces.compat.ArsNouveauThreads;
import com.bettercontent.learningsurfaces.compat.Ae2Threads;
import com.bettercontent.learningsurfaces.compat.BloodMagicThreads;
import com.bettercontent.learningsurfaces.compat.CreatingSpaceThreads;
import com.bettercontent.learningsurfaces.compat.DynamicTreeThreads;
import com.bettercontent.learningsurfaces.compat.GoetyThreads;
import com.bettercontent.learningsurfaces.compat.PneumaticThreads;
import com.bettercontent.learningsurfaces.compat.PowerGridThreads;
import com.bettercontent.learningsurfaces.compat.RelicsThreads;
import com.bettercontent.learningsurfaces.compat.SereneSeasonsThreads;
import com.bettercontent.learningsurfaces.compat.TConstructThreads;
import com.bettercontent.learningsurfaces.compat.WeatherTwoThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.DimensionDrinkThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.ArcaneChunkLoaderThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.EconomyThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.HeatSyncThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.RpgStatsThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.RealisticOresThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.SettlementRoadThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.WaterSurvivalThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.WorldLifecycleThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.BetterContentFixesThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.PlayerTraceThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.SystemicSalienceThreads;
import com.bettercontent.learningsurfaces.compat.bettercontent.DownedRevivalThreads;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

/** Optional provider adapters; gameplay authority stays with each provider. */
final class ProviderHooks {
    private ProviderHooks() {}

    static void register() {
        if (ModList.get().isLoaded("latent_chemlib")) MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.ChemicalDiscoveryThreads.class);
        if (ModList.get().isLoaded("depth_director")) MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.DepthDirectorThreads.class);
        if (ModList.get().isLoaded("bumblezone_cultivars")) MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.CultivarThreads.class);
        if (ModList.get().isLoaded("tinkers_construct_affixes")) MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.RuinousFluxThreads.class);
        if (ModList.get().isLoaded("world_lifecycle_manager")) MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.SchematicSubstitutionThreads.class);
        if (ModList.get().isLoaded("oc2r_create_bridge")) MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.ComputerOperationThreads.class);
        if (ModList.get().isLoaded("oc2r_wireless_pubsub")) MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.WirelessReceiptThreads.class);
        if (ModList.get().isLoaded("create_transmission_loss")) MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.TransmissionLossThreads.class);
        if (ModList.get().isLoaded("create_train_fuel_scaling")) MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.TrainFuelThreads.class);
        if (ModList.get().isLoaded("rail_beetle")) MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.RailBeetleThreads.class);
        if (ModList.get().isLoaded("pillager_campaigns")) MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.PillagerCampaignThreads.class);
        MinecraftForge.EVENT_BUS.register(PackActionThreads.class);
        if (ModList.get().isLoaded("ae2")) {
            MinecraftForge.EVENT_BUS.register(Ae2Threads.class);
        }
        if (ModList.get().isLoaded("arcane_chunk_loaders")) {
            MinecraftForge.EVENT_BUS.register(ArcaneChunkLoaderThreads.class);
        }
        if (ModList.get().isLoaded("better_content_economy")) {
            MinecraftForge.EVENT_BUS.register(EconomyThreads.class);
        }
        if (ModList.get().isLoaded("heat_sync")) {
            MinecraftForge.EVENT_BUS.register(HeatSyncThreads.class);
        }
        if (ModList.get().isLoaded("settlement_roads")) {
            MinecraftForge.EVENT_BUS.register(SettlementRoadThreads.class);
        }
        if (ModList.get().isLoaded("water_survival")) {
            MinecraftForge.EVENT_BUS.register(WaterSurvivalThreads.class);
        }
        if (ModList.get().isLoaded("world_lifecycle_manager")) {
            MinecraftForge.EVENT_BUS.register(WorldLifecycleThreads.class);
        }
        if (ModList.get().isLoaded("better_content_fixes")) {
            MinecraftForge.EVENT_BUS.register(BetterContentFixesThreads.class);
        }
        if (ModList.get().isLoaded("player_traces")) {
            MinecraftForge.EVENT_BUS.register(PlayerTraceThreads.class);
        }
        if (ModList.get().isLoaded("systemic_salience")) {
            MinecraftForge.EVENT_BUS.register(SystemicSalienceThreads.class);
            MinecraftForge.EVENT_BUS.register(com.bettercontent.learningsurfaces.compat.bettercontent.MetabolicDiscoveryThreads.class);
        }
        if (ModList.get().isLoaded("downed_player_revival")) {
            MinecraftForge.EVENT_BUS.register(DownedRevivalThreads.class);
        }
        if (ModList.get().isLoaded("dimension_drink")) {
            MinecraftForge.EVENT_BUS.register(DimensionDrinkThreads.class);
        }
        if (ModList.get().isLoaded("rpg_stats")) {
            MinecraftForge.EVENT_BUS.register(RpgStatsThreads.class);
        }
        if (ModList.get().isLoaded("realistic_ores")) {
            MinecraftForge.EVENT_BUS.register(RealisticOresThreads.class);
        }
        if (ModList.get().isLoaded("dynamictrees")) {
            MinecraftForge.EVENT_BUS.register(DynamicTreeThreads.class);
        }
        if (ModList.get().isLoaded("sereneseasons")) {
            MinecraftForge.EVENT_BUS.register(SereneSeasonsThreads.class);
        }
        if (ModList.get().isLoaded("weather2")) {
            MinecraftForge.EVENT_BUS.register(WeatherTwoThreads.class);
        }
        if (ModList.get().isLoaded("pneumaticcraft")) {
            MinecraftForge.EVENT_BUS.register(PneumaticThreads.class);
        }
        if (ModList.get().isLoaded("powergrid")) {
            MinecraftForge.EVENT_BUS.register(PowerGridThreads.class);
        }
        if (ModList.get().isLoaded("creatingspace")) {
            MinecraftForge.EVENT_BUS.register(CreatingSpaceThreads.class);
        }
        if (ModList.get().isLoaded("ars_nouveau")) {
            MinecraftForge.EVENT_BUS.register(ArsNouveauThreads.class);
        }
        if (ModList.get().isLoaded("bloodmagic")) {
            MinecraftForge.EVENT_BUS.register(BloodMagicThreads.class);
        }
        if (ModList.get().isLoaded("goety")) {
            MinecraftForge.EVENT_BUS.register(GoetyThreads.class);
        }
        if (ModList.get().isLoaded("tconstruct")) {
            MinecraftForge.EVENT_BUS.register(TConstructThreads.class);
        }
        if (ModList.get().isLoaded("relics")) {
            MinecraftForge.EVENT_BUS.register(RelicsThreads.class);
        }
    }
}
