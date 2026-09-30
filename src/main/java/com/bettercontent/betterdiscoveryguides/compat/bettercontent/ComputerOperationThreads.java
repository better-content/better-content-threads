package com.bettercontent.betterdiscoveryguides.compat.bettercontent;
import com.bettercontent.betteroc2rcreatecontrols.api.events.MachineOperationEvent;
import com.bettercontent.betterdiscoveryguides.OperationOwners;
import net.minecraftforge.eventbus.api.SubscribeEvent;
/** OC2's query has no invoker identity; credit the apparatus's persisted configuring owner. */
public final class ComputerOperationThreads {
 private ComputerOperationThreads(){}
 @SubscribeEvent public static void changed(MachineOperationEvent event){OperationOwners.completed(event.getMachine(),"computer_operation","changed",event.getOperation()+": "+event.getBefore()+" → "+event.getAfter());}
}
