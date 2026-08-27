package com.bettercontent.threads;

import com.bettercontent.threads.BetterContentThreads;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.*;
import java.util.function.Supplier;

public final class ThreadNetwork {
    private static final String VERSION="7";
    private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(BetterContentThreads.MOD_ID,"threads"),()->VERSION,VERSION::equals,VERSION::equals);
    private static final Map<UUID,Long>lastIssue=new HashMap<>();private static int messageId;
    private ThreadNetwork(){}
    public enum NoticeKind{REVEAL,COMPLETE}
    public static void register(){CHANNEL.messageBuilder(Sync.class,messageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(Sync::encode).decoder(Sync::decode).consumerMainThread(Sync::handle).add();CHANNEL.messageBuilder(Action.class,messageId++,NetworkDirection.PLAY_TO_SERVER).encoder(Action::encode).decoder(Action::decode).consumerMainThread(Action::handle).add();}
    public static Notice notice(ThreadDefinition d,NoticeKind kind){return new Notice(kind,d.id(),d.title(),d.suit().id(),d.aspect().id());}
    public static void sync(ServerPlayer player,boolean open,List<Notice> notices){var state=ThreadPlayerState.get(player);var cards=ThreadDefinitions.INSTANCE.all().stream().sorted(Comparator.comparing((ThreadDefinition d)->d.suit().ordinal()).thenComparingInt(ThreadDefinition::order)).map(d->card(d,state)).toList();CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new Sync(open,cards,notices));}
    private static Card card(ThreadDefinition d,ThreadPlayerState s){boolean known=s.known.contains(d.id()),active=s.active.contains(d.id());var doorway=d.doorway();return new Card(d.id(),d.title(),d.suit().id(),d.order(),d.aspect().id(),d.art().toString(),d.future(),known,known&&s.unread.contains(d.id()),active,s.completed.contains(d.id()),known&&!d.future()?d.prose():"",active&&!d.future()?d.invitation():"",active&&!d.future()?d.action():"",doorway==null?"":doorway.type(),doorway==null?"":doorway.target(),s.completionCounts.getOrDefault(d.id(),0),s.firstGeneration.getOrDefault(d.id(),-1L),s.lastGeneration.getOrDefault(d.id(),-1L),s.routeSummary(d.id()));}
    public static void request(String action,String thread){CHANNEL.sendToServer(new Action(action,thread));}

    public record Card(String id,String title,String suit,int order,String aspect,String art,boolean future,boolean known,boolean unread,boolean active,boolean completed,String prose,String invitation,String action,String doorwayType,String doorwayTarget,int completionCount,long firstGeneration,long lastGeneration,String routeSummary){
        public Card{ThreadPacketValidation.id(id);ThreadPacketValidation.title(title);ThreadSuit.parse(suit);if(order<1||order>13)throw new IllegalArgumentException("invalid thread order");ThreadAspect.parse(aspect);ThreadPacketValidation.resource(art,"art");if(prose.length()>ThreadDefinition.MAX_TEXT||invitation.length()>160||action.length()>192||routeSummary.length()>512)throw new IllegalArgumentException("oversized thread card text");if(!doorwayType.isEmpty()&&!ThreadDefinition.Doorway.validType(doorwayType))throw new IllegalArgumentException("invalid doorway type");if(doorwayTarget.length()>128||completionCount<0||firstGeneration< -1||lastGeneration< -1)throw new IllegalArgumentException("invalid thread history");if(future&&(known||unread||active||completed||!prose.isEmpty()||!invitation.isEmpty()||!action.isEmpty()||!doorwayType.isEmpty()))throw new IllegalArgumentException("future card leaked content");}
        void encode(FriendlyByteBuf b){b.writeUtf(id,48);b.writeUtf(title,64);b.writeUtf(suit,16);b.writeVarInt(order);b.writeUtf(aspect,16);b.writeUtf(art,128);b.writeBoolean(future);b.writeBoolean(known);b.writeBoolean(unread);b.writeBoolean(active);b.writeBoolean(completed);b.writeUtf(prose,ThreadDefinition.MAX_TEXT);b.writeUtf(invitation,160);b.writeUtf(action,192);b.writeUtf(doorwayType,24);b.writeUtf(doorwayTarget,128);b.writeVarInt(completionCount);b.writeLong(firstGeneration);b.writeLong(lastGeneration);b.writeUtf(routeSummary,512);}
        static Card decode(FriendlyByteBuf b){return new Card(b.readUtf(48),b.readUtf(64),b.readUtf(16),b.readVarInt(),b.readUtf(16),b.readUtf(128),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readUtf(ThreadDefinition.MAX_TEXT),b.readUtf(160),b.readUtf(192),b.readUtf(24),b.readUtf(128),b.readVarInt(),b.readLong(),b.readLong(),b.readUtf(512));}
    }
    public record Notice(NoticeKind kind,String id,String title,String suit,String aspect){
        public Notice{Objects.requireNonNull(kind);ThreadPacketValidation.id(id);ThreadPacketValidation.title(title);ThreadSuit.parse(suit);ThreadAspect.parse(aspect);}
        String identity(){return kind.name()+":"+id;}
        void encode(FriendlyByteBuf b){b.writeEnum(kind);b.writeUtf(id,48);b.writeUtf(title,64);b.writeUtf(suit,16);b.writeUtf(aspect,16);}
        static Notice decode(FriendlyByteBuf b){return new Notice(b.readEnum(NoticeKind.class),b.readUtf(48),b.readUtf(64),b.readUtf(16),b.readUtf(16));}
    }
    public record Sync(boolean open,List<Card>cards,List<Notice>notices){
        public Sync{if(cards.size()>52||notices.size()>52)throw new IllegalArgumentException("too many thread entries");}
        void encode(FriendlyByteBuf b){b.writeBoolean(open);writeCards(b,cards);b.writeVarInt(notices.size());notices.forEach(n->n.encode(b));}
        static Sync decode(FriendlyByteBuf b){boolean open=b.readBoolean();var cards=readCards(b);int n=b.readVarInt();if(n<0||n>52)throw new IllegalArgumentException("invalid thread notice packet");var notices=new ArrayList<Notice>(n);var ids=new HashSet<String>();for(int i=0;i<n;i++){var notice=Notice.decode(b);if(!ids.add(notice.identity()))throw new IllegalArgumentException("duplicate thread notice");notices.add(notice);}return new Sync(open,cards,List.copyOf(notices));}
        static void handle(Sync m,Supplier<NetworkEvent.Context>c){c.get().enqueueWork(()->ThreadClient.receive(m));c.get().setPacketHandled(true);}
    }
    public record Action(String action,String thread){
        void encode(FriendlyByteBuf b){b.writeUtf(action,16);b.writeUtf(thread,48);}static Action decode(FriendlyByteBuf b){return new Action(b.readUtf(16),b.readUtf(48));}
        static void handle(Action m,Supplier<NetworkEvent.Context>c){var player=c.get().getSender();c.get().enqueueWork(()->handle(player,m));c.get().setPacketHandled(true);}
        private static void handle(ServerPlayer player,Action action){if(player==null)return;if(action.action.equals("open")){sync(player,true,List.of());return;}if(!ThreadDefinitions.INSTANCE.contains(action.thread))return;var definition=ThreadDefinitions.INSTANCE.get(action.thread);var state=ThreadPlayerState.get(player);if(definition.future()||!state.known.contains(action.thread))return;if(action.action.equals("read")){if(state.markRead(action.thread)){state.save(player);sync(player,false,List.of());}return;}if(!action.action.equals("issue")||state.unread.contains(action.thread))return;long now=System.currentTimeMillis(),previous=lastIssue.getOrDefault(player.getUUID(),0L);if(now-previous<1000)return;lastIssue.put(player.getUUID(),now);var stack=ThreadFacsimileItem.create(action.thread,player);if(!player.getInventory().add(stack)){player.displayClientMessage(Component.literal("Make room for the facsimile first."),true);return;}player.containerMenu.broadcastChanges();}
    }
    private static void writeCards(FriendlyByteBuf b,List<Card>cards){if(cards.size()>52)throw new IllegalArgumentException("too many thread cards");b.writeVarInt(cards.size());cards.forEach(c->c.encode(b));}
    private static List<Card>readCards(FriendlyByteBuf b){int n=b.readVarInt();if(n<0||n>52)throw new IllegalArgumentException("invalid thread packet");var out=new ArrayList<Card>(n);var ids=new HashSet<String>();for(int i=0;i<n;i++){var card=Card.decode(b);if(!ids.add(card.id()))throw new IllegalArgumentException("duplicate thread card");out.add(card);}return List.copyOf(out);}
}
