/*
 * This file is part of SnowControl.
 *
 * Copyright (c) 2013 Zeb Wollner
 * SnowControl is licensed under the GNU General Public License.
 *
 * SnowControl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SnowControl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
