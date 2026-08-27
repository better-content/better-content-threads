package com.bettercontent.threads;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public final class ThreadFacsimileItem extends Item {
    private static final String THREAD="Thread",COLLECTOR="Collector",COLLECTOR_ID="CollectorId",LINEAGE="Lineage";
    ThreadFacsimileItem(Properties properties){super(properties);}
    public static ItemStack create(String id,ServerPlayer player){
        var stack=new ItemStack(ThreadRegistry.FACSIMILE.get());var tag=stack.getOrCreateTag();tag.putString(THREAD,id);tag.putString(COLLECTOR,player.getGameProfile().getName());tag.putUUID(COLLECTOR_ID,player.getUUID());
        try{var api=Class.forName("com.bettercontent.worldlifecyclemanager.PrestigeService");var lineage=api.getMethod("lineage",net.minecraft.server.MinecraftServer.class).invoke(null,player.server);tag.putString(LINEAGE,(String)lineage.getClass().getMethod("lineageId").invoke(lineage));}catch(ReflectiveOperationException ignored){tag.putString(LINEAGE,"world");}
        return stack;
    }
    public static String threadId(ItemStack stack){return stack.hasTag()?stack.getTag().getString(THREAD):"";}
    @Override public Component getName(ItemStack stack){var d=ThreadDefinitions.INSTANCE.get(threadId(stack));return d==null?super.getName(stack):Component.literal(d.title()+" — Facsimile");}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> tooltip,TooltipFlag flag){var tag=stack.getTag();if(tag==null)return;tooltip.add(Component.literal("Collected by "+tag.getString(COLLECTOR)).withStyle(ChatFormatting.GRAY));tooltip.add(Component.literal("Lineage "+tag.getString(LINEAGE)).withStyle(ChatFormatting.DARK_GRAY));tooltip.add(Component.literal("A signed display copy. It grants nothing.").withStyle(ChatFormatting.ITALIC,ChatFormatting.DARK_GRAY));}
}
