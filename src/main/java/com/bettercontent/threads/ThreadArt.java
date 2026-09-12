package com.bettercontent.threads;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** Packaged identity order is also the stable item-model predicate order on clients. */
final class ThreadArt {
 static final Map<String,ThreadDefinition> BY_ID=read();
 static final List<String> IDS=BY_ID.values().stream().sorted(Comparator.comparingInt(ThreadDefinition::order)).map(ThreadDefinition::id).toList();
 private static Map<String,ThreadDefinition> read(){
  try(var stream=ThreadArt.class.getResourceAsStream("/data/better_content_threads/threads/catalogue.json")){
   if(stream==null)return Map.of();
   var out=new LinkedHashMap<String,ThreadDefinition>();
   JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("threads").forEach(e->{var d=ThreadDefinition.parse(e.getAsJsonObject());out.put(d.id(),d);});
   return Collections.unmodifiableMap(out);
  }catch(java.io.IOException e){throw new IllegalStateException("Cannot read packaged card identities",e);}
 }
 static float itemIndex(String id){int index=IDS.indexOf(id);return index<0?0:index+1;}
 private ThreadArt(){}
}
