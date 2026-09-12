package com.bettercontent.threads.mixin;
import com.bettercontent.threads.OperationOwners;
import com.bettercontent.threads.compat.OreProcessingEvidence;
import com.simibubi.create.content.fluids.spout.*;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(value=SpoutBlockEntity.class,remap=false)
public abstract class OreSpoutingOutcomeMixin {
 @Unique private boolean threads$rinsed;
 @Inject(method="whenItemHeld",at=@At("HEAD"),remap=false)
 private void threads$begin(TransportedItemStack input,TransportedItemStackHandlerBehaviour handler,CallbackInfoReturnable<BeltProcessingBehaviour.ProcessingResult> cir){threads$rinsed=false;}
 @Redirect(method="whenItemHeld",at=@At(value="INVOKE",target="Lcom/simibubi/create/content/fluids/spout/FillingBySpout;fillItem(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/item/ItemStack;Lnet/minecraftforge/fluids/FluidStack;)Lnet/minecraft/world/item/ItemStack;"),remap=false)
 private ItemStack threads$filled(Level level,int required,ItemStack input,FluidStack fluid){var before=input.copy();var output=FillingBySpout.fillItem(level,required,input,fluid);threads$rinsed=OreProcessingEvidence.rinsed(output)&&OreProcessingEvidence.consumed(before,input);return output;}
 @Inject(method="whenItemHeld",at=@At("RETURN"),remap=false)
 private void threads$finished(TransportedItemStack input,TransportedItemStackHandlerBehaviour handler,CallbackInfoReturnable<BeltProcessingBehaviour.ProcessingResult> cir){if(threads$rinsed){OperationOwners.completed((BlockEntity)(Object)this,"ore_rinse","completed","Spouting produced rinsed ore feed");threads$rinsed=false;}}
}
