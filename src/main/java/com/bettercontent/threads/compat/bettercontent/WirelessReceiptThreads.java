package com.bettercontent.threads.compat.bettercontent;
import com.bettercontent.oc2rwirelesspubsub.api.WirelessMessageReceivedEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class WirelessReceiptThreads {
 private WirelessReceiptThreads(){}
 @SubscribeEvent public static void received(WirelessMessageReceivedEvent event){ThreadSignals.emit(event.server,event.owner,"wireless_message","received",event.operation,event.bytes+" bytes delivered; relay cost "+event.energy+" energy");}
}
