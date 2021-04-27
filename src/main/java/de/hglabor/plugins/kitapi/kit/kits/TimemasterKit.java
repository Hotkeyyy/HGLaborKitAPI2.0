package de.hglabor.plugins.kitapi.kit.kits;

import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.events.KitEvent;
import de.hglabor.plugins.kitapi.kit.settings.FloatArg;
import de.hglabor.plugins.kitapi.kit.settings.IntArg;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Hotkeyyy
 * @since 26.04.2021
 */

public class TimemasterKit extends AbstractKit implements Listener {
    public static final TimemasterKit INSTANCE = new TimemasterKit();


    @IntArg
    private final int stunnTime;

    @FloatArg(min = 0.0F)
    private final float cooldown;

    @IntArg
    private final int radius;

    @IntArg
    private final int slownessTime;

    @IntArg(min = 1)
    private final int slownessAmplifier;

    private final HashSet<UUID> stunnedPlayers;

    private TimemasterKit() {
        super("Timemaster", Material.CLOCK);
        setMainKitItem(Material.CLOCK);
        cooldown = 40;
        stunnTime = 4;
        radius = 6;
        stunnedPlayers = new HashSet<>();
        slownessTime = 4;
        slownessAmplifier = 1;
    }

    @KitEvent
    @Override
    public void onPlayerRightClickKitItem(PlayerInteractEvent event) {
        stunnNearbyEntitys(event.getPlayer());
        KitApi.getInstance().getPlayer(event.getPlayer()).activateKitCooldown(this);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if ((event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getZ() != event.getTo().getZ() ||
                event.getTo().getY() > event.getFrom().getY()) &&
                stunnedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private void stunnNearbyEntitys(Player player) {
        List<Entity> nearbyPlayers = player.getNearbyEntities(radius, radius, radius)
                .stream()
                .filter(entity -> entity instanceof Player)
                .collect(Collectors.toList());
        nearbyPlayers.forEach(entity -> {
            stunnedPlayers.add(entity.getUniqueId());
            entity.sendMessage(Localization.t("timemaster.stunn", ChatUtils.locale(entity.getUniqueId())));
        });
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
                slownessTime * 20,
                slownessAmplifier - 1,
                true,
                false,
                true));
        Location loc = player.getLocation();
        for (double i = 0; i < 16; i++) {
            for (double y = 0.0; y < (Math.PI * 2); y += .1) {
                double x = radius * Math.cos(y);
                double z = radius * Math.sin(y);
                loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc.clone().add(x, i / 4, z), 0, 0, 0, 0, 5);
            }
        }

        Bukkit.getScheduler().runTaskLater(KitApi.getInstance().getPlugin(),
                () -> nearbyPlayers.forEach(entity -> stunnedPlayers.remove(entity.getUniqueId())),
                stunnTime * 20L);

    }

    @Override
    public float getCooldown() {
        return cooldown;
    }
}
