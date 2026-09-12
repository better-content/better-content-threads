package com.bettercontent.threads.compat;
import com.bettercontent.threads.ThreadSignals;
import it.hurts.sskirillss.relics.api.events.leveling.ExperienceAddEvent;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.*;
import java.util.*;
/** Experience's pre-mutation event is checked against the same physical relic after its native update. */
public final class RelicsThreads {
    private record Pending(ServerPlayer player, ItemStack stack, IRelicItem relic, int before) {}
    private static final List<Pending> PENDING = new ArrayList<>();
    private RelicsThreads() {}
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void relicExperience(ExperienceAddEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0 || !(event.getEntity() instanceof ServerPlayer player)
            || !(event.getStack().getItem() instanceof IRelicItem relic)) return;
        PENDING.add(new Pending(player, event.getStack(), relic, relic.getLevel(event.getStack())));
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (var pending : PENDING) if (pending.relic.getLevel(pending.stack) > pending.before)
            ThreadSignals.emit(pending.player, "relic_developed", pending.stack.getItem().toString(), UUID.randomUUID().toString());
        PENDING.clear();
    }
}
