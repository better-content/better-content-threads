package com.bettercontent.threads.mixin;
import com.bettercontent.threads.OperationOwners;
import com.bettercontent.threads.compat.OreProcessingEvidence;
import com.oierbravo.createsifter.content.contraptions.components.sifter.SifterBlockEntity;
import com.oierbravo.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(value=SifterBlockEntity.class,remap=false)
public abstract class GeologySifterOutcomeMixin {
 @Shadow public ItemStackHandler inputInv;
 @Shadow private SiftingRecipe lastRecipe;
 @Unique private boolean threads$geology;
 @Inject(method="processCycle",at=@At("HEAD"),remap=false)
 private void threads$feed(CallbackInfo ci){var input=inputInv.getStackInSlot(0);threads$geology=!input.isEmpty()&&(OreProcessingEvidence.crushed(input)||(lastRecipe!=null&&OreProcessingEvidence.nativeRecipe(lastRecipe.getId())));}
 @Redirect(method="tryToInsertOutputItem",at=@At(value="INVOKE",target="Lnet/minecraftforge/items/ItemHandlerHelper;insertItemStacked(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;"),remap=false)
 private ItemStack threads$accepted(IItemHandler inventory,ItemStack output,boolean simulate){
  int offered=output.getCount();var remainder=ItemHandlerHelper.insertItemStacked(inventory,output,simulate);
  if(threads$geology&&!simulate&&offered>remainder.getCount()){OperationOwners.completed((BlockEntity)(Object)this,"geology_sift","completed","Crushed geology separated into mineral products");threads$geology=false;}
  return remainder;
 }
 @Inject(method="processCycle",at=@At("RETURN"),remap=false)
 private void threads$clear(CallbackInfo ci){threads$geology=false;}
}
