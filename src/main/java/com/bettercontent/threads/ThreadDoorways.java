package com.bettercontent.threads;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

final class ThreadDoorways {
    private ThreadDoorways() {}
    static void open(ThreadNetwork.Card card){
        var mc=Minecraft.getInstance();mc.setScreen(null);
        if(card.doorwayType().equals("ftb")&&mc.player!=null){mc.player.connection.sendCommand("ftbquests open " + card.doorwayTarget());return;}
        String needle=switch(card.doorwayType()){case"trace_sight"->"traces";case"diet"->"diet";case"rpg"->"rpg";case"tconstruct"->"tconstruct";default->card.doorwayType();};
        for(KeyMapping key:mc.options.keyMappings)if(key.getName().toLowerCase(java.util.Locale.ROOT).contains(needle)){KeyMapping.click(key.getKey());return;}
        if(mc.player!=null)mc.player.displayClientMessage(Component.literal("Look closer: "+card.doorwayTarget()),true);
    }
}
