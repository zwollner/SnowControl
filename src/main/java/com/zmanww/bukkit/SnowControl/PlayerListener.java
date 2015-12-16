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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author zwollner
 * 
 */
public class PlayerListener implements Listener {

	private static SnowControl plugin;

	public PlayerListener(SnowControl instance) {
		plugin = instance;
	}

	@EventHandler()
	public void blockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getType() == Material.ICE) {
			if (event.getPlayer().getItemInHand().getType() == Material.STONE_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.WOOD_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.IRON_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.GOLD_PICKAXE
					|| event.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE) {
				block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.ICE, 1));
			}
		}
		final Location loc = event.getBlock().getLocation();
		byte under = -1;
		if (block.getRelative(BlockFace.UP).getType() == Material.SNOW
				|| block.getRelative(BlockFace.UP).getType() == Material.SNOW_BLOCK) {
			/*
			 * If block above broken is snow, then remove the next replaceable block under it so that the falling snow
			 * lands properly.
			 */
			SnowManager.removeReplaceableUnder(block);

			under = SnowManager.getSnowLevelUnder(block);// get level of snow where it will land
		}
		while (block.getRelative(BlockFace.UP).getType() == Material.SNOW
				|| block.getRelative(BlockFace.UP).getType() == Material.SNOW_BLOCK) {
			block = block.getRelative(BlockFace.UP);

			if (block.getType() == Material.SNOW_BLOCK) {
				block.setType(Material.AIR);
				loc.getWorld().spawnFallingBlock(loc, Material.SNOW_BLOCK, (byte) 0);
			} else {
				final byte above = block.getData();// get level of snow about to fall
				final byte newUnder = (byte) (above + under + 1 > 7 ? 7 : above + under + 1);
				loc.getWorld().spawnFallingBlock(loc, Material.SNOW, newUnder);
			}

		}
	}

	@EventHandler()
	public void onBlockDamage(BlockDamageEvent event) {
		if (SnowControl.pendingCommand.containsKey(event.getPlayer())) {
			if (SnowControl.pendingCommand.get(event.getPlayer()).equals(SnowControl.COMMAND_ACCUMULATE)) {
				Config.getInstance().addAccumulate(event.getBlock().getType());
				event.getPlayer().sendMessage(
						MessageUtil.getAddedToList(event.getBlock().getType().name(), Config.CONFIG_CAN_ACCUMULATE_ON));
				SnowControl.pendingCommand.remove(event.getPlayer());
				event.setCancelled(true);
			} else if (SnowControl.pendingCommand.get(event.getPlayer()).equals(SnowControl.COMMAND_FALLTHROUGH)) {
				SnowControl.pendingCommand.remove(event.getPlayer());
				event.getPlayer().sendMessage(
						MessageUtil.getAddedToList(event.getBlock().getType().name(), Config.CONFIG_CAN_FALL_THROUGH));
				Config.getInstance().addFallThrough(event.getBlock().getType());
				event.setCancelled(true);
			} else if (SnowControl.pendingCommand.get(event.getPlayer()).equals(SnowControl.COMMAND_REPLACE)) {
				Config.getInstance().addReplaceable(event.getBlock().getType());
				event.getPlayer().sendMessage(
						MessageUtil.getAddedToList(event.getBlock().getType().name(), Config.CONFIG_CAN_REPLACE));
				SnowControl.pendingCommand.remove(event.getPlayer());
				event.setCancelled(true);
			}
		} else if (Config.getInstance().debugEnabled()) {
			Block block = event.getBlock();
			if (event.getItemInHand().getType() == Material.STICK) {
				Block highBlk = SnowManager.getHighestNonAirBlock(block.getX(), block.getZ(), block.getWorld());
				event.getPlayer().sendMessage("Highest: " + highBlk.getType().name());
				event.getPlayer().sendMessage(
						event.getBlock().getType().name() + ":" + event.getBlock().getData() + " Light="
								+ event.getBlock().getLightFromSky());

				List<Block> snowBlocks = SnowManager.getSnowBlocksUnder(event.getBlock());
				if ((event.getBlock().getType() == Material.SNOW || event.getBlock().getType() == Material.SNOW_BLOCK)) {
					snowBlocks.add(event.getBlock());
				}
				for (Block blk : snowBlocks) {
					event.getPlayer().sendMessage(
							"under >" + blk.getType().name() + ":" + blk.getData() + " Light="
									+ event.getBlock().getLightFromSky());
				}
				event.getPlayer().sendMessage("**");
				event.setCancelled(true);
			} else if (event.getItemInHand().getType() == Material.SNOW_BALL) {
				event.getPlayer().sendMessage("CurrentDepth=" + SnowManager.getSnowDepth(block));
				event.getPlayer().sendMessage("MinSurrounding=" + SnowManager.getMinSurrounding(block, (byte) -1));
				event.getPlayer().sendMessage("MaxSurrounding=" + SnowManager.getMaxSurrounding(block, (byte) -1));
				event.getPlayer().sendMessage("canSnowBeAdded=" + SnowManager.canSnowBeAdded(block));
				event.getPlayer().sendMessage(
						"canSnowBeAddedAbove=" + SnowManager.canSnowBeAdded(block.getRelative(BlockFace.UP)));

				List<Block> snowBlocks = SnowManager.getBlocksToIncreaseUnder(event.getBlock());
				for (Block blk : snowBlocks) {
					event.getPlayer().sendMessage("under>" + blk.getType().name() + ":" + blk.getData());
				}
				event.getPlayer().sendMessage("**");
				event.setCancelled(true);
			} else if (event.getItemInHand().getType() == Material.SNOW_BLOCK) {
				event.getPlayer().sendMessage("Increasing Snow Level");

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
				event.getPlayer().sendMessage("**");
				event.setCancelled(true);
			} else if (event.getItemInHand().getType() == Material.BLAZE_ROD) {
				event.getPlayer().sendMessage("Decreasing Snow Level");

				List<Block> snowBlocks = SnowManager.getSnowBlocksUnder(block);
				if ((block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK)) {
					snowBlocks.add(block);
				}
				for (Block blk : snowBlocks) {
					if (blk.getType() == Material.SNOW_BLOCK) {
						blk.setType(Material.SNOW);
						blk.setData((byte) 7);
					}
					if (blk.getLightFromSky() >= 12) {
						// Melt it down
						SnowManager.decreaseSnowLevel(new Location(event.getBlock().getWorld(), blk.getX(), blk.getY(),
								blk.getZ()));
					}
				}
				event.getPlayer().sendMessage("**");
				event.setCancelled(true);
			}
		}
	}
}
