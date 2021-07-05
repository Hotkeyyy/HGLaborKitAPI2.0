package de.hglabor.plugins.kitapi.kit.kits;

import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.events.KitEvent;
import de.hglabor.plugins.kitapi.kit.items.KitItemBuilder;
import de.hglabor.plugins.kitapi.kit.settings.DoubleArg;
import de.hglabor.plugins.kitapi.kit.settings.FloatArg;
import de.hglabor.plugins.kitapi.kit.settings.IntArg;
import de.hglabor.plugins.kitapi.player.KitPlayer;
import de.hglabor.plugins.kitapi.util.Utils;
import de.hglabor.plugins.kitapi.util.pathfinder.LaborPathfinderFindTarget;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftSlime;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SlimeKit extends AbstractKit implements Listener {
    public static final SlimeKit INSTANCE = new SlimeKit();
    @IntArg
    private final int slimeDurationInSeconds;
    @IntArg
    private final int movementTick;
    @IntArg
    private final int slimeAmount;
    @IntArg
    private final int slimeSize;
    @FloatArg(
            min = 0.0F
    )
    private final float cooldown;
    private final String slimeTrailKey;
    private final String isSlimeBlockKey;

    private SlimeKit() {
        super("Slime", Material.SLIME_BLOCK);
        this.mainKitItem = (new KitItemBuilder(Material.SLIME_BALL)).build();
        this.slimeDurationInSeconds = 5;
        this.cooldown = 30.0F;
        this.movementTick = 1;
        this.slimeAmount = 3;
        this.slimeTrailKey = this.getName() + "slimeTrailKey";
        this.isSlimeBlockKey = this.getName() + "SlimeBlockKey";
        this.slimeSize = 2;
    }

    public void onDisable(KitPlayer kitPlayer) {
        SlimeTrail slimeTrail = kitPlayer.getKitAttribute(this.slimeTrailKey);
        if (slimeTrail != null) {
            slimeTrail.stop();
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().hasMetadata(this.isSlimeBlockKey)) {
            event.setCancelled(true);
        }

    }

    @KitEvent
    public void onPlayerRightClickPlayerWithKitItem(PlayerInteractAtEntityEvent event, KitPlayer kitPlayer, Player rightClicked) {
        kitPlayer.activateKitCooldown(this);
        SlimeTrail slimeTrail = new SlimeTrail(INSTANCE, rightClicked);
        slimeTrail.runTaskTimer(KitApi.getInstance().getPlugin(), 0L, (long)this.movementTick);
        kitPlayer.putKitAttribute(this.slimeTrailKey, slimeTrail);
    }

    public String getIsSlimeBlockKey() {
        return this.isSlimeBlockKey;
    }

    public float getCooldown() {
        return this.cooldown;
    }

    public final class SlimeTrail extends BukkitRunnable {
        private final Player player;
        private final KitPlayer kitPlayer;
        private final BlockFace[] directions;
        private final long endTime;
        private final Map<BlockFace, Block> currentSlimeBlocks;
        private final Map<BlockFace, BlockData> oldFaceBlockData;
        private final Map<Block, BlockData> oldBlockData;
        private final List<Slime> slimes;
        private final SlimeKit var0;

        private SlimeTrail(SlimeKit var1, Player player) {
            this.var0 = var1;
            this.player = player;
            this.oldBlockData = new HashMap();
            this.kitPlayer = KitApi.getInstance().getPlayer(player);
            this.endTime = System.currentTimeMillis() + (long)var1.slimeDurationInSeconds * 1000L;
            this.directions = new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.SELF, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_WEST, BlockFace.SOUTH_EAST};
            this.slimes = new ArrayList();
            this.currentSlimeBlocks = new HashMap();
            this.oldFaceBlockData = new HashMap();

            for(int i = 0; i < var1.slimeAmount; ++i) {
                Slime slime = (Slime)player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.SLIME);
                slime.setSize(slimeSize);
                this.slimes.add(slime);
            }

            this.makeSlimesAngry();
        }

        public void run() {
            if (!this.player.isDead() && this.kitPlayer.isValid()) {
                if (System.currentTimeMillis() > this.endTime) {
                    this.stop();
                } else {
                    this.makeSlimesAngry();
                    Block block = this.player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                    if (!this.player.getLocation().getBlock().getType().equals(Material.SLIME_BLOCK)) {
                        BlockFace[] var2 = this.directions;
                        int var3 = var2.length;

                        for(int var4 = 0; var4 < var3; ++var4) {
                            BlockFace direction = var2[var4];
                            Block relative = block.getRelative(direction);
                            if (relative.getType().isSolid() && !relative.getType().equals(Material.SLIME_BLOCK) && !Utils.isUnbreakableLaborBlock(relative)) {
                                if (this.currentSlimeBlocks.containsKey(direction)) {
                                    Block toReplace = (Block)this.currentSlimeBlocks.get(direction);
                                    BlockData blockData = (BlockData)this.oldFaceBlockData.getOrDefault(direction, Material.DIAMOND_BLOCK.createBlockData());
                                    if (blockData != null) {
                                        toReplace.setBlockData(blockData);
                                        toReplace.removeMetadata(this.var0.isSlimeBlockKey, KitApi.getInstance().getPlugin());
                                        this.oldBlockData.remove(toReplace);
                                    }
                                }

                                BlockData clone = relative.getBlockData().clone();
                                this.oldBlockData.put(relative, clone);
                                relative.setMetadata(this.var0.isSlimeBlockKey, new FixedMetadataValue(KitApi.getInstance().getPlugin(), ""));
                                relative.setType(Material.SLIME_BLOCK);
                                this.currentSlimeBlocks.put(direction, relative);
                                this.oldFaceBlockData.put(direction, clone);
                            }
                        }

                    }
                }
            } else {
                this.stop();
            }
        }

        private void makeSlimesAngry() {
            Iterator var1 = this.slimes.iterator();
            while(var1.hasNext()) {
                Slime slime = (Slime)var1.next();
                slime.setTarget(this.player);
            }

        }

        private void stop() {
            this.cancel();
            this.oldBlockData.keySet().forEach((block) -> {
                block.setBlockData((BlockData)this.oldBlockData.get(block));
                block.removeMetadata(this.var0.isSlimeBlockKey, KitApi.getInstance().getPlugin());
            });
            this.slimes.forEach(Entity::remove);
        }
    }
}
