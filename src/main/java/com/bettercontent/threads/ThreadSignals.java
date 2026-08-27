package com.bettercontent.threads;

import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;

/** Stable optional-integration surface. Signals describe completed native actions; they never grant rewards. */
public final class ThreadSignals {
    private ThreadSignals(){}
    public static void emit(ServerPlayer player,String type,String value){
        if(player==null||type==null||value==null||!type.matches("[a-z0-9_]{1,32}")||value.length()>160)return;
        var state=ThreadPlayerState.get(player);var notices=new ArrayList<ThreadNetwork.Notice>();boolean dirty=false;
        for(var definition:ThreadDefinitions.INSTANCE.all()){
            if(definition.future())continue;boolean wasActive=state.active.contains(definition.id());
            if(!wasActive){var route=matching(definition.revealRoutes(),type,value);if(route!=null&&state.reveal(definition.id())){notices.add(ThreadNetwork.notice(definition,ThreadNetwork.NoticeKind.REVEAL));dirty=true;}}
            if(wasActive&&!state.completed.contains(definition.id())){var route=matching(definition.completionRoutes(),type,value);if(route!=null&&state.complete(definition.id(),route.label(),state.generation)){notices.add(ThreadNetwork.notice(definition,ThreadNetwork.NoticeKind.COMPLETE));dirty=true;}}
        }
        if(type.equals("condenser")&&value.equals("formed")&&state.active.contains("world_can_be_condensed")){state.pendingCondenserGeneration=state.generation+1;dirty=true;}
        if(dirty){state.save(player);ThreadNetwork.sync(player,false,notices);}
    }
    private static ThreadDefinition.Route matching(java.util.List<ThreadDefinition.Route> routes,String type,String value){return routes.stream().filter(r->r.type().equals(type)&&matches(r.value(),value)).findFirst().orElse(null);}
    private static boolean matches(String expected,String actual){if(expected.equals("*"))return true;for(String candidate:expected.split("\\|"))if(candidate.equals(actual))return true;return false;}
}
