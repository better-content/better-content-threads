package com.bettercontent.betterdiscoveryguides.compat.bettercontent;
import com.bettercontent.betterdimensionfonts.api.event.FontEnterEvent;
import com.bettercontent.betterdiscoveryguides.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class DimensionDrinkThreads {
    private DimensionDrinkThreads() {}
    @SubscribeEvent public static void enteredFont(FontEnterEvent event) {
        ThreadSignals.emit(event.getPlayer(), "font_transit", "depart", event.getRunId().toString());
    }
}
