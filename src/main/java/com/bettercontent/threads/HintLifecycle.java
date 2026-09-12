package com.bettercontent.threads;

import java.util.*;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/** Each surface records only rendered copy; an open screen never rotates beneath its reader. */
final class HintLifecycle {
    private final Map<String,DeathHintRotation.State> history = new HashMap<>();
    private DeathHintSession death = new DeathHintSession();
    private DeathHintRotation.Selection pause,menu;
    private String pauseContext = "";
    private boolean deathProcessed;
    HintLifecycle(DeathHintRotation.State state) { this(state,DeathHintRotation.State.empty(),DeathHintRotation.State.empty()); }
    HintLifecycle(DeathHintRotation.State death,DeathHintRotation.State pause,DeathHintRotation.State menu) {
        history.put("death",death);history.put("pause",pause);history.put("menu",menu);
    }
    DeathHintRotation.State state(){return state("death");}
    DeathHintRotation.State state(String surface){return history.get(surface);}
    boolean death(String context,long now){if(deathProcessed)return false;deathProcessed=true;death.receive(context,now);return true;}
    void respawn(){deathProcessed=false;death=new DeathHintSession();}
    void disconnect(){respawn();pause=null;pauseContext="";}
    void openPause(String context){if(!context.equals(pauseContext)){pause=null;pauseContext=context;}}
    DeathHintRotation.Selection deathTip(List<DeathHint> all,Predicate<String> installed,RandomGenerator random,long now){
        death("general",now);
        if(death.selection()==null)death.select(DeathHintRotation.select(all,death.freeze(now),"death",state("death"),installed,random,Set.of()));
        return death.selection();
    }
    DeathHintRotation.Selection pauseTip(List<DeathHint> all,Predicate<String> installed,RandomGenerator random){
        if(pause==null)pause=DeathHintRotation.select(all,pauseContext,"pause",state("pause"),installed,random,Set.of());return pause;
    }
    DeathHintRotation.Selection menuTip(List<DeathHint> all,Predicate<String> installed,RandomGenerator random){
        if(all.equals(List.of(DeathHints.FALLBACK)))return new DeathHintRotation.Selection(DeathHints.FALLBACK,false,state("menu").cycle());
        if(menu==null)menu=DeathHintRotation.select(all,"general","menu",state("menu"),installed,random,Set.of());return menu;
    }
    void displayed(DeathHintRotation.Selection selected,boolean pause){displayed(selected,pause?"pause":"death");}
    void displayed(DeathHintRotation.Selection selected,String surface){
        if(selected!=null&&!selected.hint().equals(DeathHints.FALLBACK))history.put(surface,DeathHintRotation.displayed(state(surface),selected,surface.equals("pause")));
    }
}
