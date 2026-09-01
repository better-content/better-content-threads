package com.bettercontent.threads;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ThreadArt {
    record Entry(String id,ThreadSuit suit,int order,ThreadAspect aspect,boolean future){}
    static final List<Entry> ENTRIES=List.of(
        e("stone_makes_promises",ThreadSuit.WORLD,1,ThreadAspect.WORK),e("world_remembers",ThreadSuit.WORLD,2,ThreadAspect.CONTROL),e("seasons_turn_work",ThreadSuit.WORLD,3,ThreadAspect.TEMPO),e("forest_many_lives",ThreadSuit.WORLD,4,ThreadAspect.RENEWAL),e("roads_make_neighbours",ThreadSuit.WORLD,5,ThreadAspect.MOBILITY),e("fire_has_country",ThreadSuit.WORLD,6,ThreadAspect.IMPACT),e("sky_another_country",ThreadSuit.WORLD,7,ThreadAspect.MOBILITY),e("deep_own_light",ThreadSuit.WORLD,8,ThreadAspect.ROBUSTNESS),e("silence_has_teeth",ThreadSuit.WORLD,9,ThreadAspect.ENDURANCE),e("weather_has_momentum",ThreadSuit.WORLD,10,ThreadAspect.TEMPO),e("body_has_weather",ThreadSuit.WORLD,11,ThreadAspect.ROBUSTNESS),e("water_made_safe",ThreadSuit.WORLD,12,ThreadAspect.CONTROL),e("feast_before_journey",ThreadSuit.WORLD,13,ThreadAspect.ENDURANCE),
        e("hands_learn_repair",ThreadSuit.WORKS,1,ThreadAspect.RENEWAL),e("materials_temperaments",ThreadSuit.WORKS,2,ThreadAspect.ROBUSTNESS),e("motion_becomes_industry",ThreadSuit.WORKS,3,ThreadAspect.WORK),e("rivers_turn_work",ThreadSuit.WORKS,4,ThreadAspect.WORK),e("precision_has_rhythm",ThreadSuit.WORKS,5,ThreadAspect.TEMPO),e("heat_go_somewhere",ThreadSuit.WORKS,6,ThreadAspect.ROBUSTNESS),e("pressure_changes_matter",ThreadSuit.WORKS,7,ThreadAspect.IMPACT),e("chemistry_remembers_sequence",ThreadSuit.WORKS,8,ThreadAspect.CONTROL),e("electricity_agreement",ThreadSuit.WORKS,9,ThreadAspect.WORK),e("machines_can_remember",ThreadSuit.WORKS,10,ThreadAspect.CONTROL),e("rails_turn_distance",ThreadSuit.WORKS,11,ThreadAspect.TEMPO),e("vessel_becomes_place",ThreadSuit.WORKS,12,ThreadAspect.MOBILITY),e("leave_atmosphere",ThreadSuit.WORKS,13,ThreadAspect.ENDURANCE),
        e("life_becomes_capable",ThreadSuit.POWERS,1,ThreadAspect.RENEWAL),e("blood_infrastructure",ThreadSuit.POWERS,2,ThreadAspect.RENEWAL),e("dead_leave_work",ThreadSuit.POWERS,3,ThreadAspect.IMPACT),e("spirits_honour_contracts",ThreadSuit.POWERS,4,ThreadAspect.CONTROL),e("reality_has_grammar",ThreadSuit.POWERS,5,ThreadAspect.CONTROL),e("elements_change_sentence",ThreadSuit.POWERS,6,ThreadAspect.IMPACT),e("doors_borrow_worlds",ThreadSuit.POWERS,7,ThreadAspect.MOBILITY),e("power_needs_anchor",ThreadSuit.POWERS,8,ThreadAspect.ROBUSTNESS),e("traditions_can_cross",ThreadSuit.POWERS,9,ThreadAspect.CONTROL),e("relics_remember_wearers",ThreadSuit.POWERS,10,ThreadAspect.ENDURANCE),e("source_becomes_machinery",ThreadSuit.POWERS,11,ThreadAspect.WORK),e("rules_adhere_matter",ThreadSuit.POWERS,12,ThreadAspect.ROBUSTNESS),e("body_borrow_state",ThreadSuit.POWERS,13,ThreadAspect.TEMPO),
        e("life_reaches_tether",ThreadSuit.FRAGILITY,1,ThreadAspect.ENDURANCE),e("ruins_are_instructions",ThreadSuit.FRAGILITY,2,ThreadAspect.CONTROL),e("enemies_no_cause",ThreadSuit.FRAGILITY,3,ThreadAspect.TEMPO),e("copy_outlives_work",ThreadSuit.FRAGILITY,4,ThreadAspect.ROBUSTNESS),e("army_walks_toward_you",ThreadSuit.FRAGILITY,5,ThreadAspect.IMPACT),e("world_can_be_condensed",ThreadSuit.FRAGILITY,6,ThreadAspect.RENEWAL),f("world_can_be_wagered",7,ThreadAspect.IMPACT),f("severity_has_yield",8,ThreadAspect.ENDURANCE),f("disaster_has_agenda",9,ThreadAspect.CONTROL),f("apocalypses_disagree",10,ThreadAspect.TEMPO),f("other_hands_built_caches",11,ThreadAspect.ROBUSTNESS),f("recognition_changes_both",12,ThreadAspect.CONTROL),f("defeat_not_erasure",13,ThreadAspect.RENEWAL));
    static final List<String> IDS=ENTRIES.stream().map(Entry::id).toList();
    static final Map<String,Entry> BY_ID=index();
    static final Map<String,ThreadAspect> EXPECTED_ASPECTS=aspects();
    private static Entry e(String id,ThreadSuit suit,int order,ThreadAspect aspect){return new Entry(id,suit,order,aspect,false);}
    private static Entry f(String id,int order,ThreadAspect aspect){return new Entry(id,ThreadSuit.FRAGILITY,order,aspect,true);}
    private static Map<String,Entry> index(){var out=new LinkedHashMap<String,Entry>();ENTRIES.forEach(e->out.put(e.id(),e));return Map.copyOf(out);}
    private static Map<String,ThreadAspect> aspects(){var out=new LinkedHashMap<String,ThreadAspect>();ENTRIES.forEach(e->out.put(e.id(),e.aspect()));return Map.copyOf(out);}
    static float itemIndex(String id){int index=IDS.indexOf(id);return index<0?0:index+1;}
    private ThreadArt(){}
}
