package de.hglabor.plugins.kitapi.kit.kits;

import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.events.KitEvent;
import de.hglabor.plugins.kitapi.kit.settings.DoubleArg;
import de.hglabor.plugins.kitapi.kit.settings.IntArg;
import de.hglabor.plugins.kitapi.kit.settings.MaterialArg;
import de.hglabor.plugins.kitapi.player.KitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

/**
 * @author Hotkeyyy
 * @since 2021/02/25
 */
public class TurtleKit extends AbstractKit {
    public static final TurtleKit INSTANCE = new TurtleKit();

    @MaterialArg
    private final Material material;

    @IntArg(min = 3)
    private final int size;

    @IntArg(min = 1)
    private final int inTurtleTime;

    private TurtleKit() {
        super("Turtle", Material.TURTLE_HELMET);
        setMainKitItem(Material.SCUTE);
        material = Material.GREEN_GLAZED_TERRACOTTA;
        size = 4;
        inTurtleTime = 5;
    }


    @KitEvent
    @Override
    public void onPlayerRightClickKitItem(PlayerInteractEvent event) {
            HashMap<Location, Material> wallBlocks = new HashMap<>();
            HashMap<Location, Material> oldBlocks = new HashMap<>();
            Location loc = event.getPlayer().getLocation().clone();
            for (int bx = 0; bx <= size; bx++) {
                for (int by = 0; by <= size; by++) {
                    for (int bz = 0; bz <= size; bz++) {
                        if ((by == 0 || by == size)) {
                            if (bx != 0 && bz != 0 && bx != size && bz != size) {
                                wallBlocks.put(loc.clone().add(bx - size / 2, by, bz - size / 2), loc.clone().add(bx - size / 2, by, bz - size / 2).getBlock().getType());
                            }
                        } else if (bx == 0 || bz == 0 || bx == size || bz == size) {
                            if ((bx == 0 && bz == 0) || (bx == size && bz == size) || (bx == 0 && bz == size) || (bz == 0 && bx == size)) {
                                oldBlocks.put(loc.clone().add(bx - size / 2, by, bz - size / 2), loc.clone().add(bx - size / 2, by, bz - size / 2).getBlock().getType());
                            } else {
                                wallBlocks.put(loc.clone().add(bx - size / 2, by, bz - size / 2), loc.clone().add(bx - size / 2, by, bz - size / 2).getBlock().getType());
                            }
                        } else {
                            oldBlocks.put(loc.clone().add(bx - size / 2, by, bz - size / 2), loc.clone().add(bx - size / 2, by, bz - size / 2).getBlock().getType());
                        }
                    }
                }
            }

            oldBlocks.forEach((location, mat) -> {
                location.getBlock().setType(Material.AIR);
            });

            wallBlocks.forEach((location, mat) -> {
                location.getBlock().setType(Material.AIR);
                location.getBlock().setType(material);

            });


            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 1, 1);
            event.getPlayer().setFallDistance(0);
            event.getPlayer().teleport(loc.add(0, 1, 0));
            event.getPlayer().setHealthScale(event.getPlayer().getMaxHealth());
            event.getPlayer().setFoodLevel(20);
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 3));
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 3));

            Bukkit.getScheduler().runTaskLater(KitApi.getInstance().getPlugin(), () -> {
                oldBlocks.forEach((location, material) -> {
                    location.getBlock().setType(material);
                });
                wallBlocks.forEach((location, material) -> {
                    location.getBlock().setType(material);
                });
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_SHULKER_OPEN, 1, 1);


            }, 20 * inTurtleTime);
    }
}
