package com.bettercontent.threads;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Personal, contextual cues for the pending-art teaching roster. */
public final class EarlyTeachingThreads {
    private static final String ROOT = "better_content_threads_early_teaching";
    private static final TagKey<net.minecraft.world.item.Item> FRUITS = TagKey.create(Registries.ITEM, new ResourceLocation("diet", "fruits"));
    private static final TagKey<net.minecraft.world.item.Item> VEGETABLES = TagKey.create(Registries.ITEM, new ResourceLocation("diet", "vegetables"));
    private static final Set<String> DRYABLE = Set.of("minecraft:beef", "minecraft:porkchop", "minecraft:chicken",
        "minecraft:mutton", "minecraft:rabbit", "minecraft:cod", "minecraft:salmon");
    private record FurnaceOperation(BlockPos position, ResourceLocation dimension, long tick) {}
    private static final Map<UUID, FurnaceOperation> FURNACES = new ConcurrentHashMap<>();

    private EarlyTeachingThreads() {}

    private static CompoundTag state(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(ROOT);
    }

    private static void save(ServerPlayer player, CompoundTag state) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(ROOT, state);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static void once(ServerPlayer player, String id, String type, String value) {
        if (!ThreadDefinitions.INSTANCE.contains(id) || !ThreadArt.BY_ID.containsKey(id)) return;
        CompoundTag data = state(player);
        if (data.getBoolean(id)) return;
        ThreadSignals.emit(player, type, value, player.getUUID() + ":" + id);
        data.putBoolean(id, true);
        save(player, data);
    }

    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !ThreadDefinitions.INSTANCE.contains("find_recipes_in_emi")) return;
        if (player.isSpectator()) {
            once(player, "choose_return_point", "first_spectator_spawn", "entered");
            return;
        }
        CompoundTag data = state(player);
        if (!data.getBoolean("find_recipes_in_emi")) {
            int activeTicks = data.getInt("activeTicks") + 1;
            data.putInt("activeTicks", activeTicks);
            save(player, data);
            if (activeTicks >= 600) once(player, "find_recipes_in_emi", "onboarding_elapsed", "active_30_seconds");
        }
        if (player.tickCount % 20 != 0) return;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            String item = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
            if (DRYABLE.contains(item)) once(player, "dry_food_for_journey", "dryable_food_acquired", "first");
            if (item.equals("tconstruct:pickaxe")) once(player, "find_dimensional_font", "first_tinkers_tool", "assembled");
            if (item.startsWith("tconstruct:") && stack.isDamageableItem() && stack.getDamageValue() * 5 >= stack.getMaxDamage() * 4)
                once(player, "maintain_tinkers_tool", "tinkers_tool_low", "first");
        }
        nearDanger(player);
        FurnaceOperation operation = FURNACES.get(player.getUUID());
        if (operation != null) {
            if (player.server.getTickCount() - operation.tick() > 200
                    || !player.serverLevel().dimension().location().equals(operation.dimension())) FURNACES.remove(player.getUUID());
            else {
                var block = player.serverLevel().getBlockState(operation.position());
                if (block.hasProperty(BlockStateProperties.LIT) && block.getValue(BlockStateProperties.LIT)) {
                    once(player, "lit_furnace_pollutes", "furnace_lit", "first");
                    FURNACES.remove(player.getUUID());
                }
            }
        }
    }

    private static void nearDanger(ServerPlayer player) {
        CompoundTag data = state(player);
        if (data.getBoolean("font_trip_time_budget") && data.getBoolean("hotstone_dangerous_ground")) return;
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -2, -4), center.offset(4, 2, 4))) {
            String block = ForgeRegistries.BLOCKS.getKey(player.serverLevel().getBlockState(pos).getBlock()).toString();
            if (block.equals("dimension_drink:dimensional_font"))
                once(player, "font_trip_time_budget", "font_approach", "first");
            if (block.equals("realistic_ores:hotstone") || block.equals("realistic_ores:deepslate_hotstone")
                    || (block.startsWith("excavated_variants:") && block.endsWith("_hotstone")))
                once(player, "hotstone_dangerous_ground", "hotstone_approach", "first");
        }
    }

    @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String item = ForgeRegistries.ITEMS.getKey(event.getCrafting().getItem()).toString();
        if (item.equals("tconstruct:hand_axe")) once(player, "start_tinkers_tools", "first_hand_axe_crafted", "completed");
    }

    @SubscribeEvent public static void ate(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack food = event.getItem();
        if (food.is(FRUITS)) once(player, "fruit_fuels_a_sprint", "first_food_category", "fruit");
        if (food.is(VEGETABLES)) once(player, "vegetables_buffer_exposure", "first_food_category", "vegetable");
    }

    @SubscribeEvent public static void boarded(EntityMountEvent event) {
        if (!event.isMounting() || !(event.getEntityMounting() instanceof ServerPlayer player)
                || !(event.getEntityBeingMounted() instanceof Boat boat)) return;
        ResourceLocation type = ForgeRegistries.ENTITY_TYPES.getKey(boat.getType());
        if (type != null && type.getNamespace().equals("minecraft"))
            once(player, "boats_break_into_parts", "vanilla_boat_boarded", "first");
    }

    @SubscribeEvent public static void placed(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String block = ForgeRegistries.BLOCKS.getKey(event.getPlacedBlock().getBlock()).toString();
        if (block.equals("create:millstone") || block.equals("create:mechanical_press"))
            once(player, "plan_sustained_rotation", "root_machine_placed", "first");
    }

    @SubscribeEvent public static void furnaceUsed(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String block = ForgeRegistries.BLOCKS.getKey(player.serverLevel().getBlockState(event.getPos()).getBlock()).toString();
        if (block.equals("tconstruct:melter") || block.equals("tconstruct:smeltery_controller"))
            once(player, "alloy_in_smeltery", "tinkers_melter_use", "first");
        if (block.equals("minecraft:furnace") || block.equals("minecraft:blast_furnace") || block.equals("minecraft:smoker"))
            FURNACES.put(player.getUUID(), new FurnaceOperation(event.getPos().immutable(),
                player.serverLevel().dimension().location(), player.server.getTickCount()));
    }

    @SubscribeEvent public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTo().location().toString().equals("rats:ratlantis"))
            once(player, "ratlantis_below_islands", "ratlantis_arrival", "first");
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        FURNACES.remove(event.getEntity().getUUID());
    }
}
