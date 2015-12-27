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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowMonitor extends BukkitRunnable
{
	private SnowControl plugin;
	private Random rnd = new Random();
	private float chanceToAccumulateChunk = 100, chanceToAccumulateBlock, chanceToMeltChunk = 100, chanceToMeltBlock;
	private int blocksPerChunkToAccumulate, blocksPerChunkToMelt, maxChunks = 441, minChunks = 1;
	private boolean accumulation, melt;

	public SnowMonitor(SnowControl plugin)
	{
		super();
		this.plugin = plugin;
		chanceToAccumulateBlock = (float) Config.getInstance().getChanceToAccumulate() / 100f; //TODO: Change config
		chanceToMeltBlock = Config.getInstance().getChanceToMelt() / 100f; //TODO: Change config
		accumulation = Config.getInstance().isAccumulationEnabled();
		melt = Config.getInstance().isMeltingEnabled();
		//TODO: Read configs for: maxChunks, minChunks, chanceToAccumulateChunk, chanceToMeltChunk

		// Calculate stuff, we don't need to do this every time we run our loops
		blocksPerChunkToAccumulate = (int) Math.ceil(256 * chanceToAccumulateBlock);
		blocksPerChunkToMelt = (int) Math.ceil(256 * chanceToMeltBlock);
	}

	private Chunk[] getRandomElements(int count, Chunk... items)
	{
		if(count > items.length)
			count = items.length;
		if(count == items.length)
			return items;
		List<Chunk> list = new ArrayList<>(count);
		Chunk data;
		while(count > 0)
		{
			data = items[rnd.nextInt(items.length)];
			if(!list.contains(data))
			{
				list.add(data);
				count--;
			}
		}
		items = list.toArray(new Chunk[count]);
		list.clear();
		return items;
	}

	private List<Pos2D> generateRandom2DPosList(int count)
	{
		return generateRandom2DPosList(0, 15, 0, 15, count);
	}

	private List<Pos2D> generateRandom2DPosList(int minX, int maxX, int minY, int maxY, int count)
	{
		if(count > (maxX - minX + 1) * (maxY - minY + 1))
			count = (maxX - minX + 1) * (maxY - minY + 1);
		maxX = maxX - minX + 1;
		maxY = maxY - minY + 1;
		List<Pos2D> list = new ArrayList<>(count);
		Pos2D randPos;
		while(count > 0)
		{
			randPos = new Pos2D(rnd.nextInt(maxX) + minX, rnd.nextInt(maxY) + minY);
			if(!list.contains(randPos))
			{
				list.add(randPos);
				count--;
			}
		}
		return list;
	}

	private class Pos2D
	{
		private int x, y;

		public Pos2D(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		public int getX()
		{
			return this.x;
		}

		public int getY()
		{
			return this.y;
		}

		@Override
		public boolean equals(Object other)
		{
			return other != null && other instanceof Pos2D && this.getX() == ((Pos2D) other).getX() && this.getY() == ((Pos2D) other).getY();
		}
	}

	public void run()
	{
		plugin.debugLog("Running Monitor...\nChecking " + Config.getInstance().enabledWorlds.size() + " worlds");
		for(String worldName : Config.getInstance().enabledWorlds)
		{
			World world = plugin.getServer().getWorld(worldName);
			if(world == null)
				continue;
			Chunk[] chunks = world.getLoadedChunks();
			plugin.debugLog("Checking " + chunks.length + " chunks in " + worldName);
			if(accumulation && world.hasStorm())
			{
				chunks = getRandomElements(Math.min(Math.max((int) Math.ceil(chunks.length * chanceToAccumulateChunk), minChunks), maxChunks), chunks); // I know it's not perfect, but we have to sacrifice precision for performance
				for(Chunk chunk : chunks)
				{
					List<Pos2D> blocks = generateRandom2DPosList(blocksPerChunkToAccumulate); // I know it's not perfect, but we have to sacrifice precision for performance
					for(Pos2D pos : blocks)
					{
						Block block = SnowManager.getHighestNonAirBlock(pos.getX() + chunk.getX() * 16, pos.getY() + chunk.getZ() * 16, world);
						if(block.getType() != Material.AIR && SnowManager.canSnowInBiome(block.getBiome()))
						{
							boolean canIncrease = SnowManager.canSnowBeAdded(block);
							if(!canIncrease && SnowManager.canSnowBeAdded(block.getRelative(BlockFace.UP)))
							{
								block = block.getRelative(BlockFace.UP);
								canIncrease = true;
							}
							if(canIncrease)
							{
								SnowManager.increaseSnowLevel(block);
								for(Block blk : SnowManager.getBlocksToIncreaseUnder(block))
								{
									SnowManager.increaseSnowLevel(blk);
								}
							}
						}
					}
				}
			}
			else if(melt && !world.hasStorm())
			{
				chunks = getRandomElements(Math.min(Math.max((int) Math.ceil(chunks.length * chanceToMeltChunk), minChunks), maxChunks), chunks); // I know it's not perfect, but we have to sacrifice precision for performance
				for(final org.bukkit.Chunk chunk : chunks)
				{
					List<Pos2D> blocks = generateRandom2DPosList(blocksPerChunkToMelt); // I know it's not perfect, but we have to sacrifice precision for performance
					for(Pos2D pos : blocks)
					{
						Block block = SnowManager.getHighestNonAirBlock(pos.getX() + chunk.getX() * 16, pos.getY() + chunk.getZ() * 16, world);
						if(block.getType() != Material.AIR && SnowManager.canSnowInBiome(block.getBiome()))
						{
							if(block.getType() != Material.AIR && (Config.getInstance().canFallThrough.contains(block.getType()) || (block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK)))
							{
								List<Block> snowBlocks = SnowManager.getSnowBlocksUnder(block); // If the block is something snow can fall through, then there could be snow
								if((block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK))
								{
									snowBlocks.add(block);
								}
								for(Block blk : snowBlocks)
								{
									if(blk.getType() == Material.SNOW_BLOCK) // Stupid hack because bukkit does not return light level for SNOW_BLOCKS
									{
										blk.setType(Material.SNOW);
										//noinspection deprecation
										blk.setData((byte) 7);
									}
									if(blk.getType() == Material.SNOW && blk.getLightFromSky() >= Config.getInstance().getMinLightLevel())
									{
										SnowManager.decreaseSnowLevel(new Location(world, blk.getX(), blk.getY(), blk.getZ())); // Melt it down
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