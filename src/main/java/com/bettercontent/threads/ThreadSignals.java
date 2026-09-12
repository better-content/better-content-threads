package com.bettercontent.threads;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
/** Authoritative outcome ingress. A route matches one completed native effect, never setup. */
public final class ThreadSignals {
 private ThreadSignals(){}
 public static void emit(ServerPlayer player,String type,String value){emit(player,type,value,null,"");}
 public static void emit(ServerPlayer player,String type,String value,String correlation){emit(player,type,value,correlation,"");}
 public static void emit(ServerPlayer player,String type,String value,String correlation,String context){if(player!=null)emit(player.server,player.getUUID(),type,value,correlation,context);}
 public static void emit(MinecraftServer server,UUID owner,String type,String value,String correlation,String context){
  if(server==null||owner==null||type==null||!type.matches("[a-z0-9_]{1,32}")||value==null||value.length()>160||!ThreadPlayerState.validCorrelation(correlation) )return;
  if(!server.isSameThread()){server.execute(()->emit(server,owner,type,value,correlation,context));return;}
  String detail=context==null?"":context.substring(0,Math.min(256,context.length()));
  var state=ThreadPlayerState.get(server,owner);state.enterGeneration(ThreadPlayerState.currentGeneration(server));boolean changed=false;
  for(var d:ThreadDefinitions.INSTANCE.all())for(var route:d.discoveryRoutes())if(route.type().equals(type)&&matches(route.value(),value)){
   changed|=state.discover(d.id(),route.id(),correlation,detail,state.generation);break;
  }
  if(!changed)return;state.save(server,owner);var player=server.getPlayerList().getPlayer(owner);if(player!=null)deliver(player);
 }
 public static void login(ServerPlayer player){var state=ThreadPlayerState.get(player);state.enterGeneration(ThreadPlayerState.currentGeneration(player.server));state.save(player);deliver(player);}
 public static void deliver(ServerPlayer player){var state=ThreadPlayerState.get(player);var notices=new ArrayList<ThreadNetwork.Notice>();for(String id:state.pendingNotices){var d=ThreadDefinitions.INSTANCE.get(id);if(d!=null)notices.add(ThreadNetwork.notice(d,state.generationCounts.getOrDefault(id,0)>1?ThreadNetwork.NoticeKind.REMINDER:ThreadNetwork.NoticeKind.DISCOVERY,state.contexts.getOrDefault(id,""),state.lastGeneration.getOrDefault(id,state.generation)));}ThreadNetwork.sync(player,false,notices);state.pendingNotices.clear();state.save(player);}
 private static boolean matches(String expected,String actual){if(expected.equals("*"))return true;return Arrays.asList(expected.split("\\|",-1)).contains(actual);}
}
