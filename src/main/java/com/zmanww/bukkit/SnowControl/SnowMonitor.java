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

	public SnowMonitor(SnowControl plugin)
	{
		super();
		this.plugin = plugin;
	}

	private Chunk[] getRandomElements(int count, Chunk... items)
	{
		if(count > items.length)
			count = items.length;
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
		return list.toArray(new Chunk[count]);
	}

	private List<Pos2D> generateRandom2DPosList(int count)
	{
		return generateRandom2DPosList(0, 15, 0, 15, count);
	}

	private List<Pos2D> generateRandom2DPosList(int minX, int maxX, int minY, int maxY, int count)
	{
		if(count > (maxX - minX + 1) * (maxY - minY + 1))
			count = (maxX - minX + 1) * (maxY - minY + 1);
		List<Pos2D> list = new ArrayList<>(count);
		Pos2D randPos;
		while(count > 0)
		{
			randPos = new Pos2D(rnd.nextInt(maxX - minX + 1) + minX, rnd.nextInt(maxY - minY + 1) + minY);
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
		plugin.debugLog("Running Monitor...");
		if(Config.getInstance().isAccumulationEnabled() || Config.getInstance().isMeltingEnabled())
		{
			float chanceToAccumulate = Config.getInstance().getChanceToAccumulate(), chanceToMelt = Config.getInstance().getChanceToMelt();
			plugin.debugLog("Checking " + Config.getInstance().enabledWorlds.size() + " worlds");
			for(String worldName : Config.getInstance().enabledWorlds)
			{
				World world = plugin.getServer().getWorld(worldName);
				if(world == null)
					continue;
				Chunk[] chunks = world.getLoadedChunks();
				plugin.debugLog("Checking " + chunks.length + " chunks in " + worldName);
				if(Config.getInstance().isAccumulationEnabled() && world.hasStorm())
				{
					chunks = getRandomElements((int) Math.ceil(chunks.length * chanceToAccumulate), chunks); // I know it's not perfect, but we have to sacrifice precision for performance
					for(Chunk chunk : chunks)
					{
						List<Pos2D> blocks = generateRandom2DPosList((int) Math.ceil(256 * chanceToAccumulate)); // I know it's not perfect, but we have to sacrifice precision for performance
						for(Pos2D pos : blocks)
						{
							Block block = SnowManager.getHighestNonAirBlock(pos.getX() + chunk.getX() * 16, pos.getY() + chunk.getZ() * 16, world);
							if(block.getType() != Material.AIR && SnowManager.canSnowInBiome(block.getBiome()))
							{
								boolean canIncrease = false;
								if(SnowManager.canSnowBeAdded(block))
								{
									canIncrease = true;
								}
								else if(SnowManager.canSnowBeAdded(block.getRelative(BlockFace.UP)))
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
					}// Chunk Loop
				}
				else if(Config.getInstance().isMeltingEnabled() && !world.hasStorm())
				{
					chunks = getRandomElements((int) Math.ceil(chunks.length * chanceToMelt), chunks); // I know it's not perfect, but we have to sacrifice precision for performance
					for(final org.bukkit.Chunk chunk : chunks)
					{
						List<Pos2D> blocks = generateRandom2DPosList((int) Math.ceil(256 * chanceToMelt)); // I know it's not perfect, but we have to sacrifice precision for performance
						for(Pos2D pos : blocks)
						{
							Block block = SnowManager.getHighestNonAirBlock(pos.getX() + chunk.getX() * 16, pos.getY() + chunk.getZ() * 16, world);
							if(block.getType() != Material.AIR && SnowManager.canSnowInBiome(block.getBiome()))
							{
								if(block.getType() != Material.AIR && (Config.getInstance().canFallThrough.contains(block.getType()) || (block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK)))
								{
									// plugin.debugLog("Melting");
										/*
										 * If the block is something snow can fall through, then there could be snow
										 * under it
										 */
									List<Block> snowBlocks = SnowManager.getSnowBlocksUnder(block);
									if((block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK))
									{
										snowBlocks.add(block);
									}
									for(Block blk : snowBlocks)
									{
											/*
											 * Stupid hack because bukkit does not return light level for
											 * SNOW_BLOCKS
											 */
										if(blk.getType() == Material.SNOW_BLOCK)
										{
											blk.setType(Material.SNOW);
											blk.setData((byte) 7);
										}
										if(blk.getType() == Material.SNOW && blk.getLightFromSky() >= Config.getInstance().getMinLightLevel())
										{
											// Melt it down
											if(rnd.nextFloat() <= Config.getInstance().getChanceToMelt())
											{
												SnowManager.decreaseSnowLevel(new Location(world, blk.getX(), blk.getY(), blk.getZ()));
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