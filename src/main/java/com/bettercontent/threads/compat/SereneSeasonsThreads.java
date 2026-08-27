package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CropBlock;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.init.ModFertility;

/** Version-pinned season transition and fertile mature-crop evidence. */
public final class SereneSeasonsThreads {
    private static final String ROOT="BetterContentThreadsSeasonEpisode",LAST="BetterContentThreadsLastSubSeason";
    private SereneSeasonsThreads(){}

    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer player)||player.tickCount%20!=0)return;var season=SeasonHelper.getSeasonState(player.level());if(season==null)return;String current=season.getSubSeason().name().toLowerCase(java.util.Locale.ROOT);var persisted=player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);String previous=persisted.getString(LAST);persisted.putString(LAST,current);if(!previous.isBlank()&&!previous.equals(current)){String token=player.getUUID()+":season:"+current+":"+player.server.getTickCount();persisted.putString(ROOT,token);ThreadSignals.emit(player,"season_change",current,token);}player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);}

    @SubscribeEvent public static void harvest(BlockEvent.BreakEvent event){if(!(event.getPlayer()instanceof ServerPlayer player)||!(event.getState().getBlock()instanceof CropBlock crop)||!crop.isMaxAge(event.getState()))return;var id=ForgeRegistries.BLOCKS.getKey(event.getState().getBlock());if(id==null||!ModFertility.isCropFertile(id.toString(),player.level(),event.getPos()))return;var persisted=player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);String token=persisted.getString(ROOT);if(token.isBlank())return;ThreadSignals.emit(player,"seasonal_harvest",id.toString(),token);persisted.remove(ROOT);player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);}
}
