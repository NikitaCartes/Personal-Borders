package xyz.nikitacartes.personalborders.listener;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;

import java.util.Collection;
import java.util.UUID;

import static xyz.nikitacartes.personalborders.PersonalBorders.borders;
import static xyz.nikitacartes.personalborders.PersonalBorders.updateForPlayer;

public class LuckPermsListener {
    private final LuckPerms luckPerms;

    public LuckPermsListener(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    public void registerListeners() {
        EventBus eventBus = this.luckPerms.getEventBus();
        eventBus.subscribe(NodeAddEvent.class, this::onNodeAdd);
        eventBus.subscribe(NodeRemoveEvent.class, this::onNodeRemove);
        eventBus.subscribe(NodeClearEvent.class, this::onNodeClear);
    }

    private void onNodeAdd(NodeAddEvent e) {
        Node node = e.getNode();
        if (!node.getKey().startsWith("personal-borders") && !node.getKey().startsWith("group.")) {
            return;
        }
        if (e.isUser()) {
            updateForTarget((User) e.getTarget());
        } else if (e.isGroup()) {
            updateForTarget((Group) e.getTarget());
        }
    }

    private void onNodeRemove(NodeRemoveEvent e) {
        Node node = e.getNode();
        if (!node.getKey().startsWith("personal-borders") && !node.getKey().startsWith("group.")) {
            return;
        }

        if (e.isUser()) {
            updateForTarget((User) e.getTarget());
        } else if (e.isGroup()) {
            updateForTarget((Group) e.getTarget());
        }
    }

    private void onNodeClear(NodeClearEvent e) {
        for (Node node : e.getNodes()) {
            if (node.getKey().startsWith("personal-borders") && !node.getKey().startsWith("group.")) {
                if (e.isUser()) {
                    updateForTarget((User) e.getTarget());
                } else if (e.isGroup()) {
                    updateForTarget((Group) e.getTarget());
                }
            }
        }
    }

    private void updateForTarget(User target) {
        updateForPlayer(target.getUniqueId());
    }

    private void updateForTarget(Group group) {
        // This doesn't work, but I don't know how to fix it
        /*
        NodeMatcher<InheritanceNode> matcher = NodeMatcher.key(InheritanceNode.builder(group).build());

        // Search all users for a match
        this.luckPerms.getUserManager().searchAll(matcher).thenAccept((Map<UUID, Collection<InheritanceNode>> map) -> {
            updateWorldBorder(map.keySet());
        });
         */
        updateWorldBorder(borders.keySet());
    }

    private void updateWorldBorder(Collection<UUID> uuids) {
        for (UUID uuid : uuids) {
            updateForPlayer(uuid);
        }
    }
}
