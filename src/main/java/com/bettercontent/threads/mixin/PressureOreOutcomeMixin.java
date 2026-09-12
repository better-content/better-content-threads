package com.bettercontent.threads.mixin;
import com.bettercontent.threads.OperationOwners;
import com.bettercontent.threads.compat.OreProcessingEvidence;
import me.desht.pneumaticcraft.common.block.entity.PressureChamberValveBlockEntity;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.*;
@Pseudo
@Mixin(value=PressureChamberValveBlockEntity.class,remap=false)
public abstract class PressureOreOutcomeMixin {
 @Unique private boolean threads$prepared;
 @Inject(method="processApplicableRecipes",at=@At("HEAD"),remap=false)
 private void threads$begin(CallbackInfo ci){threads$prepared=false;}
 @Redirect(method="processApplicableRecipes",at=@At(value="INVOKE",target="Lme/desht/pneumaticcraft/api/crafting/recipe/PressureChamberRecipe;craftRecipe(Lnet/minecraftforge/items/IItemHandler;Lit/unimi/dsi/fastutil/ints/IntList;Z)Lnet/minecraft/core/NonNullList;"),remap=false)
 private NonNullList<ItemStack> threads$crafted(PressureChamberRecipe recipe,IItemHandler inventory,IntList slots,boolean simulate){
  var before=new HashMap<Integer,ItemStack>();if(!simulate)for(int slot:slots)if(slot>=0&&slot<inventory.getSlots())before.put(slot,inventory.getStackInSlot(slot).copy());
  var output=recipe.craftRecipe(inventory,slots,simulate);
  if(!simulate)threads$prepared=before.entrySet().stream().anyMatch(e->OreProcessingEvidence.consumed(e.getValue(),inventory.getStackInSlot(e.getKey()))&&(OreProcessingEvidence.prepared(e.getValue())||OreProcessingEvidence.nativeRecipe(recipe.getId())));
  return output;
 }
 @Inject(method="giveOutput",at=@At("RETURN"),remap=false)
 private void threads$accepted(NonNullList<ItemStack> output,boolean simulate,CallbackInfoReturnable<Boolean> cir){if(!simulate){if(threads$prepared&&cir.getReturnValueZ()&&output.stream().anyMatch(s->!s.isEmpty()))OperationOwners.completed((BlockEntity)(Object)this,"pressure_complete","prepared_ore","Prepared ore became pressure-refined products");threads$prepared=false;}}
}
