package com.bettercontent.threads;

import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;

/** Stable optional-integration surface. Signals describe completed native actions; they never grant rewards. */
public final class ThreadSignals {
    private ThreadSignals(){}
    public static void emit(ServerPlayer player,String type,String value){
        emit(player,type,value,null);
    }
    public static void emit(ServerPlayer player,String type,String value,String correlationToken){
        if(player==null||type==null||value==null||!type.matches("[a-z0-9_]{1,32}")||value.length()>160||(correlationToken!=null&&!ThreadPlayerState.validCorrelation(correlationToken)))return;
        var state=ThreadPlayerState.get(player);var notices=new ArrayList<ThreadNetwork.Notice>();boolean dirty=false;
        for(var definition:ThreadDefinitions.INSTANCE.all()){
            if(definition.future())continue;boolean wasActive=state.active.contains(definition.id());
            var reveal=matching(definition.revealRoutes(),type,value);
            if(!wasActive&&reveal!=null&&correlationToken!=null&&state.reveal(definition.id(),correlationToken)){notices.add(ThreadNetwork.notice(definition,ThreadNetwork.NoticeKind.REVEAL));dirty=true;}
            else if(wasActive&&reveal!=null&&correlationToken!=null&&state.bindCorrelation(definition.id(),correlationToken))dirty=true;
            if(wasActive&&!state.completed.contains(definition.id())){var route=matching(definition.completionRoutes(),type,value);if(route!=null&&correlationToken!=null&&correlationToken.equals(state.correlations.get(definition.id()))&&state.complete(definition.id(),route.label(),state.generation)){notices.add(ThreadNetwork.notice(definition,ThreadNetwork.NoticeKind.COMPLETE));dirty=true;}}
        }
        if(type.equals("condenser")&&value.equals("formed")&&state.active.contains("world_can_be_condensed")&&correlationToken!=null){state.pendingCondenserGeneration=state.generation+1;state.pendingCondenserCorrelation=correlationToken;dirty=true;}
        if(dirty){state.save(player);ThreadNetwork.sync(player,false,notices);}
    }
    public static String activeCorrelation(ServerPlayer player,String threadId){if(player==null||threadId==null||!threadId.matches("[a-z0-9_]{1,48}"))return null;return ThreadPlayerState.get(player).correlations.get(threadId);}
    private static ThreadDefinition.Route matching(java.util.List<ThreadDefinition.Route> routes,String type,String value){return routes.stream().filter(r->r.type().equals(type)&&matches(r.value(),value)).findFirst().orElse(null);}
    private static boolean matches(String expected,String actual){if(expected.equals("*"))return true;for(String candidate:expected.split("\\|"))if(candidate.equals(actual))return true;return false;}
}
