package com.bettercontent.threads.compat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
/** The installed geology vocabulary, including native family tags and canonical feed IDs. */
public final class OreProcessingEvidence {
 private OreProcessingEvidence(){}
 public static boolean nativeRecipe(ResourceLocation id){return id!=null&&id.getNamespace().equals("realistic_ores");}
 public static boolean crushed(ItemStack stack){return feed(stack,"crushed_feeds","crushed_");}
 public static boolean rinsed(ItemStack stack){return feed(stack,"rinsed_feeds","rinsed_");}
 public static boolean prepared(ItemStack stack){return crushed(stack)||rinsed(stack);}
 private static boolean feed(ItemStack stack,String tag,String prefix){
  if(stack.isEmpty())return false;
  if(stack.getTags().anyMatch(t->t.location().getNamespace().equals("realistic_ores")&&(t.location().getPath().equals(tag)||t.location().getPath().startsWith(tag+"/"))))return true;
  var id=ForgeRegistries.ITEMS.getKey(stack.getItem());return id!=null&&id.getNamespace().equals("realistic_ores")&&id.getPath().startsWith(prefix);
 }
 public static boolean consumed(ItemStack before,ItemStack after){return !before.isEmpty()&&(!ItemStack.isSameItemSameTags(before,after)||after.getCount()<before.getCount());}
}
