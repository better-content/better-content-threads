package com.bettercontent.threads;

import com.bettercontent.threads.BetterContentThreads;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.*;
import java.util.function.Supplier;

public final class ThreadNetwork {
    private static final String VERSION="11";
    private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(BetterContentThreads.MOD_ID,"threads"),()->VERSION,VERSION::equals,VERSION::equals);
    private static final Map<UUID,Long>lastIssue=new HashMap<>();private static int messageId;
    private ThreadNetwork(){}
    public enum NoticeKind{DISCOVERY,REMINDER}
    public static void register(){CHANNEL.messageBuilder(HintContext.class,messageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(HintContext::encode).decoder(HintContext::decode).consumerMainThread(HintContext::handle).add();CHANNEL.messageBuilder(Sync.class,messageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(Sync::encode).decoder(Sync::decode).consumerMainThread(Sync::handle).add();CHANNEL.messageBuilder(Action.class,messageId++,NetworkDirection.PLAY_TO_SERVER).encoder(Action::encode).decoder(Action::decode).consumerMainThread(Action::handle).add();CHANNEL.messageBuilder(DeathContext.class,messageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(DeathContext::encode).decoder(DeathContext::decode).consumerMainThread(DeathContext::handle).add();}
    public static void deathHint(ServerPlayer player, String context) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DeathContext(player.getUUID(), context));
    }
    public record DeathContext(UUID player, String context) {
        public DeathContext {
            Objects.requireNonNull(player);
            if (!DeathHintContext.CATEGORIES.contains(context)) throw new IllegalArgumentException("invalid death context");
        }
        void encode(FriendlyByteBuf buffer) { buffer.writeUUID(player); buffer.writeUtf(context, 16); }
        static DeathContext decode(FriendlyByteBuf buffer) { return new DeathContext(buffer.readUUID(), buffer.readUtf(16)); }
        static void handle(DeathContext message, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> DeathHintClient.receive(message.player(), message.context()));
            context.get().setPacketHandled(true);
        }
    }
    public static void hintContext(ServerPlayer player,String context){CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new HintContext(player.getUUID(),context));}
    public record HintContext(UUID player,String context){
        public HintContext{Objects.requireNonNull(player);if(context==null||!context.matches("[a-z0-9_:-]{0,64}"))throw new IllegalArgumentException("invalid hint context");}
        void encode(FriendlyByteBuf b){b.writeUUID(player);b.writeUtf(context,64);}
        static HintContext decode(FriendlyByteBuf b){return new HintContext(b.readUUID(),b.readUtf(64));}
        static void handle(HintContext m,Supplier<NetworkEvent.Context> c){c.get().enqueueWork(()->ContextHintClient.receive(m.player(),m.context()));c.get().setPacketHandled(true);}
    }
    public static Notice notice(ThreadDefinition d,NoticeKind kind,String context,long generation){return new Notice(kind,d.id(),d.title(),d.topic().id(),d.aspect()==null?"":d.aspect().id(),context,generation);}
    public static void sync(ServerPlayer player,boolean open,List<Notice> notices){var state=ThreadPlayerState.get(player);var cards=ThreadDefinitions.INSTANCE.all().stream().filter(d->state.known.contains(d.id())).sorted(Comparator.comparingLong(d->state.discoveryOrder.getOrDefault(d.id(),Long.MAX_VALUE))).map(d->card(d,state)).toList();CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new Sync(open,cards,notices));}
    private static Card card(ThreadDefinition d,ThreadPlayerState s){var doorway=d.doorway();return new Card(d.id(),d.conceptId(),d.title(),d.topic().id(),d.order(),d.aspect()==null?"":d.aspect().id(),d.art().toString(),true,s.unread.contains(d.id()),s.discovered.contains(d.id()),d.event(),d.cause(),d.action(),doorway==null?"":doorway.type(),doorway==null?"":doorway.target(),s.generationCounts.getOrDefault(d.id(),0),s.firstGeneration.getOrDefault(d.id(),-1L),s.lastGeneration.getOrDefault(d.id(),-1L),s.discoveryOrder.getOrDefault(d.id(),0L),s.contexts.getOrDefault(d.id(),""));}
    public static void request(String action,String thread){DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->ClientAccess.send(action,thread));}

    private static final class ClientAccess{
        private static void send(String action,String thread){if(net.minecraft.client.Minecraft.getInstance().getConnection()!=null)CHANNEL.sendToServer(new Action(action,thread));}
    }

    public record Card(String id,String conceptId,String title,String topic,int order,String aspect,String art,boolean known,boolean unread,boolean discovered,String event,String cause,String action,String doorwayType,String doorwayTarget,int generationCount,long firstGeneration,long lastGeneration,long discoveryOrder,String context){
        public Card{ThreadPacketValidation.id(id);if(!conceptId.matches("[a-z0-9_.]{3,80}"))throw new IllegalArgumentException("invalid concept");ThreadPacketValidation.title(title);ThreadTopic.parse(topic);if(order<1||order>52)throw new IllegalArgumentException("invalid global order");if(!aspect.isEmpty())ThreadAspect.parse(aspect);ThreadPacketValidation.resource(art,"art");if(event.length()>ThreadDefinition.MAX_TEXT||cause.length()>ThreadDefinition.MAX_TEXT||action.length()>ThreadDefinition.MAX_TEXT||context.length()>256)throw new IllegalArgumentException("oversized text");if(!doorwayType.isEmpty()&&!ThreadDefinition.Doorway.validType(doorwayType))throw new IllegalArgumentException("invalid doorway");if(doorwayTarget.length()>128||generationCount<0||firstGeneration< -1||lastGeneration< -1||discoveryOrder<0)throw new IllegalArgumentException("invalid history");if(!known&&(!event.isEmpty()||!cause.isEmpty()||!action.isEmpty()||!context.isEmpty()||!doorwayType.isEmpty()||!doorwayTarget.isEmpty()))throw new IllegalArgumentException("unknown card leaked teaching copy");}
        void encode(FriendlyByteBuf b){b.writeUtf(id,48);b.writeUtf(conceptId,80);b.writeUtf(title,64);b.writeUtf(topic,16);b.writeVarInt(order);b.writeUtf(aspect,16);b.writeUtf(art,128);b.writeBoolean(known);b.writeBoolean(unread);b.writeBoolean(discovered);b.writeUtf(event,ThreadDefinition.MAX_TEXT);b.writeUtf(cause,ThreadDefinition.MAX_TEXT);b.writeUtf(action,ThreadDefinition.MAX_TEXT);b.writeUtf(doorwayType,24);b.writeUtf(doorwayTarget,128);b.writeVarInt(generationCount);b.writeLong(firstGeneration);b.writeLong(lastGeneration);b.writeLong(discoveryOrder);b.writeUtf(context,256);}
        static Card decode(FriendlyByteBuf b){return new Card(b.readUtf(48),b.readUtf(80),b.readUtf(64),b.readUtf(16),b.readVarInt(),b.readUtf(16),b.readUtf(128),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readUtf(ThreadDefinition.MAX_TEXT),b.readUtf(ThreadDefinition.MAX_TEXT),b.readUtf(ThreadDefinition.MAX_TEXT),b.readUtf(24),b.readUtf(128),b.readVarInt(),b.readLong(),b.readLong(),b.readLong(),b.readUtf(256));}
    }
    public record Notice(NoticeKind kind,String id,String title,String topic,String aspect,String context,long generation){
        public Notice{Objects.requireNonNull(kind);ThreadPacketValidation.id(id);ThreadPacketValidation.title(title);ThreadTopic.parse(topic);if(!aspect.isEmpty())ThreadAspect.parse(aspect);if(context==null||context.length()>256||generation<0)throw new IllegalArgumentException("invalid notice context");}
        String identity(){return generation+":"+id;}
        void encode(FriendlyByteBuf b){b.writeEnum(kind);b.writeUtf(id,48);b.writeUtf(title,64);b.writeUtf(topic,16);b.writeUtf(aspect,16);b.writeUtf(context,256);b.writeLong(generation);}
        static Notice decode(FriendlyByteBuf b){return new Notice(b.readEnum(NoticeKind.class),b.readUtf(48),b.readUtf(64),b.readUtf(16),b.readUtf(16),b.readUtf(256),b.readLong());}
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
        private static void handle(ServerPlayer player,Action action){if(player==null)return;if(action.action.equals("open")){sync(player,true,List.of());return;}if(!ThreadDefinitions.INSTANCE.contains(action.thread))return;var state=ThreadPlayerState.get(player);if(!state.known.contains(action.thread))return;if(action.action.equals("read")){if(state.markRead(action.thread)){state.save(player);sync(player,false,List.of());}return;}if(!action.action.equals("issue")||state.unread.contains(action.thread))return;long now=System.currentTimeMillis(),previous=lastIssue.getOrDefault(player.getUUID(),0L);if(now-previous<1000)return;lastIssue.put(player.getUUID(),now);var stack=ThreadFacsimileItem.create(action.thread,player);if(!player.getInventory().add(stack)){player.displayClientMessage(Component.literal("Make room in your inventory for the card copy."),true);return;}player.containerMenu.broadcastChanges();}
    }
    private static void writeCards(FriendlyByteBuf b,List<Card>cards){if(cards.size()>52)throw new IllegalArgumentException("too many thread cards");b.writeVarInt(cards.size());cards.forEach(c->c.encode(b));}
    private static List<Card>readCards(FriendlyByteBuf b){int n=b.readVarInt();if(n<0||n>52)throw new IllegalArgumentException("invalid thread packet");var out=new ArrayList<Card>(n);var ids=new HashSet<String>();for(int i=0;i<n;i++){var card=Card.decode(b);if(!ids.add(card.id()))throw new IllegalArgumentException("duplicate thread card");out.add(card);}return List.copyOf(out);}
}
