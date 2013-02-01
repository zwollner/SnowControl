package com.zmanww.bukkit.SnowControl;

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
						ChunkSnapshot chunkSnap = chunk.getChunkSnapshot();
						for (int x = 0; x < 16; x++) {
							for (int z = 0; z < 16; z++) {
								int y = chunkSnap.getHighestBlockYAt(x, z);
								Block block = chunk.getBlock(x, y, z);
								if (SnowManager.canSnowInBiome(block.getBiome())) {
									if (world.hasStorm()) {
										if (((block.getType() == Material.SNOW
												|| Config.getInstance().canAccumulateOn.contains(block.getType()) || Config
													.getInstance().canReplace.contains(block.getType())))
												&& block.getType() != Material.AIR) {
											if (world.hasStorm() && Config.getInstance().isAccumulationEnabled()) {// Pile
																													// it
																													// up
												/*
												 * If this block isn't snow, and snow can fall on it, we need to use the
												 * block above to to actually accumulate the snow.
												 */
												if (Config.getInstance().canAccumulateOn.contains(block.getType())
														&& block.getType() != Material.SNOW
														&& Config.getInstance().canReplace.contains(block
																.getRelative(BlockFace.UP))) {

													block = block.getRelative(BlockFace.UP);
												}
												if (Config.getInstance().debugEnabled()
														&& block.getType() != Material.SNOW
														&& block.getType() != Material.GRASS) {
													plugin.debugLog("Accumulating snow on: " + block.getType().name());
												}
												if (rnd.nextFloat() <= Config.getInstance().getChanceToAccumulate()) {
													SnowManager.increaseSnowLevel(new Location(world, block.getX(),
															block.getY(), block.getZ()));
												}
											}
										}
									} else {
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
												if (blk.getLightFromSky() >= 12) {
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
