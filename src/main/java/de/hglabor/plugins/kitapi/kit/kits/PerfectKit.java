package de.hglabor.plugins.kitapi.kit.kits;

import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.settings.IntArg;
import de.hglabor.plugins.kitapi.player.KitPlayer;
import de.hglabor.plugins.kitapi.pvp.SoupHealing;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.stream.IntStream;

public class PerfectKit extends AbstractKit implements Listener {
    public static final PerfectKit INSTANCE = new PerfectKit();
    private final String soupAmountKey;

    @IntArg
    private final int soupAmountForReward;

    private PerfectKit() {
        super("Perfect", Material.BEACON);
        soupAmountKey = this.getName() + "soupAmount";
        soupAmountForReward = 7;
    }

    // kitplayer.onSoupEat wär nice oder so ka das hier ist iwie scuffed
    @EventHandler
    public void onSoupEat(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitPlayer kitPlayer = KitApi.getInstance().getPlayer(player);
        if (!kitPlayer.hasKit(this)) return;
        if (event.getAction() == Action.LEFT_CLICK_AIR) return;
        if (event.getItem() == null) return;
        if (event.hasItem() && SoupHealing.SOUP_MATERIAL.contains(event.getMaterial())) {
            if (event.getHand() == EquipmentSlot.OFF_HAND) return;

            int heal = 7;
            if (event.getItem().getType() == Material.SUSPICIOUS_STEW) heal = SpitKit.INSTANCE.getSpitSoupHealing();

            boolean presouped = player.getHealth() + heal > player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();

            if (!presouped) {
                incrementStreak(kitPlayer);
                int soupAmount = kitPlayer.getKitAttributeOrDefault(soupAmountKey, 0);
                if (soupAmount % soupAmountForReward == 0) {
                    int soupsToBeAdded = (int) Math.round(Math.sqrt( (double) (soupAmount / soupAmountForReward) * 1.3)); // beim ersten mal 1 soup dann 2 dann 3x2 udn dann 3 wenn mann gott ist irgendwann vllt 4
                    for (int i = 0; i < soupsToBeAdded; i++) {
                        player.getInventory().addItem(new ItemStack(Material.MUSHROOM_STEW));
                    }
                    player.sendMessage(Localization.INSTANCE.getMessage("perfect.streak", ChatUtils.locale(player)));
                }
            } else if (kitPlayer.isInCombat()) { // only lose streak if in combat?
                resetStreak(kitPlayer);
            }
        }
    }

    private void incrementStreak(KitPlayer kitPlayer) {
        Player player = kitPlayer.getBukkitPlayer().get();
        int soupAmount = kitPlayer.getKitAttributeOrDefault(soupAmountKey, 0);
        kitPlayer.putKitAttribute(soupAmountKey, soupAmount + 1);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 100, 1);
    }

    private void resetStreak(KitPlayer kitPlayer) {
        Player player = kitPlayer.getBukkitPlayer().get();
        if (kitPlayer.getKitAttributeOrDefault(soupAmountKey, 0) > 0) player.playSound(player.getLocation(), Sound.ENTITY_DONKEY_DEATH,1,1);
        kitPlayer.putKitAttribute(soupAmountKey, 0);
    }


}
