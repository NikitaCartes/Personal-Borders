package xyz.nikitacartes.personalborders.utils;

import net.luckperms.api.node.Node;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;

import java.util.Map;
import java.util.Set;

import static xyz.nikitacartes.personalborders.PersonalBorders.server;
import static xyz.nikitacartes.personalborders.utils.PersonalBordersLogger.LogDebug;

public class BorderCache {

    private final WorldBorder overworldBorder;
    private final WorldBorder netherBorder;
    private final WorldBorder endBorder;

    public BorderCache(Node defaultBorderNote, Node overworldBorderNode, Node netherBorderNode, Node endBorderNode) {
        WorldBorder defaultBorder = getBorderFromNode(defaultBorderNote, server.overworld().getWorldBorder(), 1);

        this.overworldBorder = getBorderFromNode(overworldBorderNode, defaultBorder, 1);
        this.netherBorder = getBorderFromNode(netherBorderNode, defaultBorder, 8);
        this.endBorder = getBorderFromNode(endBorderNode, defaultBorder,1);
    }

    public WorldBorder getOverworldBorder() {
        return this.overworldBorder;
    }

    public WorldBorder getNetherBorder() {
        return this.netherBorder;
    }

    public WorldBorder getEndBorder() {
        return this.endBorder;
    }

    public void sendOverworldBorder(ServerGamePacketListenerImpl netHandler) {
        netHandler.send(new ClientboundInitializeBorderPacket(this.overworldBorder));
    }

    public void sendNetherBorder(ServerGamePacketListenerImpl netHandler) {
        netHandler.send(new ClientboundInitializeBorderPacket(this.netherBorder));
    }

    public void sendEndBorder(ServerGamePacketListenerImpl netHandler) {
        netHandler.send(new ClientboundInitializeBorderPacket(this.endBorder));
    }

    public void sendBorder(ServerPlayer player) {
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            sendOverworldBorder(player.connection);
            LogDebug("Sent overworld border to " + player.getScoreboardName());
            LogDebug("{x: " + this.overworldBorder.getCenterX() + ", z: " + this.overworldBorder.getCenterZ() + ", distance: " + this.overworldBorder.getSize() + "}");
        } else if (player.level().dimension().equals(Level.NETHER)) {
            sendNetherBorder(player.connection);
            LogDebug("Sent nether border to " + player.getScoreboardName());
            LogDebug("{x: " + this.netherBorder.getCenterX() + ", z: " + this.netherBorder.getCenterZ() + ", distance: " + this.netherBorder.getSize() + "}");
        } else if (player.level().dimension().equals(Level.END)) {
            sendEndBorder(player.connection);
            LogDebug("Sent end border to " + player.getScoreboardName());
            LogDebug("{x: " + this.endBorder.getCenterX() + ", z: " + this.endBorder.getCenterZ() + ", distance: " + this.endBorder.getSize() + "}");
        }
    }

    private WorldBorder getBorderFromNode(Node node, WorldBorder defaultBorder, double coordinateScale) {
        if (node == null) {
            return defaultBorder;
        }

        if (!node.getValue()) {
            return defaultBorder;
        }

        Map<String, Set<String>> context = node.getContexts().toMap();
        double centerX = Double.parseDouble(context.getOrDefault("center.x", Set.of(Double.toString(defaultBorder.getCenterX()))).iterator().next()) * coordinateScale;
        double centerZ = Double.parseDouble(context.getOrDefault("center.z", Set.of(Double.toString(defaultBorder.getCenterZ()))).iterator().next()) * coordinateScale;
        double distance = Double.parseDouble(context.getOrDefault("distance", Set.of(Double.toString(defaultBorder.getSize()))).iterator().next());
        int warningBlocks = Integer.parseInt(context.getOrDefault("warning.distance", Set.of(Integer.toString(defaultBorder.getWarningBlocks()))).iterator().next());
        int warningTime = Integer.parseInt(context.getOrDefault("warning.time", Set.of(Integer.toString(defaultBorder.getWarningTime()))).iterator().next());
        double damagePerBlock = Double.parseDouble(context.getOrDefault("damage.amount", Set.of(Double.toString(defaultBorder.getDamagePerBlock()))).iterator().next());
        //? if >=1.21.9 {
        double safeZone = Double.parseDouble(context.getOrDefault("damage.buffer", Set.of(Double.toString(defaultBorder.getSafeZone()))).iterator().next());
        //?} else {
        /*double safeZone = Double.parseDouble(context.getOrDefault("damage.buffer", Set.of(Double.toString(defaultBorder.getDamageSafeZone()))).iterator().next());
        *///?}

        WorldBorder border = new WorldBorder();
        border.setCenter(centerX, centerZ);
        border.setDamagePerBlock(damagePerBlock);
        //? if >=1.21.9 {
        border.setSafeZone(safeZone);
        //?} else {
        /*border.setDamageSafeZone(safeZone);
        *///?}
        border.setWarningBlocks(warningBlocks);
        border.setWarningTime(warningTime);
        border.setSize(distance);
        border.setAbsoluteMaxSize(server.getAbsoluteMaxWorldSize());

        return border;
    }

    public WorldBorder getWorldBorder(Level world) {
        if (world.dimension().equals(Level.OVERWORLD)) {
            return this.overworldBorder;
        } else if (world.dimension().equals(Level.NETHER)) {
            return this.netherBorder;
        } else if (world.dimension().equals(Level.END)) {
            return this.endBorder;
        }
        return world.getWorldBorder();
    }
}
