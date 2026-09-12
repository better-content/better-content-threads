package com.bettercontent.threads;

import net.minecraft.client.Minecraft;
import net.minecraft.Util;
import java.util.UUID;

public final class ContextHintClient {
    private static String context="general";
    private static long received;
    public static void receive(UUID player,String value){
        var local=Minecraft.getInstance().player;
        if(local!=null&&local.getUUID().equals(player)){context=value;received=Util.getMillis();}
    }
    static String context(){return Util.getMillis()-received<=5000?context:"general";}
    public static boolean suppressThreadNotice(){
        if(context().startsWith("door_"))return true;
        return net.minecraftforge.fml.ModList.get().isLoaded("downed_player_revival")&&BodyScreenGuard.open();
    }
    private static final class BodyScreenGuard { static boolean open(){return Minecraft.getInstance().screen instanceof com.bettercontent.downedplayerrevival.client.BodyScreen;} }
    static void clear(){context="general";received=0;}
}
