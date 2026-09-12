package com.bettercontent.threads.compat.bettercontent;
import com.bettercontent.arcanechunkloaders.api.AnchorWorkApi;
import com.bettercontent.threads.ThreadSignals;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.UUID;
/** Ticket support is considered only after a native owned operation actually changes or finishes. */
public final class ArcaneChunkLoaderThreads {
 private ArcaneChunkLoaderThreads(){}
 public static void workCompleted(BlockEntity device,UUID owner,String operation,String context){
  if(owner==null||!(device.getLevel() instanceof ServerLevel level))return;
  var state=com.bettercontent.threads.ThreadPlayerState.get(level.getServer(),owner);
  if(state.generation==com.bettercontent.threads.ThreadPlayerState.currentGeneration(level.getServer())&&state.discovered.contains("anchored_work"))return;
  if(AnchorWorkApi.supportingAnchor(level,device.getBlockPos()).isPresent())ThreadSignals.emit(level.getServer(),owner,"anchor_work","remote_progress",operation,context);
 }
}
