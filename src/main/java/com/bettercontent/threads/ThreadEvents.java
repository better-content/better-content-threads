package com.bettercontent.threads;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public final class ThreadEvents {
    private static final Map<UUID,Boolean> DOWNED=new HashMap<>();
    private static final Map<UUID,String> CAMPAIGN=new HashMap<>();
    private static final Map<UUID,RuinVisit> RUINS=new HashMap<>();
    private static final Map<UUID,HostileCollision> COLLISIONS=new HashMap<>();
    private static final Set<String> MAJOR_STRUCTURE_WORDS=Set.of("ruin","temple","fortress","mansion","monument","mineshaft","stronghold","ancient_city","dungeon","citadel","palace","castle");

    private record RuinVisit(String structure,Set<ResourceLocation> inventoryBefore){}
    private static final class HostileCollision {final LinkedHashSet<UUID> hostiles=new LinkedHashSet<>();long lastTargetTick;}

    @SubscribeEvent public static void reload(AddReloadListenerEvent event){event.addListener(ThreadDefinitions.INSTANCE);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){
        if(!(event.getEntity()instanceof ServerPlayer player))return;
        var state=ThreadPlayerState.get(player);long current=generation(player);var notices=new ArrayList<ThreadNetwork.Notice>();
        boolean pending=state.pendingCondenserGeneration==current&&state.known.contains("world_can_be_condensed");
        if(current!=state.generation)state.enterGeneration(current);
        if(pending){state.active.add("world_can_be_condensed");if(state.complete("world_can_be_condensed","Verified successor",current)){var definition=ThreadDefinitions.INSTANCE.get("world_can_be_condensed");if(definition!=null)notices.add(ThreadNetwork.notice(definition,ThreadNetwork.NoticeKind.COMPLETE));}state.pendingCondenserGeneration=-1L;}
        state.save(player);ThreadNetwork.sync(player,false,notices);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity()instanceof ServerPlayer p){ThreadPlayerState.get(p).save(p);ThreadPlayerState.forget(p);DOWNED.remove(p.getUUID());CAMPAIGN.remove(p.getUUID());RUINS.remove(p.getUUID());COLLISIONS.remove(p.getUUID());}}
    @SubscribeEvent public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event){if(event.getEntity()instanceof ServerPlayer p){ThreadSignals.emit(p,"dimension_enter",event.getTo().location().toString());if(event.getTo()==net.minecraft.world.level.Level.OVERWORLD)ThreadSignals.emit(p,"dimension_return",event.getFrom().location().toString());}}
    @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent event){if(event.getEntity()instanceof ServerPlayer p)ThreadSignals.emit(p,"craft",key(event.getCrafting()));}
    @SubscribeEvent public static void smelted(PlayerEvent.ItemSmeltedEvent event){if(event.getEntity()instanceof ServerPlayer p)ThreadSignals.emit(p,"smelt",key(event.getSmelting()));}
    @SubscribeEvent public static void pickedUp(PlayerEvent.ItemPickupEvent event){if(event.getEntity()instanceof ServerPlayer p)ThreadSignals.emit(p,"pickup",key(event.getStack()));}
    @SubscribeEvent public static void death(LivingDeathEvent event){if(event.getEntity()instanceof ServerPlayer p)ThreadSignals.emit(p,"death","player");}

    @SubscribeEvent public static void targetChanged(LivingChangeTargetEvent event){
        if(!(event.getEntity()instanceof Mob mob)||!(mob instanceof Enemy)||!(event.getNewTarget()instanceof ServerPlayer player))return;
        var collision=COLLISIONS.computeIfAbsent(player.getUUID(),ignored->new HostileCollision());collision.hostiles.add(mob.getUUID());collision.lastTargetTick=player.server.getTickCount();
        while(collision.hostiles.size()>8)collision.hostiles.remove(collision.hostiles.iterator().next());
        if(collision.hostiles.size()>=2)ThreadSignals.emit(player,"hostile_collision","targeted_by_two");
    }
    @SubscribeEvent public static void hurt(LivingHurtEvent event){
        var attacker=event.getSource().getEntity();if(!(attacker instanceof Mob)||!(attacker instanceof Enemy)||!(event.getEntity()instanceof Mob)||!(event.getEntity()instanceof Enemy))return;
        var server=attacker.level().getServer();if(server==null)return;
        for(var player:server.getPlayerList().getPlayers()){var collision=COLLISIONS.get(player.getUUID());if(collision!=null&&player.server.getTickCount()-collision.lastTargetTick<=20*45&&collision.hostiles.contains(attacker.getUUID())&&collision.hostiles.contains(event.getEntity().getUUID()))ThreadSignals.emit(player,"hostile_collision","cross_damage");}
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
        if(event.phase!=TickEvent.Phase.END||event.getServer().getTickCount()%5!=0)return;
        for(var player:event.getServer().getPlayerList().getPlayers()){
            boolean downed=isDowned(player),before=DOWNED.getOrDefault(player.getUUID(),false);DOWNED.put(player.getUUID(),downed);if(downed&&!before)ThreadSignals.emit(player,"downed","player");
            if(event.getServer().getTickCount()%20==0){String campaign=campaign(player),old=CAMPAIGN.put(player.getUUID(),campaign);if(!campaign.equals(old)){ThreadSignals.emit(player,"campaign_state",campaign);if(isTerminal(campaign))ThreadSignals.emit(player,"campaign_outcome",campaign);}updateRuin(player);var collision=COLLISIONS.get(player.getUUID());if(collision!=null&&event.getServer().getTickCount()-collision.lastTargetTick>20*45)COLLISIONS.remove(player.getUUID());}
        }
    }
    private static void updateRuin(ServerPlayer player){
        String structure=currentMajorStructure(player);var visit=RUINS.get(player.getUUID());
        if(structure!=null&&visit==null){RUINS.put(player.getUUID(),new RuinVisit(structure,inventoryTypes(player)));ThreadSignals.emit(player,"structure_enter","major_ruin");return;}
        if(structure==null&&visit!=null){RUINS.remove(player.getUUID());var after=inventoryTypes(player);if(after.stream().anyMatch(id->!visit.inventoryBefore().contains(id)))ThreadSignals.emit(player,"structure_exit","new_item_alive");}
    }
    private static String currentMajorStructure(ServerPlayer player){
        var level=player.serverLevel();var registry=level.registryAccess().registryOrThrow(Registries.STRUCTURE);var pos=player.blockPosition();
        return level.structureManager().startsForStructure(new ChunkPos(pos),structure->{var id=registry.getKey(structure);return id!=null&&MAJOR_STRUCTURE_WORDS.stream().anyMatch(word->id.getPath().contains(word));}).stream().filter(start->start.getBoundingBox().isInside(pos)).map(start->{var id=registry.getKey(start.getStructure());return id==null?"unknown":id.toString();}).findFirst().orElse(null);
    }
    private static Set<ResourceLocation> inventoryTypes(ServerPlayer player){var out=new HashSet<ResourceLocation>();for(ItemStack stack:player.getInventory().items){var id=ForgeRegistries.ITEMS.getKey(stack.getItem());if(!stack.isEmpty()&&id!=null)out.add(id);}return Set.copyOf(out);}
    private static String key(ItemStack stack){var id=ForgeRegistries.ITEMS.getKey(stack.getItem());return id==null?"":id.toString();}
    private static boolean isTerminal(String value){return value.equals("survived")||value.equals("resolved")||value.equals("retreated")||value.equals("target_dead")||value.equals("defeated");}
    private static boolean isDowned(ServerPlayer player){try{var api=Class.forName("com.bettercontent.downedplayerrevival.api.RevivalApi");return(boolean)api.getMethod("isDowned",net.minecraft.world.entity.player.Player.class).invoke(null,player);}catch(ReflectiveOperationException ignored){return false;}}
    private static String campaign(ServerPlayer player){try{var api=Class.forName("com.bettercontent.pillagercampaigns.api.CampaignStatusApi");return api.getMethod("state",ServerPlayer.class).invoke(null,player).toString().toLowerCase(Locale.ROOT);}catch(ReflectiveOperationException ignored){return "none";}}
    private static long generation(ServerPlayer player){try{var api=Class.forName("com.bettercontent.worldlifecyclemanager.PrestigeService");var lineage=api.getMethod("lineage",net.minecraft.server.MinecraftServer.class).invoke(null,player.server);return((Number)lineage.getClass().getMethod("generation").invoke(lineage)).longValue();}catch(ReflectiveOperationException ignored){return 0L;}}
}
