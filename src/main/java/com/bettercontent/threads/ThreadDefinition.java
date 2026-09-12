package com.bettercontent.threads;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import java.util.HashSet;
import java.util.List;

public record ThreadDefinition(String id,String conceptId,String owner,String title,ThreadSuit suit,int order,ThreadAspect aspect,ResourceLocation art,
                               String rule,String action,List<Route> revealRoutes,
                               List<Route> completionRoutes,Doorway doorway){
    public static final int MAX_RULE=240;
    public ThreadDefinition{
        if(!id.matches("[a-z0-9_]{1,48}"))throw new IllegalArgumentException("invalid thread id");bounded(title,64,"title");
        if(!conceptId.matches("[a-z0-9_.]{3,80}"))throw new IllegalArgumentException("invalid thread concept");
        if(!owner.matches("[a-z0-9_.-]{1,80}"))throw new IllegalArgumentException("invalid thread owner");
        if(order<1||order>13)throw new IllegalArgumentException("invalid suit order");var ids=new HashSet<String>();
        java.util.stream.Stream.concat(revealRoutes.stream(),completionRoutes.stream()).forEach(r->{if(!ids.add(r.id()))throw new IllegalArgumentException("duplicate route "+r.id());});
        bounded(rule,MAX_RULE,"rule");if(wordCount(rule)>35)throw new IllegalArgumentException("thread rule must contain at most 35 words");
        bounded(action,192,"action");if(revealRoutes.isEmpty()||completionRoutes.isEmpty())throw new IllegalArgumentException("live thread lacks routes");
    }
    private static void bounded(String value,int max,String field){if(value==null||value.isBlank()||value.length()>max)throw new IllegalArgumentException("invalid thread "+field);}
    public record Route(String id,String label,String type,String value,String producer,String correlation){public Route{if(!id.matches("[a-z0-9_]{1,48}"))throw new IllegalArgumentException("invalid route id");bounded(label,64,"route label");if(!type.matches("[a-z0-9_]{1,32}")||value==null||value.length()>160)throw new IllegalArgumentException("invalid route signal");if(!producer.matches("[a-z0-9_.-]{1,64}"))throw new IllegalArgumentException("invalid route producer");if(!correlation.matches("[a-z0-9_]{1,32}"))throw new IllegalArgumentException("invalid route correlation");}}
    public record Doorway(String type,String target){private static final java.util.Set<String>TYPES=java.util.Set.of("trace_sight","rpg","diet","tconstruct","emi","ponder","font","campaign","guideme","powers","lifecycle");public Doorway{if(!TYPES.contains(type)||target==null||target.isBlank()||target.length()>128)throw new IllegalArgumentException("unknown doorway action "+type);if((type.equals("emi")||type.equals("ponder"))&&ResourceLocation.tryParse(target)==null)throw new IllegalArgumentException("invalid item doorway target "+target);}static boolean validType(String type){return TYPES.contains(type);}}
    public static ThreadDefinition parse(JsonObject json){var doorway=json.has("doorway")?json.getAsJsonObject("doorway"):null;return new ThreadDefinition(json.get("id").getAsString(),json.get("concept_id").getAsString(),json.get("owner").getAsString(),json.get("title").getAsString(),ThreadSuit.parse(json.get("suit").getAsString()),json.get("order").getAsInt(),ThreadAspect.parse(json.get("aspect").getAsString()),new ResourceLocation(json.get("art").getAsString()),json.get("rule").getAsString(),string(json,"action"),routes(json.getAsJsonArray("reveal_routes")),routes(json.getAsJsonArray("completion_routes")),doorway==null?null:new Doorway(doorway.get("type").getAsString(),doorway.get("target").getAsString()));}
    private static String string(JsonObject json,String key){return json.has(key)?json.get(key).getAsString():"";}
    private static int wordCount(String value){return value.trim().split("\\s+").length;}
    private static List<Route> routes(JsonArray array){if(array==null)return List.of();return java.util.stream.StreamSupport.stream(array.spliterator(),false).map(e->e.getAsJsonObject()).map(o->new Route(o.get("id").getAsString(),o.get("label").getAsString(),o.get("type").getAsString(),o.has("value")?o.get("value").getAsString():"",string(o,"producer"),string(o,"correlation"))).toList();}
}
