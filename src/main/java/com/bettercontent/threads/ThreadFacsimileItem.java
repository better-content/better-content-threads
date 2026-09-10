package com.bettercontent.threads;

import com.bettercontent.threads.compat.bettercontent.WorldLifecycleThreads;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.List;

public final class ThreadFacsimileItem extends Item {
    private static final String THREAD="Thread",COLLECTOR="Collector",COLLECTOR_ID="CollectorId",LINEAGE="Lineage";
    ThreadFacsimileItem(Properties properties){super(properties);}
    public static ItemStack create(String id,ServerPlayer player){
        var stack=new ItemStack(ThreadRegistry.FACSIMILE.get());var tag=stack.getOrCreateTag();tag.putString(THREAD,id);tag.putString(COLLECTOR,player.getGameProfile().getName());tag.putUUID(COLLECTOR_ID,player.getUUID());
        if(!ModList.get().isLoaded("world_lifecycle_manager")){tag.putString(LINEAGE,"world");return stack;}
        try{tag.putString(LINEAGE,WorldLifecycleThreads.lineageId(player.server));}catch(IOException ignored){tag.putString(LINEAGE,"world");}
        return stack;
    }
    public static String threadId(ItemStack stack){return stack.hasTag()?stack.getTag().getString(THREAD):"";}
    @Override public Component getName(ItemStack stack){var d=ThreadDefinitions.INSTANCE.get(threadId(stack));return d==null?super.getName(stack):Component.literal(d.title()+" — Facsimile");}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> tooltip,TooltipFlag flag){var tag=stack.getTag();if(tag==null)return;tooltip.add(Component.literal("Collected by "+tag.getString(COLLECTOR)).withStyle(ChatFormatting.GRAY));tooltip.add(Component.literal("Lineage "+tag.getString(LINEAGE)).withStyle(ChatFormatting.DARK_GRAY));tooltip.add(Component.literal("A signed display copy. It grants nothing.").withStyle(ChatFormatting.ITALIC,ChatFormatting.DARK_GRAY));}
}
