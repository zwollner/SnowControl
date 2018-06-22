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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class SnowManager {

	private static final List<BlockFace> directions = Arrays.asList(BlockFace.EAST, BlockFace.NORTH_EAST,
			BlockFace.NORTH, BlockFace.NORTH_WEST, BlockFace.WEST, BlockFace.SOUTH_WEST, BlockFace.SOUTH,
			BlockFace.SOUTH_EAST);

	public static void increaseSnowLevel(Block block) {
		byte blkData = (block.getType() == Material.SNOW ? block.getData() : 0);
		if (blkData <= 6) {
			if (shouldIncrease(block, blkData) && getSnowDepth(block) <= Config.getInstance().getMaxAccumulation(
					getTypeUnderSnow(block))) {

				if (block.getRelative(BlockFace.DOWN).getType() == Material.SOIL) {
					block.getRelative(BlockFace.DOWN).setType(Material.DIRT);
				}

				if (block.getType() == Material.SNOW && blkData < 6) {
					block.setData((byte) (blkData + 1));
				} else if (block.getType() == Material.SNOW && blkData == 6) {
					block.setType(Material.SNOW_BLOCK);
				} else if (Config.getInstance().canReplace.contains(block.getType())) {
					block.setType(Material.AIR);// for some reason this helps
					block.setType(Material.SNOW);
				}
			}
		}
	}

	private static Material getTypeUnderSnow(Block block) {
		while (block.getType().equals(Material.SNOW) || block.getType().equals(Material.SNOW_BLOCK)) {
			block = block.getRelative(BlockFace.DOWN);
		}
		return block.getType();
	}

	private static boolean shouldIncrease(Block block, byte blkData) {
		return blkData <= getMaxSurrounding(block, (byte) 7) && blkData < getMinSurrounding(block, (byte) 7) + 2;
	}

	public static int getSnowDepth(Block block) {
		int retVal = 0;
		while (block.getType().equals(Material.SNOW) || block.getType().equals(Material.SNOW_BLOCK)) {
			if (block.getType().equals(Material.SNOW)) {
				retVal += block.getData() + 1;
			} else {
				retVal += 8;
			}
			block = block.getRelative(BlockFace.DOWN);
		}

		return retVal;
	}

	public static boolean canSnowInBiome(Biome biome) {
		return biome.name().contains("ICE_") || biome.name().contains("COLD_") || biome.name().contains("_COLD") || biome.name().contains("FROZEN_");
	}

	public static void decreaseSnowLevel(Block block) {
		if (block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK) {
			byte blkData = getSnowValue(block);
			if (blkData >= getMinSurrounding(block, (byte) 0) && blkData > getMaxSurrounding(block, (byte) 0, true) - 2) {
				if (blkData > 0) {
					if (block.getType() == Material.SNOW_BLOCK) {
						block.setType(Material.SNOW);
					}
					block.setData((byte) (blkData - 1));
				} else if ((Config.getInstance().meltDownCompletely()) || (block.getRelative(BlockFace.DOWN).getType() == Material.SNOW)
						|| (block.getRelative(BlockFace.DOWN).getType() == Material.SNOW_BLOCK)) {
					block.setType(Material.AIR);
				}
			}
		}
	}

	public static void removeReplaceableUnder(Block block) {
		while (block.getRelative(BlockFace.DOWN).getType().equals(Material.AIR)) {
			block = block.getRelative(BlockFace.DOWN);
		}

		if (Config.getInstance().canReplace.contains(block.getRelative(BlockFace.DOWN).getType())) {
			block.setType(Material.AIR);
		}

	}

	public static byte getMaxSurrounding(final Block block, final byte def, final boolean ignoreBlocks) {
		byte retVal = -1;

		for (BlockFace face : directions) {
			Block tempBlk = block.getRelative(face);
			if (tempBlk.getType() == Material.SNOW || (tempBlk.getType() == Material.SNOW_BLOCK && !ignoreBlocks)) {
				retVal = getSnowValue(tempBlk) > retVal ? getSnowValue(tempBlk) : retVal;
			}
		}

		return retVal == -1 ? def : retVal;
	}

	public static byte getMaxSurrounding(final Block block, final byte def) {
		return getMaxSurrounding(block, def, false);
	}

	public static byte getMinSurrounding(final Block block, final byte def) {
		byte retVal = 8;

		for (BlockFace face : directions) {
			Block tempBlk = block.getRelative(face);
			if (tempBlk.getType() == Material.SNOW || tempBlk.getType() == Material.SNOW_BLOCK) {
				retVal = getSnowValue(tempBlk) < retVal ? getSnowValue(tempBlk) : retVal;
			}
		}
		return retVal == 8 ? def : retVal;
	}

	private static byte getSnowValue(Block block) {
		byte retVal = 0;

		if (block.getType() == Material.SNOW) {
			retVal = block.getData();
		} else if (block.getType() == Material.SNOW_BLOCK) {
			retVal = 7;
		}
		return retVal;

	}

	public static byte getSnowLevelUnder(Block block) {
		while (block.getRelative(BlockFace.DOWN).getType().equals(Material.AIR)) {
			block = block.getRelative(BlockFace.DOWN);
		}
		if (block.getRelative(BlockFace.DOWN).getType() == Material.SNOW) {
			return block.getRelative(BlockFace.DOWN).getData();
		} else if (block.getRelative(BlockFace.DOWN).getType() == Material.SNOW_BLOCK) {
			return 7;
		}
		return -1;
	}

	public static Block getHighestNonAirBlock(Block currentBlock)
	{
		while (currentBlock.getType() == Material.AIR)
		{
			if (currentBlock.getY() < 2) return currentBlock; // For sky levels or other areas where there are no Non-Air blocks
			currentBlock = currentBlock.getRelative(BlockFace.DOWN);
		}
		return currentBlock;
	}

	public static Block getHighestNonAirBlock(int x, int z, World world)
	{
		Block currentBlock = world.getBlockAt(x, world.getMaxHeight() - 1, z);
		while (currentBlock.getType() == Material.AIR)
		{
			if (currentBlock.getY() < 2) return currentBlock; // For sky levels or other areas where there are no Non-Air blocks
			currentBlock = currentBlock.getRelative(BlockFace.DOWN);
		}
		return currentBlock;
	}

	public static List<Block> getSnowBlocksUnder(Block block) {
		ArrayList<Block> blocks = new ArrayList<Block>();
		boolean done = false;
		while (!done) {
			Block tempBlk = block.getRelative(BlockFace.DOWN);
			if (block.getType() != Material.SNOW && block.getType() != Material.SNOW_BLOCK) {
				if (tempBlk.getType() == Material.SNOW || tempBlk.getType() == Material.SNOW_BLOCK) {
					blocks.add(tempBlk);
				}
			}

			if (!Config.getInstance().canFallThrough.contains(tempBlk.getType()) && tempBlk.getType() != Material.SNOW
					&& tempBlk.getType() != Material.SNOW_BLOCK) {
				// Can't fall through block, and it's not snow
				done = true;
			}

			block = tempBlk;
		}

		return blocks;

	}

	public static List<Block> getBlocksToIncreaseUnder(Block block) {

		ArrayList<Block> blocks = new ArrayList<Block>();
		boolean done = false;
		while (!done) {
			Block tempBlk = block.getRelative(BlockFace.DOWN);
			if (canSnowBeAdded(tempBlk)) {
				blocks.add(tempBlk);
			}

			if ((!Config.getInstance().canFallThrough.contains(tempBlk.getType()) && tempBlk.getType() != Material.SNOW && tempBlk
					.getType() != Material.SNOW_BLOCK) || !(new Random(System.nanoTime()).nextFloat() <= Config
					.getInstance().getChanceToFallThrough())) {
				// Can't fall through block, and it's not snow, OR odds say no
				done = true;
			}

			block = tempBlk;
		}

		return blocks;
	}

	public static boolean canAccumulateOn(final Block block) {
		if (block.getType() == Material.SNOW && block.getData() == 7) {
			// This is essentially a full block
			return true;
		}
		return Config.getInstance().canAccumulateOn.contains(block.getType());
	}

	public static boolean canSnowBeAdded(Block block) {
		if (block.getType() == Material.SNOW && block.getData() < 7) {
			return true;
		}
		if (canAccumulateOn(block.getRelative(BlockFace.DOWN))) {
			return Config.getInstance().canReplace.contains(block.getType());
		}
		return false;
	}
}
