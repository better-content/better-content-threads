package com.bettercontent.threads;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public final class ThreadEvents {
    private static final Map<UUID,Boolean> DOWNED=new HashMap<>();
    private static final Map<UUID,CampaignEpisode> CAMPAIGN=new HashMap<>();
    private static final Map<UUID,RuinVisit> RUINS=new HashMap<>();
    private static final Map<UUID,HostileCollision> COLLISIONS=new HashMap<>();
    private static final Set<String> MAJOR_STRUCTURE_WORDS=Set.of("ruin","temple","fortress","mansion","monument","mineshaft","stronghold","ancient_city","dungeon","citadel","palace","castle");
    private static final Set<String> MAJOR_PORTALS=Set.of("minecraft:nether_portal","minecraft:end_portal","minecraft:end_gateway","aether:aether_portal");
    private static final String JOURNEY="BetterContentThreadsJourneyEpisode",HAZARD="BetterContentThreadsHazardEpisode",ENCHANT_TOKEN="BetterContentThreadsEnchantEpisode";

    private record CampaignEpisode(String state,String token){}
    private static final class RuinVisit {final String structure,token;boolean acquired;RuinVisit(String structure,String token){this.structure=structure;this.token=token;}}
    private static final class HostileCollision {final LinkedHashSet<UUID> hostiles=new LinkedHashSet<>();long lastTargetTick;String token;}

    @SubscribeEvent public static void reload(AddReloadListenerEvent event){event.addListener(ThreadDefinitions.INSTANCE);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){
        if(!(event.getEntity()instanceof ServerPlayer player))return;
        var state=ThreadPlayerState.get(player);long current=generation(player);var notices=new ArrayList<ThreadNetwork.Notice>();
        boolean pending=state.pendingCondenserGeneration==current&&state.known.contains("world_can_be_condensed")&&ThreadPlayerState.validCorrelation(state.pendingCondenserCorrelation);String pendingToken=state.pendingCondenserCorrelation;
        if(current!=state.generation)state.enterGeneration(current);
        if(pending){state.active.add("world_can_be_condensed");state.correlations.put("world_can_be_condensed",pendingToken);if(state.complete("world_can_be_condensed","Verified successor",current)){var definition=ThreadDefinitions.INSTANCE.get("world_can_be_condensed");if(definition!=null)notices.add(ThreadNetwork.notice(definition,ThreadNetwork.NoticeKind.COMPLETE));}state.pendingCondenserGeneration=-1L;state.pendingCondenserCorrelation=null;}
        state.save(player);ThreadNetwork.sync(player,false,notices);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity()instanceof ServerPlayer p){ThreadPlayerState.get(p).save(p);ThreadPlayerState.forget(p);DOWNED.remove(p.getUUID());CAMPAIGN.remove(p.getUUID());RUINS.remove(p.getUUID());COLLISIONS.remove(p.getUUID());}}
    @SubscribeEvent public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event){if(event.getEntity()instanceof ServerPlayer p){String destination=event.getTo().location().toString(),source=event.getFrom().location().toString();if(destination.equals("creatingspace:earth_orbit")){String rocket=ThreadSignals.activeCorrelation(p,"leave_atmosphere");if(rocket!=null)ThreadSignals.emit(p,"orbit_reached",destination,rocket);}if(event.getTo()==net.minecraft.world.level.Level.OVERWORLD){String token=ThreadSignals.activeCorrelation(p,dimensionCard(source));if(token!=null)ThreadSignals.emit(p,"dimension_return",source,token);}else{ThreadSignals.emit(p,"dimension_enter",destination,episode(p,"dimension:"+destination));var journey=episodeTag(p,JOURNEY);String token=journey.getString("token");long fedAt=journey.getLong("fedAt");if(ThreadPlayerState.validCorrelation(token)&&fedAt>=0&&p.server.getTickCount()-fedAt<=20*120)ThreadSignals.emit(p,"fed_dimension_enter",destination,token);clearEpisode(p,JOURNEY);}}}
    @SubscribeEvent public static void pickedUp(PlayerEvent.ItemPickupEvent event){if(event.getEntity()instanceof ServerPlayer player){var visit=RUINS.get(player.getUUID());if(visit!=null&&!event.getStack().isEmpty())visit.acquired=true;}}
    @SubscribeEvent public static void enchantedAtAnvil(AnvilRepairEvent event){if(!(event.getEntity()instanceof ServerPlayer player))return;var before=EnchantmentHelper.getEnchantments(event.getLeft());var after=EnchantmentHelper.getEnchantments(event.getOutput());boolean added=after.entrySet().stream().anyMatch(entry->entry.getValue()>before.getOrDefault(entry.getKey(),0));if(!added)return;String token=episode(player,"enchant");event.getOutput().getOrCreateTag().putString(ENCHANT_TOKEN,token);ThreadSignals.emit(player,"enchant_apply","supported",token);}
    @SubscribeEvent public static void brokeBlock(BlockEvent.BreakEvent event){if(!(event.getPlayer()instanceof ServerPlayer player))return;ItemStack tool=player.getMainHandItem();if(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY,tool)>0&&player.hasCorrectToolForDrops(event.getState()))completeEnchant(player,tool);}
    @SubscribeEvent public static void death(LivingDeathEvent event){if(event.getEntity()instanceof ServerPlayer p){String token=ThreadSignals.activeCorrelation(p,"life_reaches_tether");if(token!=null)ThreadSignals.emit(p,"death","player",token);}}

    @SubscribeEvent public static void targetChanged(LivingChangeTargetEvent event){
        if(!(event.getEntity()instanceof Mob mob)||!(mob instanceof Enemy)||!(event.getNewTarget()instanceof ServerPlayer player))return;
        var collision=COLLISIONS.computeIfAbsent(player.getUUID(),ignored->new HostileCollision());collision.hostiles.add(mob.getUUID());collision.lastTargetTick=player.server.getTickCount();
        while(collision.hostiles.size()>8)collision.hostiles.remove(collision.hostiles.iterator().next());
        if(collision.hostiles.size()>=2){if(collision.token==null)collision.token=episode(player,"hostiles");ThreadSignals.emit(player,"hostile_collision","targeted_by_two",collision.token);}
    }
    @SubscribeEvent public static void hurt(LivingHurtEvent event){
        if(event.getEntity()instanceof ServerPlayer player){String hazard=hazard(event.getSource());if(hazard!=null){var current=episodeTag(player,HAZARD);String token=current.getString("token");if(!ThreadPlayerState.validCorrelation(token)){token=episode(player,"hazard:"+hazard);current.putString("token",token);current.putString("hazard",hazard);saveEpisode(player,HAZARD,current);ThreadSignals.emit(player,"effect_hazard",hazard,token);}}}
        if(event.getSource().getEntity()instanceof ServerPlayer attacker){ItemStack weapon=attacker.getMainHandItem();if(EnchantmentHelper.getDamageBonus(weapon,event.getEntity().getMobType())>0.0f)completeEnchant(attacker,weapon);}
        var attacker=event.getSource().getEntity();if(!(attacker instanceof Mob)||!(attacker instanceof Enemy)||!(event.getEntity()instanceof Mob)||!(event.getEntity()instanceof Enemy))return;
        var server=attacker.level().getServer();if(server==null)return;
        for(var player:server.getPlayerList().getPlayers()){var collision=COLLISIONS.get(player.getUUID());if(collision!=null&&collision.token!=null&&player.server.getTickCount()-collision.lastTargetTick<=20*45&&collision.hostiles.contains(attacker.getUUID())&&collision.hostiles.contains(event.getEntity().getUUID()))ThreadSignals.emit(player,"hostile_collision","cross_damage",collision.token);}
    }
    @SubscribeEvent public static void finishedUsing(LivingEntityUseItemEvent.Finish event){if(!(event.getEntity()instanceof ServerPlayer player))return;ItemStack stack=event.getItem();if(stack.isEdible()&&stack.getFoodProperties(player)!=null&&stack.getFoodProperties(player).getNutrition()>=6){var journey=episodeTag(player,JOURNEY);String token=journey.getString("token");if(ThreadPlayerState.validCorrelation(token)){journey.putLong("fedAt",player.server.getTickCount());saveEpisode(player,JOURNEY,journey);}}}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
        if(event.phase!=TickEvent.Phase.END||event.getServer().getTickCount()%5!=0)return;
        for(var player:event.getServer().getPlayerList().getPlayers()){
            if(event.getServer().getTickCount()%10==0){updateJourney(player);updateHazard(player);}
            boolean downed=isDowned(player),before=DOWNED.getOrDefault(player.getUUID(),false);DOWNED.put(player.getUUID(),downed);if(downed&&!before)ThreadSignals.emit(player,"downed","player",episode(player,"downed"));
            if(event.getServer().getTickCount()%20==0){updateCampaign(player);updateRuin(player);var collision=COLLISIONS.get(player.getUUID());if(collision!=null&&event.getServer().getTickCount()-collision.lastTargetTick>20*45)COLLISIONS.remove(player.getUUID());}
        }
    }
    private static void updateJourney(ServerPlayer player){if(!nearMajorPortal(player))return;var existing=episodeTag(player,JOURNEY);if(ThreadPlayerState.validCorrelation(existing.getString("token")))return;String token=episode(player,"journey");existing.putString("token",token);existing.putLong("fedAt",-1L);saveEpisode(player,JOURNEY,existing);ThreadSignals.emit(player,"portal_approach","major_realm",token);}
    private static boolean nearMajorPortal(ServerPlayer player){var origin=player.blockPosition();for(var pos:net.minecraft.core.BlockPos.betweenClosed(origin.offset(-3,-2,-3),origin.offset(3,2,3))){var id=ForgeRegistries.BLOCKS.getKey(player.level().getBlockState(pos).getBlock());if(id!=null&&MAJOR_PORTALS.contains(id.toString()))return true;}return false;}
    private static void updateHazard(ServerPlayer player){var state=episodeTag(player,HAZARD);String token=state.getString("token"),hazard=state.getString("hazard");if(!ThreadPlayerState.validCorrelation(token))return;boolean protectedNow=switch(hazard){case "fire"->player.hasEffect(MobEffects.FIRE_RESISTANCE);case "water"->player.hasEffect(MobEffects.WATER_BREATHING);case "temperature"->player.hasEffect(MobEffects.FIRE_RESISTANCE)||player.hasEffect(MobEffects.DAMAGE_RESISTANCE);default->false;};if(protectedNow){ThreadSignals.emit(player,"effect_resolution","correlated",token);clearEpisode(player,HAZARD);}}
    private static void updateRuin(ServerPlayer player){
        String structure=currentMajorStructure(player);var visit=RUINS.get(player.getUUID());
        if(structure!=null&&visit==null){String token=episode(player,"ruin:"+structure);RUINS.put(player.getUUID(),new RuinVisit(structure,token));ThreadSignals.emit(player,"structure_enter","major_ruin",token);return;}
        if(structure==null&&visit!=null){RUINS.remove(player.getUUID());if(visit.acquired&&player.isAlive())ThreadSignals.emit(player,"structure_exit","new_item_alive",visit.token);}
    }
    private static void updateCampaign(ServerPlayer player){String current=campaign(player);var previous=CAMPAIGN.get(player.getUUID());if(previous!=null&&current.equals(previous.state()))return;String active=ThreadSignals.activeCorrelation(player,"army_walks_toward_you");String token=active!=null?active:previous==null||previous.token()==null?episode(player,"campaign"):previous.token();if(current.equals("none")||current.equals("quiet")){CAMPAIGN.remove(player.getUUID());return;}CAMPAIGN.put(player.getUUID(),new CampaignEpisode(current,token));if(isTerminal(current))ThreadSignals.emit(player,"campaign_outcome",current,token);else ThreadSignals.emit(player,"campaign_state",current,token);}
    private static String currentMajorStructure(ServerPlayer player){
        var level=player.serverLevel();var registry=level.registryAccess().registryOrThrow(Registries.STRUCTURE);var pos=player.blockPosition();
        return level.structureManager().startsForStructure(new ChunkPos(pos),structure->{var id=registry.getKey(structure);return id!=null&&MAJOR_STRUCTURE_WORDS.stream().anyMatch(word->id.getPath().contains(word));}).stream().filter(start->start.getBoundingBox().isInside(pos)).map(start->{var id=registry.getKey(start.getStructure());return id==null?"unknown":id.toString();}).findFirst().orElse(null);
    }
    private static String hazard(net.minecraft.world.damagesource.DamageSource source){String id=source.getMsgId();if(id.equals("inFire")||id.equals("onFire")||id.equals("lava")||id.equals("hotFloor"))return "fire";if(id.equals("drown"))return "water";if(id.equals("freeze"))return "temperature";return null;}
    private static void completeEnchant(ServerPlayer player,ItemStack stack){String token=stack.hasTag()?stack.getTag().getString(ENCHANT_TOKEN):"";if(!ThreadPlayerState.validCorrelation(token)||!token.equals(ThreadSignals.activeCorrelation(player,"rules_adhere_matter")))return;ThreadSignals.emit(player,"enchant_effect","correlated",token);stack.getTag().remove(ENCHANT_TOKEN);}
    private static net.minecraft.nbt.CompoundTag episodeTag(ServerPlayer player,String key){return player.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).getCompound(key);}
    private static void saveEpisode(ServerPlayer player,String key,net.minecraft.nbt.CompoundTag value){var persisted=player.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);persisted.put(key,value);player.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG,persisted);}
    private static void clearEpisode(ServerPlayer player,String key){var persisted=player.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);persisted.remove(key);player.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG,persisted);}
    private static String episode(ServerPlayer player,String kind){return player.getUUID()+":"+Integer.toUnsignedString(kind.hashCode(),36)+":"+player.server.getTickCount();}
    private static String dimensionCard(String dimension){return switch(dimension){case "minecraft:the_nether"->"fire_has_country";case "aether:the_aether"->"sky_another_country";case "the_bumblezone:the_bumblezone"->"deep_own_light";case "rats:ratlantis"->"silence_has_teeth";default->"";};}
    private static boolean isTerminal(String value){return value.equals("survived")||value.equals("resolved")||value.equals("retreated")||value.equals("target_dead")||value.equals("defeated");}
    private static boolean isDowned(ServerPlayer player){try{var api=Class.forName("com.bettercontent.downedplayerrevival.api.RevivalApi");return(boolean)api.getMethod("isDowned",net.minecraft.world.entity.player.Player.class).invoke(null,player);}catch(ReflectiveOperationException ignored){return false;}}
    private static String campaign(ServerPlayer player){try{var api=Class.forName("com.bettercontent.pillagercampaigns.api.CampaignStatusApi");return api.getMethod("state",ServerPlayer.class).invoke(null,player).toString().toLowerCase(Locale.ROOT);}catch(ReflectiveOperationException ignored){return "none";}}
    private static long generation(ServerPlayer player){try{var api=Class.forName("com.bettercontent.worldlifecyclemanager.PrestigeService");var lineage=api.getMethod("lineage",net.minecraft.server.MinecraftServer.class).invoke(null,player.server);return((Number)lineage.getClass().getMethod("generation").invoke(lineage)).longValue();}catch(ReflectiveOperationException ignored){return 0L;}}
}
