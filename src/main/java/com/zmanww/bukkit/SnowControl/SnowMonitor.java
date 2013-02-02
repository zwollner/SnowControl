package com.zmanww.bukkit.SnowControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

	@Override
	public void run() {
		plugin.debugLog("Running Monitor...");
		if (Config.getInstance().isAccumulationEnabled() || Config.getInstance().isMeltingEnabled()) {
			plugin.debugLog("Checking " + plugin.getServer().getWorlds().size() + " worlds");
			for (World world : plugin.getServer().getWorlds()) {
				if (Config.getInstance().enabledWorlds.contains(world.getName())) {
					plugin.debugLog("Checking " + world.getLoadedChunks().length + " chunks");
					for (Chunk chunk : world.getLoadedChunks()) {
						ChunkSnapshot chunkSnap = chunk.getChunkSnapshot(true, false, false);
						for (int x = 0; x < 16; x++) {
							for (int z = 0; z < 16; z++) {
								int y = chunkSnap.getHighestBlockYAt(x, z);
								Block block = SnowManager.getHighestNonAirBlock(chunk.getBlock(x, y, z));
								if (SnowManager.canSnowInBiome(block.getBiome())) {
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
													SnowManager.increaseSnowLevel(new Location(world, block.getX(),
															block.getY(), block.getZ()));
													for (Block blk : SnowManager.getBlocksToIncreaseUnder(block)) {
														SnowManager.increaseSnowLevel(new Location(world, blk.getX(),
																blk.getY(), blk.getZ()));
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
												if (blk.getLightFromSky() >= Config.getInstance().getMinLightLevel()) {
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
					}
				}
			}
		}
		plugin.debugLog("Monitor done");
	}
}
