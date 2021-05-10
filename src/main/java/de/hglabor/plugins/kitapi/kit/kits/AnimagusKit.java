package de.hglabor.plugins.kitapi.kit.kits;

import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.events.KitEvent;
import de.hglabor.plugins.kitapi.player.KitPlayer;
import de.hglabor.utils.noriskutils.ChatUtils;
import de.hglabor.utils.noriskutils.ItemBuilder;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static de.hglabor.utils.localization.Localization.t;

public class AnimagusKit extends AbstractKit implements Listener {
    public static final AnimagusKit INSTANCE = new AnimagusKit();

    private final HashMap<UUID, HashSet<EntityType>> receivedMobs = new HashMap<>();

    private final Component inventoryName;

    protected AnimagusKit() {
        super("Animagus", Material.ZOMBIE_SPAWN_EGG);
        setMainKitItem(Material.BOOK);
        inventoryName = Component.text("Mobs");
    }

    @KitEvent
    @Override
    public void onPlayerKillsLivingEntity(EntityDeathEvent event, Player killer, Entity entity) {
        if (!DisguiseType.getType(event.getEntityType()).isMob()) return;
        receivedMobs.computeIfAbsent(killer.getUniqueId(), k -> new HashSet<>());
        receivedMobs.get(killer.getUniqueId()).add(event.getEntityType());
    }

    @KitEvent
    @Override
    public void onPlayerRightClickKitItem(PlayerInteractEvent event) {
        event.getPlayer().openInventory(getMobInventory(event.getPlayer()));
    }

    private Inventory getMobInventory(Player player) {

        Inventory inventory = Bukkit.createInventory(null, 6 * 9, inventoryName);
        inventory.addItem(new ItemBuilder(Material.PAPER).setName(t("animagus.undisguise", ChatUtils.locale(player))).build());

        receivedMobs.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        for (EntityType entityType : receivedMobs.get(player.getUniqueId())) {
            if (!DisguiseType.getType(entityType).isMob() ||
                    entityType.equals(EntityType.ILLUSIONER) ||
                    entityType.equals(EntityType.ARMOR_STAND) ||
                    entityType.equals(EntityType.GIANT) ||
                    entityType.equals(EntityType.ENDER_DRAGON) ||
                    entityType.equals(EntityType.WITHER)
            )
                continue;

            String name = entityType.name();

            switch (entityType) {
                case MUSHROOM_COW: {
                    inventory.addItem(new ItemBuilder(Material.RED_MUSHROOM).setName(name).build());
                    continue;
                }
                case SNOWMAN: {
                    inventory.addItem(new ItemBuilder(Material.SNOWBALL).setName(name).build());
                    continue;
                }
                case IRON_GOLEM: {
                    inventory.addItem(new ItemBuilder(Material.IRON_INGOT).setName(name).build());
                    continue;
                }
            }
            inventory.addItem(new ItemBuilder(Material.valueOf(entityType.name() + "_SPAWN_EGG"))
                    .setName(name)
                    .build());
        }
        return inventory;
    }

    @Deprecated
    @EventHandler
    public void onClickMobInventory(InventoryClickEvent event) {
        if (!event.getView().title().equals(inventoryName)) return;
        event.setCancelled(true);
        if (event.getClick() == ClickType.UNKNOWN || event.getCurrentItem() == null) return;
        String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
        event.getWhoClicked().closeInventory();
        if (displayName.equals(t("animagus.undisguise", ChatUtils.locale(event.getWhoClicked())))) {
            DisguiseAPI.undisguiseToAll(event.getWhoClicked());
            return;
        }
        MobDisguise mobDisguise = new MobDisguise(DisguiseType.getType(EntityType.valueOf(displayName.toUpperCase())));
        DisguiseAPI.disguiseEntity(event.getWhoClicked(), mobDisguise);
    }

    @Override
    public void onKitPlayerDeath(PlayerDeathEvent event) {
        receivedMobs.get(event.getEntity().getUniqueId()).clear();
    }

    @Override
    public void onDeactivation(KitPlayer kitPlayer) {
        kitPlayer.getBukkitPlayer().ifPresent(DisguiseAPI::undisguiseToAll);
    }
}



