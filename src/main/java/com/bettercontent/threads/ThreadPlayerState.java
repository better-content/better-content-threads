package com.bettercontent.threads;

import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.util.*;

public final class ThreadPlayerState {
    private static final String KEY="BetterContentThreads";
    private static final ResourceLocation LINEAGE_KEY=new ResourceLocation("better_content_threads","threads");
    private static final Map<UUID,ThreadPlayerState>CACHE=new HashMap<>();
    public final Set<String> known=new LinkedHashSet<>(),unread=new LinkedHashSet<>(),active=new LinkedHashSet<>(),completed=new LinkedHashSet<>();
    public final Map<String,Integer> completionCounts=new HashMap<>();
    public final Map<String,Long> firstGeneration=new HashMap<>(),lastGeneration=new HashMap<>();
    public final Map<String,String> correlations=new HashMap<>();
    public final Map<String,Map<String,Integer>> routeCounts=new HashMap<>();
    public long generation;
    public long pendingCondenserGeneration=-1L;
    public String pendingCondenserCorrelation;

    public static ThreadPlayerState get(ServerPlayer player){return CACHE.computeIfAbsent(player.getUUID(),ignored->fromTag(readLineage(player)));}
    public static void forget(ServerPlayer player){CACHE.remove(player.getUUID());}
    static ThreadPlayerState fromTag(CompoundTag root){
        var state=new ThreadPlayerState();state.generation=Math.max(0L,root.getLong("generation"));state.pendingCondenserGeneration=root.contains("pendingCondenserGeneration")?root.getLong("pendingCondenserGeneration"):-1L;String pending=root.getString("pendingCondenserCorrelation");state.pendingCondenserCorrelation=validCorrelation(pending)?pending:null;
        readKnown(root,"known",state.known);if(state.known.isEmpty())readKnown(root,"collected",state.known);
        readKnown(root,"unread",state.unread);state.unread.retainAll(state.known);readKnown(root,"active",state.active);readKnown(root,"completed",state.completed);state.active.retainAll(state.known);state.completed.retainAll(state.active);
        readInts(root.getCompound("completionCounts"),state.completionCounts);readLongs(root.getCompound("firstGeneration"),state.firstGeneration);readLongs(root.getCompound("lastGeneration"),state.lastGeneration);
        readCorrelations(root.getCompound("correlations"),state.correlations);
        var routes=root.getCompound("routeCounts");for(String card:routes.getAllKeys()){var values=new LinkedHashMap<String,Integer>();readInts(routes.getCompound(card),values);if(!values.isEmpty())state.routeCounts.put(card,values);}
        return state;
    }
    CompoundTag toTag(){var root=new CompoundTag();root.putInt("schema",3);root.putLong("generation",generation);if(pendingCondenserGeneration>=0)root.putLong("pendingCondenserGeneration",pendingCondenserGeneration);if(validCorrelation(pendingCondenserCorrelation))root.putString("pendingCondenserCorrelation",pendingCondenserCorrelation);root.put("known",strings(known));root.put("unread",strings(unread));root.put("active",strings(active));root.put("completed",strings(completed));root.put("completionCounts",ints(completionCounts));root.put("firstGeneration",longs(firstGeneration));root.put("lastGeneration",longs(lastGeneration));root.put("correlations",strings(correlations));var routes=new CompoundTag();routeCounts.forEach((id,map)->routes.put(id,ints(map)));root.put("routeCounts",routes);return root;}
    public void save(ServerPlayer player){var root=toTag();if(writeLineage(player,root))return;var persisted=player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);persisted.put(KEY,root);player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);}
    public boolean reveal(String id){return reveal(id,null);}
    public boolean reveal(String id,String correlation){var identity=ThreadArt.BY_ID.get(id);if(identity==null||identity.future())return false;boolean firstKnown=known.add(id);boolean activated=active.add(id);if(activated&&correlation!=null)correlations.put(id,correlation);if(firstKnown)unread.add(id);return activated;}
    public boolean bindCorrelation(String id,String correlation){if(!active.contains(id)||completed.contains(id)||correlation==null||correlation.equals(correlations.get(id)))return false;correlations.put(id,correlation);return true;}
    public boolean markRead(String id){return known.contains(id)&&unread.remove(id);}
    public boolean complete(String id,String route,long atGeneration){if(!active.contains(id)||completed.contains(id))return false;completed.add(id);correlations.remove(id);completionCounts.merge(id,1,Integer::sum);firstGeneration.putIfAbsent(id,atGeneration);lastGeneration.put(id,atGeneration);routeCounts.computeIfAbsent(id,ignored->new LinkedHashMap<>()).merge(route,1,Integer::sum);return true;}
    public void enterGeneration(long next){if(next==generation)return;generation=Math.max(0L,next);active.clear();completed.clear();correlations.clear();}
    public String routeSummary(String id){var routes=routeCounts.get(id);if(routes==null||routes.isEmpty())return "";return routes.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e->e.getKey()+" ×"+e.getValue()).collect(java.util.stream.Collectors.joining(" · "));}
    private static void readKnown(CompoundTag root,String key,Collection<String> out){readStrings(root,key).stream().filter(ThreadArt.BY_ID::containsKey).limit(52).forEach(out::add);}
    private static List<String> readStrings(CompoundTag root,String key){return root.getList(key,Tag.TAG_STRING).stream().map(Tag::getAsString).toList();}
    private static void readInts(CompoundTag tag,Map<String,Integer> out){for(String key:tag.getAllKeys())if(key.length()<=64)out.put(key,Math.max(0,tag.getInt(key)));}
    private static void readLongs(CompoundTag tag,Map<String,Long> out){for(String key:tag.getAllKeys())if(key.length()<=48)out.put(key,Math.max(0L,tag.getLong(key)));}
    private static void readCorrelations(CompoundTag tag,Map<String,String> out){for(String key:tag.getAllKeys()){String value=tag.getString(key);if(ThreadArt.BY_ID.containsKey(key)&&validCorrelation(value))out.put(key,value);}}
    private static ListTag strings(Collection<String> values){var list=new ListTag();values.forEach(v->list.add(StringTag.valueOf(v)));return list;}
    private static CompoundTag ints(Map<String,Integer> values){var out=new CompoundTag();values.forEach(out::putInt);return out;}
    private static CompoundTag longs(Map<String,Long> values){var out=new CompoundTag();values.forEach(out::putLong);return out;}
    private static CompoundTag strings(Map<String,String> values){var out=new CompoundTag();values.forEach((key,value)->{if(ThreadArt.BY_ID.containsKey(key)&&validCorrelation(value))out.putString(key,value);});return out;}
    static boolean validCorrelation(String value){return value!=null&&!value.isBlank()&&value.length()<=128&&value.chars().allMatch(c->c>=0x21&&c<=0x7e);}
    private static CompoundTag readLineage(ServerPlayer player){try{var api=Class.forName("com.bettercontent.worldlifecyclemanager.api.LineagePlayerDataApi");return(CompoundTag)api.getMethod("read",net.minecraft.server.MinecraftServer.class,ResourceLocation.class,UUID.class).invoke(null,player.server,LINEAGE_KEY,player.getUUID());}catch(ClassNotFoundException|NoSuchMethodException ignored){return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(KEY);}catch(ReflectiveOperationException failure){throw new IllegalStateException("Could not read lineage Thread state",failure);}}
    private static boolean writeLineage(ServerPlayer player,CompoundTag root){try{var api=Class.forName("com.bettercontent.worldlifecyclemanager.api.LineagePlayerDataApi");api.getMethod("write",net.minecraft.server.MinecraftServer.class,ResourceLocation.class,UUID.class,CompoundTag.class).invoke(null,player.server,LINEAGE_KEY,player.getUUID(),root);return true;}catch(ClassNotFoundException|NoSuchMethodException ignored){return false;}catch(ReflectiveOperationException failure){throw new IllegalStateException("Could not write lineage Thread state",failure);}}
}
