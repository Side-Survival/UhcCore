package com.gmail.val59000mc.game.handlers;

import com.comphenix.packetwrapper.WrapperPlayServerAnimation;
import com.comphenix.packetwrapper.WrapperPlayServerMount;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.gmail.val59000mc.UhcCore;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Packet freezing partially by https://github.com/BetonQuest/BetonQuest

public class FreezeHandler {

    private static FreezeHandler instance = new FreezeHandler();
    private final UhcCore plugin = UhcCore.getPlugin();

    public static FreezeHandler get() {
        return instance;
    }

    private Map<UUID, ArmorStand> stands = new HashMap<>();
    private Map<UUID, PacketAdapter> packetAdapters = new HashMap<>();

    public void freeze(Player player) {
        // Create something painful looking for the player to sit on and make it invisible.
        ArmorStand stand = player.getWorld().spawn(player.getLocation().clone().add(0, -1.1, 0), ArmorStand.class);

        stand.setGravity(false);
        stand.setVisible(false);

        stands.put(player.getUniqueId(), stand);

        // Mount the player to it using packets
        final WrapperPlayServerMount mount = new WrapperPlayServerMount();
        mount.setEntityID(stand.getEntityId());
        mount.setPassengerIds(new int[]{player.getEntityId()});

        // Send Packets
        mount.sendPacket(player);

        // Display Actionbar to hide the dismount message
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(" "));

        // Intercept Packets
        PacketAdapter packetAdapter = getPacketAdapter(player);
        packetAdapters.put(player.getUniqueId(), packetAdapter);
        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
    }

    private PacketAdapter getPacketAdapter(Player player) {
        return new PacketAdapter(plugin, ListenerPriority.HIGHEST,
                PacketType.Play.Client.STEER_VEHICLE,
                PacketType.Play.Server.ANIMATION
        ) {

            @Override
            public void onPacketSending(final PacketEvent event) {
                if (!event.getPacketType().equals(PacketType.Play.Server.ANIMATION)) {
                    return;
                }
                final WrapperPlayServerAnimation animation = new WrapperPlayServerAnimation(event.getPacket());
                if (animation.getEntityID() == player.getEntityId()) {
                    event.setCancelled(true);
                }
            }

            @Override
            public void onPacketReceiving(final PacketEvent event) {
                if (!event.getPlayer().equals(player)) {
                    return;
                }
                if (!event.getPacketType().equals(PacketType.Play.Client.STEER_VEHICLE)) {
                    return;
                }
                event.setCancelled(true);
            }
        };
    }

    public void unfreeze(Player player) {
        if (packetAdapters.get(player.getUniqueId()) != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapters.remove(player.getUniqueId()));
        }
        if (stands.get(player.getUniqueId()) != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                stands.remove(player.getUniqueId()).remove();
            });
        }
    }

    public boolean isFrozen(Player player) {
        return packetAdapters.containsKey(player.getUniqueId());
    }

    public void removeAllStands() {
        for (ArmorStand stand : stands.values()) {
            stand.remove();
        }
    }
}
