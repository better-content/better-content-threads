package com.bettercontent.threads.compat;

import com.bettercontent.threads.PackActionThreads.VesselObservation;
import net.minecraft.server.level.ServerPlayer;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/**
 * Direct, server-safe Valkyrien Skies boundary. Keeping foreign types in this
 * optional class prevents absent installations from linking them, while the
 * direct invocation avoids reflecting over client-only overload descriptors.
 */
public final class ValkyrienSkiesThreads {
    private ValkyrienSkiesThreads() {}

    public static VesselObservation observe(ServerPlayer player) {
        Ship ship = VSGameUtilsKt.getShipManaging(player);
        if (ship == null) return null;
        Vector3dc position = ship.getTransform().getPositionInWorld();
        return new VesselObservation(Long.toUnsignedString(ship.getId()), position.x(), position.z());
    }
}
