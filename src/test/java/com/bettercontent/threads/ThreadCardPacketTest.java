package com.bettercontent.threads;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
final class ThreadCardPacketTest {
 private ThreadNetwork.Card card(boolean known,String event,String cause,String action){var d=ThreadArt.BY_ID.values().iterator().next();return new ThreadNetwork.Card(d.id(),d.conceptId(),d.title(),d.topic().id(),d.order(),"",d.art().toString(),known,known,known,event,cause,action,"","",2,1,3,9,"");}
 @Test void completeExplanationAndGenerationHistoryRoundTrip(){var c=card(true,"A machine finished.","It received power.","Collect its output.");var b=new FriendlyByteBuf(Unpooled.buffer());try{c.encode(b);assertEquals(c,ThreadNetwork.Card.decode(b));assertEquals(0,b.readableBytes());}finally{b.release();}}
 @Test void unknownCardsCannotLeakAnyTeachingPart(){assertThrows(IllegalArgumentException.class,()->card(false,"Event","",""));assertThrows(IllegalArgumentException.class,()->card(false,"","Cause",""));assertThrows(IllegalArgumentException.class,()->card(false,"","","Action"));}
 @Test void noticesCarryOutcomeContextAndGeneration(){var d=ThreadArt.BY_ID.values().iterator().next();var n=new ThreadNetwork.Notice(ThreadNetwork.NoticeKind.REMINDER,d.id(),d.title(),d.topic().id(),"","Iron ingot",3);var b=new FriendlyByteBuf(Unpooled.buffer());try{n.encode(b);assertEquals(n,ThreadNetwork.Notice.decode(b));}finally{b.release();}}
}
