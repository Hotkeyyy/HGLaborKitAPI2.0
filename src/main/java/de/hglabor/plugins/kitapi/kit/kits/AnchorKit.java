package de.hglabor.plugins.kitapi.kit.kits;

import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.events.KitEvent;
import de.hglabor.plugins.kitapi.kit.settings.SoundArg;
import de.hglabor.plugins.kitapi.player.KitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * @author Hotkeyyy
 * @since 2021/02/25
 */
public class AnchorKit extends AbstractKit {
    public static final AnchorKit INSTANCE = new AnchorKit();
    @SoundArg
    private final Sound hitSound;

    private AnchorKit() {
        super("Anchor", Material.ANVIL);
        hitSound = Sound.BLOCK_ANVIL_HIT;
    }

    @Override
    public void onDeactivation(KitPlayer kitPlayer) {
        kitPlayer.getBukkitPlayer().ifPresent(this::resetKnockbackAttribute);
    }

    @Override
    public void onEnable(KitPlayer kitPlayer) {
        kitPlayer.getBukkitPlayer().ifPresent(this::setKnockbackAttribute);
    }

    @KitEvent
    @Override
    public void onPlayerAttacksLivingEntity(EntityDamageByEntityEvent event, KitPlayer attacker, LivingEntity entity) {
        if (entity instanceof Player) {
            ((Player) entity).playSound(entity.getLocation(), hitSound, 1, 1);
        }
        attacker.getBukkitPlayer().ifPresent(player -> player.playSound(player.getLocation(), hitSound, 1, 1));
        setKnockbackAttribute(entity);
        Bukkit.getScheduler().runTaskLater(KitApi.getInstance().getPlugin(), () -> resetKnockbackAttribute(entity), 1);
    }

    private void setKnockbackAttribute(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (attribute != null) {
            attribute.setBaseValue(1.0);
        }
    }

    private void resetKnockbackAttribute(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (attribute != null) {
            attribute.setBaseValue(0.0);
        }
    }
}

