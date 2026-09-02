package com.bettercontent.threads.mixin;

import java.util.List;
import java.util.Set;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class BetterContentThreadsMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(final String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        final LoadingModList mods = FMLLoader.getLoadingModList();
        if (mixinClassName.endsWith("Ae2CraftingCpuMixin")) return isLoaded(mods, "ae2");
        if (mixinClassName.endsWith("ArsSourceEnergyMixin")) return isLoaded(mods, "arseng");
        if (mixinClassName.endsWith("ArsUpdateCasterMixin")) return isLoaded(mods, "ars_nouveau");
        if (mixinClassName.endsWith("OccultismRitualBowlMixin")) return isLoaded(mods, "occultism");
        if (mixinClassName.endsWith("TinkerAlloyMixin")
                || mixinClassName.endsWith("TinkerRepairResultMixin")) {
            return isLoaded(mods, "tconstruct");
        }
        return true;
    }

    private static boolean isLoaded(final LoadingModList mods, final String modId) {
        return mods != null && mods.getModFileById(modId) != null;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass,
                         final String mixinClassName, final IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass,
                          final String mixinClassName, final IMixinInfo mixinInfo) {
    }
}
