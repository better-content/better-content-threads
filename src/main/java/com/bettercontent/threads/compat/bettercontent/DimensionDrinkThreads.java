package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.dimensiondrink.api.event.FontAggregateReturnEvent;
import com.bettercontent.dimensiondrink.api.event.FontEnterEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Dimension Drink's transport lifecycle into correlated Thread evidence. */
public final class DimensionDrinkThreads {
    private DimensionDrinkThreads() {}

    @SubscribeEvent
    public static void enteredFont(FontEnterEvent event) {
        ThreadSignals.emit(event.getPlayer(), "font_transit", "depart", event.getRunId().toString());
    }

    @SubscribeEvent
    public static void returnedFromAggregate(FontAggregateReturnEvent event) {
        ThreadSignals.emit(event.getPlayer(), "font_transit", "return", event.getRunId().toString());
        String active = ThreadSignals.activeCorrelation(event.getPlayer(), "the_end_is_not_a_door");
        if (active != null) {
            ThreadSignals.emit(event.getPlayer(), "font_route_completed", "returned", active);
        }
    }
}
