package com.bettercontent.learningsurfaces.compat.bettercontent;
import com.bettercontent.dimensiondrink.api.event.FontEnterEvent;
import com.bettercontent.learningsurfaces.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class DimensionDrinkThreads {
    private DimensionDrinkThreads() {}
    @SubscribeEvent public static void enteredFont(FontEnterEvent event) {
        ThreadSignals.emit(event.getPlayer(), "font_transit", "depart", event.getRunId().toString());
    }
}
