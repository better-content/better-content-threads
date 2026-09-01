package com.bettercontent.threads;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraftforge.fml.ModList;

final class ThreadDoorways {
    private ThreadDoorways() {}
    static void open(ThreadNetwork.Card card){
        var mc=Minecraft.getInstance();mc.setScreen(null);
        if(card.doorwayType().equals("emi")&&openEmi(card.doorwayTarget()))return;
        if(card.doorwayType().equals("ponder")&&openPonder(mc,card.doorwayTarget()))return;
        String needle=switch(card.doorwayType()){case"trace_sight"->"traces";case"diet"->"diet";case"rpg"->"rpg";case"tconstruct"->"tconstruct";default->card.doorwayType();};
        for(KeyMapping key:mc.options.keyMappings)if(key.getName().toLowerCase(java.util.Locale.ROOT).contains(needle)){KeyMapping.click(key.getKey());return;}
        if(mc.player!=null)mc.player.displayClientMessage(Component.literal("Look closer: "+card.doorwayTarget()),true);
    }

    private static boolean openEmi(String target){
        if(!ModList.get().isLoaded("emi"))return false;
        ItemStack stack=targetStack(target);
        if(stack.isEmpty())return false;
        EmiApi.displayRecipes(EmiStack.of(stack));
        return true;
    }

    private static boolean openPonder(Minecraft minecraft,String target){
        if(!ModList.get().isLoaded("ponder"))return false;
        ItemStack stack=targetStack(target);
        if(stack.isEmpty())return false;
        minecraft.setScreen(PonderUI.of(stack));
        return true;
    }

    private static ItemStack targetStack(String target){
        ResourceLocation id=ResourceLocation.tryParse(target);
        if(id==null)return ItemStack.EMPTY;
        var item=BuiltInRegistries.ITEM.get(id);
        return item==Items.AIR?ItemStack.EMPTY:new ItemStack(item);
    }
}
