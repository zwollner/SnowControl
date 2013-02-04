package com.zmanww.bukkit.SnowControl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

/**
 * @author zwollner
 * 
 */
public class WorldListener implements Listener {
	private static SnowControl plugin;

	public WorldListener(SnowControl instance) {
		plugin = instance;
	}

	@EventHandler()
	public void leavesDecay(LeavesDecayEvent event) {
		final Block block = event.getBlock();
		if (block.getRelative(BlockFace.UP).getType() == Material.SNOW) {
			final Location loc = event.getBlock().getLocation();
			SnowManager.removeReplaceableUnder(block);
			// Need to delay it so that the leaves can be removed first.
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					loc.getWorld().spawnFallingBlock(loc, Material.SNOW, block.getRelative(BlockFace.UP).getData());
					// SnowManager.dropSnow(block.getRelative(BlockFace.UP));
				}
			}, 3L);
		}
	}

}
