package com.zmanww.bukkit.SnowControl;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SnowMonitor implements Runnable {

	private SnowControl plugin;
	private Random rnd = new Random();

	public SnowMonitor(SnowControl plugin) {
		super();
		this.plugin = plugin;
	}

	@Override
	public void run() {
		// plugin.getLogger().log(Level.INFO, "Running Monitor...");
		if (Config.getInstance().isAccumulationEnabled() || Config.getInstance().isMeltingEnabled()) {
			for (World world : plugin.getServer().getWorlds()) {
				for (Chunk chunk : world.getLoadedChunks()) {
					ChunkSnapshot chunkSnap = chunk.getChunkSnapshot();
					for (int x = 0; x < 16; x++) {
						for (int z = 0; z < 16; z++) {
							int y = chunkSnap.getHighestBlockYAt(x, z);
							Block block = chunk.getBlock(x, y, z);
							if ((block.getType() == Material.SNOW || Config.getInstance().canAccumulateOn
									.contains(block.getType())) && SnowManager.canSnowInBiome(block.getBiome())) {
								if (world.hasStorm() && Config.getInstance().isAccumulationEnabled()) {// Pile it up
									if (rnd.nextFloat() <= Config.getInstance().getChanceToAccumulate()) {
										SnowManager.increaseSnowLevel(new Location(world, block.getX(), block.getY(),
												block.getZ()));
									}
								} else if (Config.getInstance().isMeltingEnabled() && block.getLightFromSky() > 12) {
									// Melt it down
									if (rnd.nextFloat() <= Config.getInstance().getChanceToMelt()) {
										SnowManager.decreaseSnowLevel(new Location(world, block.getX(), block.getY(),
												block.getZ()));
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
