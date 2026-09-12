package com.bettercontent.threads;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

final class DeathHintsTest {
    private List<DeathHint> catalogue() throws Exception{return DeathHints.parse(JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/better_content_threads/death_hints/catalogue.json"))).getAsJsonObject());}
    @Test void allTipsHaveExplicitSurfaceAndAuthoritativeRequirements() throws Exception {
        var all=catalogue();assertFalse(all.isEmpty());
        assertEquals(all.size(),all.stream().map(DeathHint::id).distinct().count());
        for(var hint:all){assertFalse(hint.sources().isEmpty());assertFalse(hint.surfaces().isEmpty());
            if(hint.pool().equals("teaser"))assertEquals(Set.of("menu"),hint.surfaces());
            assertFalse(hint.text().toLowerCase().contains("bleed out"));assertFalse(hint.text().toLowerCase().contains("revive a downed"));}
    }
    @Test void unavailableModsAndMissingEvidenceNeverInventADiagnosis() throws Exception {
        var locked=catalogue().stream().filter(h->h.id().equals("door_healing_locked")).findFirst().orElseThrow();
        assertEquals(DeathHints.FALLBACK,DeathHintRotation.select(List.of(locked),"general","pause",DeathHintRotation.State.empty(),m->true,new Random(),Set.of()).hint());
        assertEquals(DeathHints.FALLBACK,DeathHintRotation.select(List.of(locked),"door_locked","pause",DeathHintRotation.State.empty(),m->false,new Random(),Set.of()).hint());
    }
    @Test void terminalDamageNeverUsesTheOriginalDoorHit(){
        var context=new DeathHintContext();var player=UUID.randomUUID();context.downed(player,"fire");
        assertEquals("fall",context.death(player,"fall"));
        assertEquals("general",DeathHintContext.classify("downed_player_revival:bled_out",false,false,false,false,false,false));
    }
    @Test void savedSurfaceHistoryRoundTripsAtomically(@TempDir Path directory) throws Exception {
        var state=new DeathHintRotation.State(Set.of("some_tip"),0,1,"","some_tip","");
        var file=directory.resolve("history.json");DeathHintStore.write(file,state);
        assertEquals(state,DeathHintStore.decode(JsonParser.parseString(Files.readString(file)).getAsJsonObject()));
        try(var files=Files.list(directory)){assertEquals(1,files.count());}
    }
    @Test void malformedBindingsAreRejected(){
        assertThrows(IllegalArgumentException.class,()->new DeathHint("bad","test.tip","survival","Press {unknown}.",Set.of(),Set.of(),List.of("source")));
    }
    @Test void staleDeathContextsFallBack(){var session=new DeathHintSession();session.receive("fire",100);assertEquals("general",session.freeze(6000));}
}
