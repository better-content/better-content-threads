package com.bettercontent.betterdiscoveryguides.compat.bettercontent;
import com.bettercontent.betteroc2rwirelessmessaging.api.WirelessMessageReceivedEvent;
import com.bettercontent.betterdiscoveryguides.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class WirelessReceiptThreads {
 private WirelessReceiptThreads(){}
 @SubscribeEvent public static void received(WirelessMessageReceivedEvent event){ThreadSignals.emit(event.server,event.owner,"wireless_message","received",event.operation,event.bytes+" bytes delivered; relay cost "+event.energy+" energy");}
}
