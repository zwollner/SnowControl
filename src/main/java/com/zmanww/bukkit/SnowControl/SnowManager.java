package com.zmanww.bukkit.SnowControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class SnowManager {

	private static final List<BlockFace> directions = Arrays.asList(BlockFace.EAST, BlockFace.NORTH_EAST,
			BlockFace.NORTH, BlockFace.NORTH_WEST, BlockFace.WEST, BlockFace.SOUTH_WEST, BlockFace.SOUTH,
			BlockFace.SOUTH_EAST);

	public static void checkChunk(Chunk chunk) {
		final ChunkSnapshot chunkSnap = chunk.getChunkSnapshot(true, false, false);
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				final int y = chunkSnap.getHighestBlockYAt(x, z);
				Block block = chunk.getBlock(x, y, z);
				if (block.getType() == Material.SNOW) {
					letItFall(block);
				}
			}
		}
	}

	public static void letItFall(final Block block) {
		letItFall(block, false);
	}

	public static void letItFall(final Block block, final boolean dropAll) {
		byte snowLevel = block.getData();

		// S - block
		// L
		// A - loop starts with this one as cur
		// A
		// B
		final Config config = Config.getInstance();
		Block curBlock = block.getRelative(BlockFace.DOWN);
		boolean done = false;
		if (!config.canFallThrough.contains(curBlock.getType())) {
			done = true;
		}
		while (!done) {
			curBlock = curBlock.getRelative(BlockFace.DOWN);
			Block blockAbove = curBlock.getRelative(BlockFace.UP);
			if (config.canAccumulateOn.contains(curBlock.getType())) {
				if (config.canReplace.contains(blockAbove.getType()) || blockAbove.getType() == Material.SNOW) {

					if (blockAbove.getLightFromBlocks() <= 10) {// Not near torches
						if (!dropAll) {
							snowLevel = 0;
						}
						if (blockAbove.getType() == Material.SNOW) {
							snowLevel += blockAbove.getData() + 1;
						}
						if (snowLevel > config.getMaxAccumulation(curBlock.getType())) {
							snowLevel = config.getMaxAccumulation(curBlock.getType());
						}
						blockAbove.setType(Material.SNOW);
						blockAbove.setData(snowLevel);

					}
				}
			} else if (curBlock.getType() == Material.STATIONARY_WATER) {
				curBlock.setType(Material.ICE);
			}

			if (!config.canFallThrough.contains(curBlock.getType()) && curBlock.getType() != Material.SNOW) {
				done = true;
			}
		}
	}

	public static void increaseSnowLevel(Location loc) {
		Block block = loc.getBlock();
		byte blkData = (block.getType() == Material.SNOW ? block.getData() : 0);
		if (blkData <= 6) {
			if (blkData <= getMaxSurrounding(block) && blkData <= getMinSurrounding(block) + 2) {
				if (block.getType() == Material.SNOW) {
					block.setData((byte) (blkData + 1));
				} else {
					block.setType(Material.SNOW);
				}
				if (new Random(System.nanoTime()).nextFloat() <= Config.getInstance().getChanceToMelt()) {
					letItFall(block);
				}
			}
		}
	}

	public static boolean canSnowInBiome(Biome biome) {
		boolean retVal = false;
		if (biome == Biome.ICE_MOUNTAINS || biome == Biome.ICE_PLAINS || biome == Biome.TAIGA
				|| biome == Biome.TAIGA_HILLS || biome == Biome.FROZEN_OCEAN || biome == Biome.FROZEN_RIVER) {
			retVal = true;
		}
		return retVal;
	}

	public static void decreaseSnowLevel(Location loc) {
		Block block = loc.getBlock();
		if (block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK) {
			byte blkData = getSnowValue(block);
			if (blkData >= getMinSurrounding(block) && blkData >= getMaxSurrounding(block) - 2) {
				if (blkData > 0) {
					if (block.getType() == Material.SNOW_BLOCK) {
						block.setType(Material.SNOW);
					}
					block.setData((byte) (blkData - 1));
				} else if ((Config.getInstance().meltDownCompletely())
						|| (block.getRelative(BlockFace.DOWN).getType() == Material.SNOW)
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

	private static byte getMaxSurrounding(final Block block) {
		byte retVal = 0;

		for (BlockFace face : directions) {
			retVal = getSnowValue(block.getRelative(face)) > retVal ? getSnowValue(block.getRelative(face)) : retVal;
		}

		return retVal;
	}

	private static byte getMinSurrounding(final Block block) {
		byte retVal = 7;

		for (BlockFace face : directions) {
			retVal = getSnowValue(block.getRelative(face)) < retVal ? getSnowValue(block.getRelative(face)) : retVal;
		}
		return retVal;
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
}
