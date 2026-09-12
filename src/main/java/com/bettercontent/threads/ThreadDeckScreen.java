package com.bettercontent.threads;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** Discovered-first journal. The reader advances only through explicit input. */
public class ThreadDeckScreen extends Screen {
    private static final int DETAIL_MARGIN=12,DETAIL_GAP=18,DETAIL_MIN_PANEL_WIDTH=96,DETAIL_MAX_PANEL_WIDTH=230;
    private static final int TOP=82,ROW_HEIGHT=34;
    private final List<ThreadNetwork.Card> cards=new ArrayList<>();
    private final Set<String> readHere=new HashSet<>();
    private final ThreadRevealState reveal=new ThreadRevealState();
    private ThreadTopic topic;
    private String selectedId;
    private boolean detail;
    private int scrollRow,textScroll,textMaximumScroll;
    private long lastFrame;
    private Button doorwayButton,facsimileButton,continueButton;

    ThreadDeckScreen(List<ThreadNetwork.Card> cards){this(cards,"");}
    ThreadDeckScreen(List<ThreadNetwork.Card> cards,String focusId){
        super(Component.literal("Threads"));this.cards.addAll(cards.stream().filter(ThreadNetwork.Card::known).toList());
        var focus=this.cards.stream().filter(c->c.id().equals(focusId)).findFirst().orElse(null);
        if(focus==null)focus=nextUnread();
        if(focus!=null){selectedId=focus.id();detail=true;selectCurrent(true);}
        else if(!this.cards.isEmpty())selectedId=this.cards.get(0).id();
    }
    void updateCards(List<ThreadNetwork.Card> replacement){
        cards.clear();cards.addAll(replacement.stream().filter(ThreadNetwork.Card::known).toList());
        if(current()==null){detail=false;selectedId=visible().isEmpty()?null:visible().get(0).id();}
    }
    @Override protected void init(){
        addRenderableWidget(Button.builder(Component.translatable("screen.better_content_threads.lessons"),b->minecraft.setScreen(new LearningLibraryScreen(cards))).bounds(8,2,68,20).build());
        doorwayButton=addRenderableWidget(Button.builder(Component.literal("Look closer"),b->{if(current()!=null)ThreadDoorways.open(current());}).bounds(0,0,100,20).build());
        facsimileButton=addRenderableWidget(Button.builder(Component.literal("Get card copy"),b->{if(current()!=null)ThreadNetwork.request("issue",current().id());}).bounds(0,0,100,20).build());
        continueButton=addRenderableWidget(Button.builder(Component.literal("Continue"),b->advanceReader()).bounds(width-92,height-24,80,20).build());
        doorwayButton.visible=facsimileButton.visible=continueButton.visible=false;
    }
    @Override public boolean isPauseScreen(){return true;}
    private boolean unread(ThreadNetwork.Card c){return c.unread()&&!readHere.contains(c.id());}
    private ThreadNetwork.Card current(){return cards.stream().filter(c->c.id().equals(selectedId)).findFirst().orElse(null);}
    private ThreadNetwork.Card nextUnread(){return ThreadJournalOrder.nextUnread(cards,readHere);}
    private List<ThreadNetwork.Card> visible(){return ThreadJournalOrder.visible(cards,readHere,topic);}
    private int columns(){return width<500?1:2;}
    private int visibleRows(){return Math.max(1,(height-TOP-16)/ROW_HEIGHT);}
    private int totalRows(){return (visible().size()+columns()-1)/columns();}
    private void selectCurrent(boolean automaticReveal){var c=current();reveal.select(c!=null&&unread(c));if(automaticReveal&&c!=null&&unread(c))reveal.activate();lastFrame=System.currentTimeMillis();textScroll=0;}
    private void finishDevelopment(){var c=current();if(c!=null&&unread(c)){readHere.add(c.id());ThreadNetwork.request("read",c.id());}}
    private void advanceReader(){
        if(reveal.phase()!=ThreadRevealState.Phase.COMPLETE){if(reveal.activate()==ThreadRevealState.Activation.COMPLETED)finishDevelopment();return;}
        var next=nextUnread();if(next==null){detail=false;return;}
        selectedId=next.id();detail=true;selectCurrent(true);
    }
    private void selectTopic(ThreadTopic value){topic=value;detail=false;scrollRow=0;var list=visible();selectedId=list.isEmpty()?null:list.get(0).id();}

    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        long now=System.currentTimeMillis();long delta=Math.min(100,Math.max(0,now-lastFrame));lastFrame=now;
        if(detail&&reveal.advance(delta))finishDevelopment();
        renderBackground(g);g.fill(0,0,width,height,0xEF101412);
        g.drawCenteredString(font,"THREADS",width/2,10,0xFFF0E5CE);
        doorwayButton.visible=facsimileButton.visible=continueButton.visible=false;
        renderTabs(g);
        if(detail)renderDetail(g);else renderJournal(g);
        super.render(g,mx,my,partial);
    }
    private void renderTabs(GuiGraphics g){
        int total=Math.min(width-16,560),cell=total/4,start=(width-cell*4)/2;
        for(int i=0;i<8;i++){var t=i==0?null:ThreadTopic.values()[i-1];int x=start+(i%4)*cell,y=24+(i/4)*20;
            int color=t==null?0x716346:t.color();g.fill(x,y,x+cell-2,y+18,((t==topic?0xD0:0x66)<<24)|color);
            g.drawCenteredString(font,capital(t==null?"all":t.id()),x+(cell-2)/2,y+5,0xFFFFFFFF);
        }
    }
    private void renderJournal(GuiGraphics g){
        long current=cards.stream().filter(ThreadNetwork.Card::discovered).count(),unread=cards.stream().filter(this::unread).count();
        g.drawCenteredString(font,Component.translatable("screen.better_content_threads.journal_status",current,cards.size(),unread),width/2,68,0xFFBAB8AB);
        var list=visible();if(list.isEmpty()){g.drawCenteredString(font,"Discoveries appear here when their events happen.",width/2,TOP+24,0xFFBAB8AB);return;}
        scrollRow=Math.max(0,Math.min(scrollRow,Math.max(0,totalRows()-visibleRows())));
        int cell=(width-24)/columns(),start=(width-cell*columns())/2;
        for(int i=0;i<list.size();i++){int row=i/columns()-scrollRow;if(row<0||row>=visibleRows())continue;
            var c=list.get(i);int x=start+(i%columns())*cell,y=TOP+row*ROW_HEIGHT;
            if(unread(c))g.fill(x,y,x+cell-2,y+ROW_HEIGHT-2,0x28C6A15B);
            if(c.id().equals(selectedId))g.fill(x,y+ROW_HEIGHT-3,x+cell-3,y+ROW_HEIGHT-2,0xFFC6A15B);
            if(unread(c))ThreadClient.renderSealedPlate(g,x+3,y+3,16,24,ThreadTopic.parse(c.topic()).color(),ThreadClient.ARCHIVE_GOLD,c.id().hashCode(),true);
            else ThreadClient.renderArt(g,ThreadClient.layer(c.art(),"thumb"),x+3,y+3,16,24);
            g.drawString(font,fit(c.title(),cell-34),x+26,y+5,0xFFF0E5CE,false);
            g.drawString(font,capital(c.topic())+(unread(c)?" · Unread":" · "+c.generationCount()+" generation"+(c.generationCount()==1?"":"s")),x+26,y+18,0xFFBAB8AB,false);
        }
        g.drawCenteredString(font,"↑ ↓ Select · Enter Read · Scroll for more",width/2,height-10,0xFFBAB8AB);
    }
    private void renderDetail(GuiGraphics g){
        var c=current();if(c==null)return;var l=detailLayout(width,height);
        g.drawString(font,"‹ Journal",12,68,0xFFB6A98D,false);
        g.fill(l.cardX()-2,l.cardY()-2,l.cardX()+l.cardWidth()+2,l.cardY()+l.cardHeight()+2,0xFF000000|ThreadTopic.parse(c.topic()).color());
        if(reveal.phase()==ThreadRevealState.Phase.COMPLETE)ThreadClient.renderArt(g,c.art(),l.cardX(),l.cardY(),l.cardWidth(),l.cardHeight());
        else{ThreadClient.renderSealedPlate(g,l.cardX(),l.cardY(),l.cardWidth(),l.cardHeight(),ThreadTopic.parse(c.topic()).color(),ThreadClient.ARCHIVE_GOLD,c.id().hashCode(),true);
            if(reveal.phase()==ThreadRevealState.Phase.DEVELOPING)ThreadClient.renderArt(g,c.art(),l.cardX(),l.cardY(),l.cardWidth(),l.cardHeight(),reveal.elapsedMs()/(float)ThreadRevealState.DURATION_MS);
        }
        if(reveal.phase()!=ThreadRevealState.Phase.COMPLETE)return;
        var text=new ReadingText(font,l.panelWidth()-8);
        text.add(c.title(),0xFFF0E5CE);text.add(capital(c.topic()),0xFFBAB8AB);text.gap();
        text.add("WHAT HAPPENED",0xFFC6A15B);text.add(c.event(),0xFFF0E5CE);
        if(!c.context().isEmpty())text.add(c.context(),0xFFBAB8AB);
        text.gap();text.add("WHY",0xFFC6A15B);text.add(c.cause(),0xFFF0E5CE);
        text.gap();text.add("WHAT YOU CAN DO",0xFFC6A15B);text.add(c.action(),0xFFF0E5CE);
        text.gap();text.add("Discovered in "+c.generationCount()+" generation"+(c.generationCount()==1?"":"s")+(c.discovered()?" · This generation":""),0xFFBAB8AB);
        doorwayButton.visible=ThreadDoorways.available(c);doorwayButton.setMessage(ThreadDoorways.label(c));
        int footer=doorwayButton.visible?48:24;
        doorwayButton.setX(l.detailsX());doorwayButton.setY(l.cardY()+l.cardHeight()-44);doorwayButton.setWidth(l.panelWidth());
        facsimileButton.visible=true;facsimileButton.setX(l.detailsX());facsimileButton.setY(l.cardY()+l.cardHeight()-20);facsimileButton.setWidth(l.panelWidth());
        int viewport=Math.max(12,l.cardHeight()-footer);textMaximumScroll=text.maximumScroll(viewport);textScroll=Math.max(0,Math.min(textScroll,textMaximumScroll));
        text.render(g,l.detailsX(),l.cardY(),l.panelWidth(),viewport,textScroll);
        continueButton.visible=true;continueButton.setMessage(Component.literal(nextUnread()==null?"Journal":"Continue"));
    }
    @Override public boolean mouseClicked(double x,double y,int button){
        if(super.mouseClicked(x,y,button))return true;
        int total=Math.min(width-16,560),cell=total/4,start=(width-cell*4)/2;
        if(y>=24&&y<64&&x>=start&&x<start+cell*4){int i=(int)((y-24)/20)*4+(int)((x-start)/cell);selectTopic(i==0?null:ThreadTopic.values()[i-1]);return true;}
        if(detail){if(x<100&&y>=64&&y<82){detail=false;return true;}if(reveal.phase()!=ThreadRevealState.Phase.COMPLETE)advanceReader();return true;}
        int cardCell=(width-24)/columns(),left=(width-cardCell*columns())/2;
        if(y>=TOP&&y<TOP+visibleRows()*ROW_HEIGHT&&x>=left&&x<left+cardCell*columns()){
            int index=((int)((y-TOP)/ROW_HEIGHT)+scrollRow)*columns()+(int)((x-left)/cardCell);var list=visible();
            if(index<list.size()){selectedId=list.get(index).id();detail=true;selectCurrent(true);}return true;
        }
        return false;
    }
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(detail&&key==GLFW.GLFW_KEY_SPACE){advanceReader();return true;}
        if(detail&&key==GLFW.GLFW_KEY_ESCAPE){detail=false;return true;}
        if(detail&&(key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_DOWN||key==GLFW.GLFW_KEY_PAGE_UP||key==GLFW.GLFW_KEY_PAGE_DOWN)){
            int step=(key==GLFW.GLFW_KEY_PAGE_UP||key==GLFW.GLFW_KEY_PAGE_DOWN)?72:12;scrollText((key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_PAGE_UP)?-step:step);return true;
        }
        if(!detail&&(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_RIGHT||key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_DOWN)){
            var list=visible();if(list.isEmpty())return true;int index=0;for(int i=0;i<list.size();i++)if(list.get(i).id().equals(selectedId))index=i;
            int step=(key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_DOWN)?columns():1;
            index=Math.floorMod(index+((key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_UP)?-step:step),list.size());selectedId=list.get(index).id();
            scrollRow=Math.max(0,Math.min(scrollRow,index/columns()));if(index/columns()>=scrollRow+visibleRows())scrollRow=index/columns()-visibleRows()+1;setFocused(null);return true;
        }
        if(!detail&&(key==GLFW.GLFW_KEY_ENTER||key==GLFW.GLFW_KEY_SPACE)){if(current()!=null){detail=true;selectCurrent(true);}return true;}
        return super.keyPressed(key,scan,mods);
    }
    private void scrollText(int delta){textScroll=Math.max(0,Math.min(textMaximumScroll,textScroll+delta));}
    @Override public boolean mouseScrolled(double x,double y,double delta){if(detail)scrollText(delta<0?24:-24);else scrollRow=Math.max(0,Math.min(Math.max(0,totalRows()-visibleRows()),scrollRow+(delta<0?1:-1)));return true;}
    private String fit(String text,int max){max=Math.max(1,max);if(font.width(text)<=max)return text;while(text.length()>1&&font.width(text+"…")>max)text=text.substring(0,text.length()-1);return text+"…";}
    private static String capital(String s){return s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1);}
    static DetailLayout detailLayout(int screenWidth, int screenHeight) {
        int availableWidth = Math.max(1, screenWidth - DETAIL_MARGIN * 2);
        int availableHeight = Math.max(1, screenHeight - 120);
        int maximumCardWidth = Math.max(1, availableWidth - DETAIL_GAP - DETAIL_MIN_PANEL_WIDTH);
        int maximumCardHeightFromWidth = Math.max(1, maximumCardWidth * 3 / 2);
        int cardHeight = Math.max(1, Math.min(300, Math.min(availableHeight, maximumCardHeightFromWidth)));
        int cardWidth = Math.max(1, cardHeight * 2 / 3);
        int panelWidth = Math.max(1, Math.min(DETAIL_MAX_PANEL_WIDTH, availableWidth - cardWidth - DETAIL_GAP));
        int contentWidth = cardWidth + DETAIL_GAP + panelWidth;
        int cardX = Math.max(0, (screenWidth - contentWidth) / 2);
        return new DetailLayout(cardX, 86, cardWidth, cardHeight, cardX + cardWidth + DETAIL_GAP, panelWidth);
    }

    record DetailLayout(int cardX, int cardY, int cardWidth, int cardHeight, int detailsX, int panelWidth) {}
}
