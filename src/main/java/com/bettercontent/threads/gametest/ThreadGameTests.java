package com.bettercontent.threads.gametest;
import com.bettercontent.threads.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("better_content_threads")
@PrefixGameTestTemplate(false)
public final class ThreadGameTests {
 private static final String THREAD="surveyed_deposit",EPISODE="gametest:deposit-a";
 @GameTest(template="empty") public static void committed_outcome_immediately_discovers(GameTestHelper h){withPlayer(h,p->{ThreadSignals.emit(p,"deposit_extract","gametest-deposit",EPISODE);var s=ThreadPlayerState.get(p);h.assertTrue(s.known.contains(THREAD)&&s.unread.contains(THREAD)&&s.discovered.contains(THREAD),"One committed extraction must immediately teach the complete card");h.assertTrue(s.generationCounts.getOrDefault(THREAD,0)==1,"Discovery must credit once");});}
 @GameTest(template="empty") public static void setup_and_invalid_evidence_do_not_discover(GameTestHelper h){withPlayer(h,p->{ThreadSignals.emit(p,"deposit_read","gametest-deposit",EPISODE);ThreadSignals.emit(p,"deposit_extract","gametest-deposit",null);h.assertFalse(ThreadPlayerState.get(p).known.contains(THREAD),"Setup and uncorrelated claims cannot discover");});}
 @GameTest(template="empty") public static void duplicate_delivery_counts_once(GameTestHelper h){withPlayer(h,p->{ThreadSignals.emit(p,"deposit_extract","gametest-deposit",EPISODE);ThreadSignals.emit(p,"deposit_extract","gametest-deposit",EPISODE);ThreadSignals.emit(p,"deposit_extract","gametest-deposit","gametest:deposit-b");h.assertTrue(ThreadPlayerState.get(p).generationCounts.getOrDefault(THREAD,0)==1,"Repeated operations in one generation must not inflate history");});}
 @GameTest(template="empty") public static void offline_success_survives_reload(GameTestHelper h){withPlayer(h,p->{ThreadSignals.emit(p.server,p.getUUID(),"deposit_extract","gametest-deposit",EPISODE,"Survey matched");var s=ThreadPlayerState.get(p);h.assertTrue(s.pendingNotices.contains(THREAD),"Offline owner notice must be durable");s.markRead(THREAD);s.save(p);ThreadPlayerState.forget(p);var restored=ThreadPlayerState.get(p);h.assertTrue(restored.known.contains(THREAD)&&!restored.unread.contains(THREAD)&&restored.pendingNotices.contains(THREAD),"Saved history, reading and pending notice must survive cache eviction");h.assertTrue("Survey matched".equals(restored.contexts.get(THREAD)),"Outcome context must survive");});}
 private static void withPlayer(GameTestHelper h,Consumer<ServerPlayer> scenario){h.assertTrue(ThreadDefinitions.INSTANCE.contains(THREAD),"Production v4 catalogue must load");var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"thread-test"));try{scenario.accept(p);h.succeed();}finally{ThreadPlayerState.forget(p);}}
}
