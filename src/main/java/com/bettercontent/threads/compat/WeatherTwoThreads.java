package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import weather2.ServerTickHandler;

/** Version-pinned Weather2 exposure followed by sustained shelter under the same active storm episode. */
public final class WeatherTwoThreads {
    private static final String ROOT="BetterContentThreadsStormEpisode";
    private WeatherTwoThreads(){}

    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer player)||player.tickCount%10!=0)return;var manager=ServerTickHandler.getWeatherManagerFor(player.serverLevel());if(manager==null)return;var storm=manager.getClosestStormAny(player.position(),256.0);boolean significant=storm!=null&&storm.levelCurIntensityStage>=1;var persisted=player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);var state=persisted.getCompound(ROOT);String token=state.getString("token");boolean exposed=significant&&player.serverLevel().canSeeSky(player.blockPosition().above());if(exposed){if(token.isBlank()){token=player.getUUID()+":storm:"+player.server.getTickCount();state.putString("token",token);ThreadSignals.emit(player,"storm_exposure","significant",token);}state.putLong("shelteredSince",-1L);}else if(significant&&!token.isBlank()){long since=state.getLong("shelteredSince");if(since<0){state.putLong("shelteredSince",player.server.getTickCount());}else if(player.server.getTickCount()-since>=40){ThreadSignals.emit(player,"storm_shelter","protected",token);persisted.remove(ROOT);player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);return;}}persisted.put(ROOT,state);player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);}
}
