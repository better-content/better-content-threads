package com.bettercontent.threads;

import com.bettercontent.downedplayerrevival.client.ClientRevivalState;
import com.bettercontent.downedplayerrevival.network.BodyView;
import com.bettercontent.downedplayerrevival.network.StateSyncPacket;
import com.bettercontent.downedplayerrevival.state.Region;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import java.util.*;

/** Native DeathScreen rendering and both production overlays, with inert controls and a supplied recap. */
final class CombinedDeathVisualScreen extends DeathScreen {
    private final DeathHint hint;
    CombinedDeathVisualScreen(DeathHint hint){super(Component.literal("The final fall ended this life."),false);this.hint=hint;}
    @Override protected void init(){
        addRenderableWidget(Button.builder(Component.translatable("deathScreen.respawn"),b->{}).bounds(width/2-100,height/4+72,200,20).build());
        addRenderableWidget(Button.builder(Component.translatable("deathScreen.titleScreen"),b->{}).bounds(width/2-100,height/4+96,200,20).build());
        ((com.bettercontent.threads.visual.DeathScreenVisualAccess)(Object)this).threads$score(Component.literal("Score: 17"));
        var regions=new ArrayList<BodyView.RegionView>();
        for(var region:Region.values())regions.add(new BodyView.RegionView(region,1,0,region.ordinal()%2,.1,2,1,3));
        var view=new BodyView(UUID.fromString("00000000-0000-0000-0000-000000000001"),"Layout fixture",0,20,false,0,3,1200,.55,.8,4,regions,List.of(),0,1,List.of(0,0,0));
        ClientRevivalState.accept(new StateSyncPacket(view,2,0,false));
    }
}
