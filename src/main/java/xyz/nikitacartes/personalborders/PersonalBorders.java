package xyz.nikitacartes.personalborders;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import xyz.nikitacartes.personalborders.listener.LuckPermsListener;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import java.util.*;

import static xyz.nikitacartes.personalborders.utils.PersonalBordersLogger.LogDebug;
import static xyz.nikitacartes.personalborders.utils.PersonalBordersLogger.LogInfo;

public class PersonalBorders implements ModInitializer {
    public static MinecraftServer server;
    public static LuckPerms luckPerms;

    public static Map<UUID, BorderCache> borders = new HashMap<>();

    // Todo:Config
    // Teleport player nether portal: outside of border, or inside border
    // Teleport players in entity: together, or separate

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onStartServer);

        ServerPlayConnectionEvents.JOIN.register((netHandler, packetSender, server) -> onPlayerJoin(netHandler.getPlayer().getUuid()));
        // ServerPlayConnectionEvents.DISCONNECT.register((netHandler, server) -> borders.remove(netHandler.getPlayer().getUuid()));
    }

    private void onStartServer(MinecraftServer server) {
        PersonalBorders.server = server;
        luckPerms = LuckPermsProvider.get();
        LuckPermsListener luckPermsListener = new LuckPermsListener(luckPerms);
        luckPermsListener.registerListeners();

        Group defaultGroup = luckPerms.getGroupManager().getGroup("default");
        if (defaultGroup == null) {
            return;
        }

        // Check time
        LogInfo("Loading borders for all users...");
        long startTime = System.currentTimeMillis();
        luckPerms.getUserManager().getUniqueUsers().join().forEach(user -> {
            borders.put(user, getOfflineBorderCache(user));
        });
        LogInfo("Loaded borders for all users in " + (System.currentTimeMillis() - startTime) + "ms");


        CachedPermissionData permissionData = defaultGroup.getCachedData().getPermissionData();
        permissionData.checkPermission("personal-borders");
        permissionData.checkPermission("personal-borders.overworld");
        permissionData.checkPermission("personal-borders.the_nether");
        permissionData.checkPermission("personal-borders.the_end");

        boolean hasBorder = defaultGroup.getNodes().stream().anyMatch(node -> node.getKey().startsWith("personal-borders"));
        if (hasBorder) {
            return;
        }

        WorldBorder defaultBorder = PersonalBorders.server.getOverworld().getWorldBorder();

        NodeMap data = defaultGroup.data();
        data.add(createNode("personal-borders", defaultBorder));
        data.add(createNode("personal-borders.overworld", defaultBorder));
        data.add(createNode("personal-borders.the_nether", defaultBorder));
        data.add(createNode("personal-borders.the_end", defaultBorder));

        luckPerms.getGroupManager().saveGroup(defaultGroup);
    }

    private void onPlayerJoin(UUID uuid) {
        User user = luckPerms.getUserManager().getUser(uuid);

        if (user == null) {
            return;
        }

        borders.put(uuid, getBorderCache(user.resolveInheritedNodes(QueryOptions.nonContextual())));
    }

    public static void updateForPlayer(UUID uuid) {
        ServerPlayerEntity player = PersonalBorders.server.getPlayerManager().getPlayer(uuid);
        if (player == null) {
            return; // Player not online.
        }
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            return;
        }
        LogDebug("Updating border for player: " + player.getNameForScoreboard());

        BorderCache borderCache = getBorderCache(user.resolveInheritedNodes(QueryOptions.nonContextual()));
        borders.put(uuid, borderCache);
        borderCache.sendBorder(player);
    }

    private static BorderCache getBorderCache(Collection<Node> nodes) {
        Node defaultBorderNode = null;
        Node overworldBorderNode = null;
        Node netherBorderNode = null;
        Node endBorderNode = null;

        for (Node node : nodes) {
            if (defaultBorderNode == null && node.getKey().equals("personal-borders")) {
                defaultBorderNode = node;
            } else if (overworldBorderNode == null && node.getKey().equals("personal-borders.overworld")) {
                overworldBorderNode = node;
            } else if (netherBorderNode == null && node.getKey().equals("personal-borders.the_nether")) {
                netherBorderNode = node;
            } else if (endBorderNode == null && node.getKey().equals("personal-borders.the_end")) {
                endBorderNode = node;
            }
            if (overworldBorderNode != null && netherBorderNode != null && endBorderNode != null) {
                break;
            }
        }

        return new BorderCache(defaultBorderNode, overworldBorderNode, netherBorderNode, endBorderNode);
    }

    private Node createNode(String nodeName, WorldBorder defaultBorder) {
        return Node.builder(nodeName)
                .value(false)
                .withContext("center.x", Integer.toString((int) defaultBorder.getCenterX()))
                .withContext("center.z", Integer.toString((int) defaultBorder.getCenterZ()))
                .withContext("distance", Integer.toString((int) defaultBorder.getSize()))
                .withContext("warning.distance", Integer.toString(defaultBorder.getWarningBlocks()))
                .withContext("warning.time", Integer.toString(defaultBorder.getWarningTime()))
                .withContext("damage.amount", Double.toString(defaultBorder.getDamagePerBlock()))
                .withContext("damage.buffer", Double.toString(defaultBorder.getSafeZone()))
                .build();
    }

    public static BorderCache getOfflineBorderCache(UUID uuid) {
        BorderCache borderCache = borders.get(uuid);
        if (borderCache != null) {
            return borderCache;
        }
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user != null) {
            return getBorderCache(user.resolveInheritedNodes(QueryOptions.nonContextual()));
        }

        LogDebug("Loading border for offline player: " + uuid);
        user = luckPerms.getUserManager().loadUser(uuid).join(); // ToDo: This is blocking, find a better way.
        if (user != null) {
            return getBorderCache(user.resolveInheritedNodes(QueryOptions.nonContextual()));
        }
        return null;
    }

    public static BorderCache getBorderCache(Entity entity) {
        UUID uuid = getCorrectUuid(entity);
        if (uuid == null) {
            return null;
        }
        BorderCache cache = borders.get(uuid);
        if (cache == null) {
            cache = getOfflineBorderCache(uuid);
        }
        return cache;
    }

    private static UUID getCorrectUuid(Entity entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof PlayerEntity) {
            return entity.getUuid();
        }

        UUID uuid = getPlayerPassengerUUID(entity);
        if (uuid != null) {
            return uuid;
        }

        if (entity instanceof ProjectileEntity projectileEntity) {
            return projectileEntity.ownerUuid;
        }

        if (entity instanceof HorseEntity horseEntity) {
            return horseEntity.getOwnerUuid();
        }

        if (entity instanceof Tameable tameable) {
            return tameable.getOwnerUuid();
        }

        return null;
    }

    private static UUID getPlayerPassengerUUID(Entity entity) {
        if (!entity.hasPassengers()) {
            return null;
        }

        for (Entity passenger : entity.getPassengerList()) {
            if (passenger instanceof PlayerEntity) {
                return passenger.getUuid();
            }

            UUID uuid = getPlayerPassengerUUID(passenger);
            if (uuid != null) {
                return uuid;
            }
        }

        return null;
    }

    public static BlockPos getModifiedSpawnPos(World world, WorldBorder worldBorder, BlockPos originalSpawnPos) {
        if (!worldBorder.contains(originalSpawnPos)) {
            return world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(worldBorder.getCenterX(), 0.0, worldBorder.getCenterZ()));
        }

        return originalSpawnPos;
    }
}