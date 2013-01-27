package com.zmanww.bukkit.SnowControl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author zwollner
 * 
 */
public class PlayerListener implements Listener {

	private static SnowControl plugin;

	public PlayerListener(SnowControl instance) {
		plugin = instance;
	}

	@EventHandler()
	public void blockBreak(BlockBreakEvent event) {
		final Block block = event.getBlock();
		if (block.getType() == Material.ICE) {
			if (event.getPlayer().getItemInHand().getType() == Material.STONE_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.WOOD_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.IRON_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.GOLD_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE) {
				block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.ICE, 1));
			}
		}
		final Location loc = event.getBlock().getLocation();
		if (block.getRelative(BlockFace.UP).getType() == Material.SNOW) {
			/*
			 * If block above broken is snow, then remove the next replaceable block under it so that the falling snow
			 * lands properly.
			 */
			SnowManager.removeReplaceableUnder(block);
			final byte above = block.getRelative(BlockFace.UP).getData();// get level of snow about to fall
			final byte under = SnowManager.getSnowLevelUnder(block);// get level of snow where it will land
			final byte newUnder = (byte) (above + under + 1 > 7 ? 7 : above + under + 1);
			loc.getWorld().spawnFallingBlock(loc, Material.SNOW, newUnder);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockDamage(BlockDamageEvent event) {
		if (Config.getInstance().debugEnabled()) {
			if (event.getItemInHand().getType() == Material.STICK) {
				event.getPlayer().sendMessage(event.getBlock().getType().name());
				event.setCancelled(true);
			}
		}
	}

}
