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

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class SnowMonitor implements Runnable {

	private SnowControl plugin;
	private Random rnd = new Random();

	public SnowMonitor(SnowControl plugin) {
		super();
		this.plugin = plugin;
	}

	public void run() {
		if (!plugin.getServer().getScheduler().isCurrentlyRunning(SnowControl.snowMonitorTaskID)) {
			plugin.debugLog("Running Monitor...");
			if (Config.getInstance().isAccumulationEnabled() || Config.getInstance().isMeltingEnabled()) {
				plugin.debugLog("Checking " + Config.getInstance().enabledWorlds.size() + " worlds");
				for (String worldname : Config.getInstance().enabledWorlds) {
					World world = plugin.getServer().getWorld(worldname);
					plugin.debugLog("Checking " + world.getLoadedChunks().length + " chunks in " + worldname);
					for (final Chunk chunk : world.getLoadedChunks()) {
						ChunkSnapshot chunkSnap = chunk.getChunkSnapshot(true, false, false);
						for (int x = 0; x < 16; x++) {
							for (int z = 0; z < 16; z++) {
								int y = chunkSnap.getHighestBlockYAt(x, z);
								Block block = SnowManager.getHighestNonAirBlock(chunk.getBlock(x, y, z));
								if (block.getType() != Material.AIR && SnowManager.canSnowInBiome(block.getBiome())) {
									if (world.hasStorm()) {
										if (Config.getInstance().isAccumulationEnabled()) {
											if (rnd.nextFloat() <= Config.getInstance().getChanceToAccumulate()) {
												boolean canIncrease = false;
												if (SnowManager.canSnowBeAdded(block)) {
													canIncrease = true;
												} else if (SnowManager.canSnowBeAdded(block.getRelative(BlockFace.UP))) {
													block = block.getRelative(BlockFace.UP);
													canIncrease = true;
												}
												if (canIncrease) {
													SnowManager.increaseSnowLevel(block);
													for (Block blk : SnowManager.getBlocksToIncreaseUnder(block)) {
														SnowManager.increaseSnowLevel(blk);
													}

												}
											}

										}
									} else {// No Storm
										if (Config.getInstance().isMeltingEnabled()
												&& block.getType() != Material.AIR
												&& (Config.getInstance().canFallThrough.contains(block.getType()) || (block
														.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK))) {
											// plugin.debugLog("Melting");
											/*
											 * If the block is something snow can fall through, then there could be snow
											 * under it
											 */
											List<Block> snowBlocks = SnowManager.getSnowBlocksUnder(block);
											if ((block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK)) {
												snowBlocks.add(block);
											}
											for (Block blk : snowBlocks) {
												/*
												 * Stupid hack because bukkit does not return light level for
												 * SNOW_BLOCKS
												 */
												if (blk.getType() == Material.SNOW_BLOCK) {
													blk.setType(Material.SNOW);
													blk.setData((byte) 7);
												}
												if (blk.getType() == Material.SNOW
														&& blk.getLightFromSky() >= Config.getInstance()
																.getMinLightLevel()) {
													// Melt it down
													if (rnd.nextFloat() <= Config.getInstance().getChanceToMelt()) {
														SnowManager.decreaseSnowLevel(new Location(world, blk.getX(),
																blk.getY(), blk.getZ()));
													}
												}
											}
										}
									}
								}
							}
						}
					}// Chunk Loop
				}
			}
			plugin.debugLog("Monitor done");
		} else {
			plugin.getLogger()
					.log(Level.SEVERE,
							"The SnowMonitor task is trying to start before the last one finished!!  Please set 'CheckEvery' to a higher value.");
		}
	}
}
