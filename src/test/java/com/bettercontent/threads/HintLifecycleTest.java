package com.bettercontent.threads;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

final class HintLifecycleTest {
    private final Random random=new Random(38);
    private DeathHint tip(String id,String context,String surface){return new DeathHint(id,"test.tip","survival","Inspect your equipment before traveling.",context.isEmpty()?Set.of():Set.of(context),Set.of(),List.of("test"),Set.of(surface),context.isEmpty()?Set.of():Set.of(context));}
    @Test void menuIsStableAcrossWorldsResizesDeathsAndReconnects(){
        var life=new HintLifecycle(DeathHintRotation.State.empty());
        var tips=List.of(tip("menu_one","","menu"),tip("menu_two","","menu"));
        var first=life.menuTip(tips,m->true,random);
        life.death("fall",100);life.respawn();life.disconnect();
        assertSame(first,life.menuTip(tips,m->true,random));
        assertTrue(life.state("menu").shown().isEmpty());
        life.displayed(first,"menu");
        assertEquals(Set.of(first.hint().id()),life.state("menu").shown());
        assertTrue(life.state("pause").shown().isEmpty());
        assertTrue(life.state("death").shown().isEmpty());
    }
    @Test void pauseReconsidersOnlyWhenReopenedWithChangedCondition(){
        var life=new HintLifecycle(DeathHintRotation.State.empty());
        var tips=List.of(tip("locked","door_locked","pause"),tip("healable","door_healable","pause"),tip("general_tip","","pause"));
        life.openPause("door_locked");var first=life.pauseTip(tips,m->true,random);
        assertEquals("locked",first.hint().id());
        assertSame(first,life.pauseTip(tips,m->true,random));
        life.openPause("door_locked");assertSame(first,life.pauseTip(tips,m->true,random));
        life.openPause("door_healable");assertEquals("healable",life.pauseTip(tips,m->true,random).hint().id());
        life.openPause("general");assertEquals("general_tip",life.pauseTip(tips,m->true,random).hint().id());
    }
    @Test void contextualRelevanceWinsOverUnseenUnrelatedAdvice(){
        var related=tip("related","fall","death");var generic=tip("generic","","death");
        var state=new DeathHintRotation.State(Set.of("related"),0,0,"","related","");
        assertEquals(related,DeathHintRotation.select(List.of(related,generic),"fall","death",state,m->true,random,Set.of()).hint());
    }
    @Test void finalDeathIsIdempotentAndFreezesFirstCommittedContext(){
        var life=new HintLifecycle(DeathHintRotation.State.empty());
        var tips=List.of(tip("fall_tip","fall","death"),tip("fire_tip","fire","death"));
        assertTrue(life.death("fall",100));assertFalse(life.death("fire",101));
        var selected=life.deathTip(tips,m->true,random,102);
        assertEquals("fall_tip",selected.hint().id());
        life.displayed(selected,false);life.displayed(selected,false);
        assertEquals(1,life.state("death").shown().size());
    }
    @Test void compactPausePreservesNativeControlSize(){
        var layout=PauseHintLayout.calculate(240,62,202,3,9);assertTrue(layout.fits());assertEquals(26,layout.shift());
    }
}
