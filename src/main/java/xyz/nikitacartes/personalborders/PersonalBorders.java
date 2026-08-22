package xyz.nikitacartes.personalborders;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
//?} else {
/*import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
*///?}
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
//? if >=1.21.9 {
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.storage.LevelData;
//?}
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import xyz.nikitacartes.personalborders.listener.LuckPermsListener;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import java.util.*;

import static xyz.nikitacartes.personalborders.utils.PersonalBordersLogger.LogDebug;
import static xyz.nikitacartes.personalborders.utils.PersonalBordersLogger.LogInfo;

//? if fabric {
public class PersonalBorders implements ModInitializer {
//?} else {
/*@net.neoforged.fml.common.Mod("personal-borders")
public class PersonalBorders {
*///?}
    public static MinecraftServer server;
    public static LuckPerms luckPerms;

    public static Map<UUID, BorderCache> borders = new HashMap<>();

    // Border for vanilla lookups that carry no entity: DismountHelper.findSafeDismountLocation and
    // PlayerSpawnFinder.findSpawn (1.21.9+). Every caller sets it first. Server thread only.
    public static BorderCache pendingBorderCache;

    // Todo:Config
    // Teleport player nether portal: outside of border, or inside border
    // Teleport players in entity: together, or separate

    //? if fabric {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onStartServer);

        ServerPlayConnectionEvents.JOIN.register((netHandler, packetSender, server) -> onPlayerJoin(netHandler.getPlayer().getUUID()));
    }
    //?} else {
    /*public PersonalBorders() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> onStartServer(event.getServer()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> onPlayerJoin(event.getEntity().getUUID()));
    }
    *///?}

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

        WorldBorder defaultBorder = PersonalBorders.server.overworld().getWorldBorder();

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
        ServerPlayer player = PersonalBorders.server.getPlayerList().getPlayer(uuid);
        if (player == null) {
            return; // Player not online.
        }
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            return;
        }
        LogDebug("Updating border for player: " + player.getScoreboardName());

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
                //? if >=1.21.9 {
                .withContext("damage.buffer", Double.toString(defaultBorder.getSafeZone()))
                //?} else {
                /*.withContext("damage.buffer", Double.toString(defaultBorder.getDamageSafeZone()))
                *///?}
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

        if (entity instanceof Player) {
            return entity.getUUID();
        }

        UUID uuid = getPlayerPassengerUUID(entity);
        if (uuid != null) {
            return uuid;
        }

        // 1.21.6: Projectile.owner became an EntityReference.
        //? if >=1.21.6 {
        if (entity instanceof Projectile projectile) {
            if (projectile.owner == null) {
                return null;
            }
            return projectile.owner.getUUID();
        }
        //?} else {
        /*if (entity instanceof Projectile projectile) {
            return projectile.ownerUUID;
        }
        *///?}

        // 1.21.5: the owner UUID getter became EntityReference. AbstractHorse is OwnableEntity everywhere.
        //? if >=1.21.5 {
        if (entity instanceof OwnableEntity ownable) {
            if (ownable.getOwnerReference() == null) {
                return null;
            }
            return ownable.getOwnerReference().getUUID();
        }
        //?} else {
        /*if (entity instanceof OwnableEntity ownable) {
            return ownable.getOwnerUUID();
        }
        *///?}

        return null;
    }

    private static UUID getPlayerPassengerUUID(Entity entity) {
        if (!entity.isVehicle()) {
            return null;
        }

        for (Entity passenger : entity.getPassengers()) {
            if (passenger instanceof Player) {
                return passenger.getUUID();
            }

            UUID uuid = getPlayerPassengerUUID(passenger);
            if (uuid != null) {
                return uuid;
            }
        }

        return null;
    }

    // 1.21.9: the spawn BlockPos became a RespawnData record.
    //? if >=1.21.9 {
    public static LevelData.RespawnData getModifiedSpawnPoint(Level world, WorldBorder worldBorder, LevelData.RespawnData originalSpawnPos) {
        if (!worldBorder.isWithinBounds(originalSpawnPos.pos())) {
            BlockPos newBlockPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(worldBorder.getCenterX(), 0.0, worldBorder.getCenterZ()));
            return new LevelData.RespawnData(new GlobalPos(world.dimension(), newBlockPos), originalSpawnPos.yaw(), originalSpawnPos.pitch());
        }

        return originalSpawnPos;
    }
    //?} else {
    /*public static BlockPos getModifiedSpawnPoint(Level world, WorldBorder worldBorder, BlockPos originalSpawnPos) {
        if (!worldBorder.isWithinBounds(originalSpawnPos)) {
            return world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(worldBorder.getCenterX(), 0.0, worldBorder.getCenterZ()));
        }

        return originalSpawnPos;
    }
    *///?}
}
