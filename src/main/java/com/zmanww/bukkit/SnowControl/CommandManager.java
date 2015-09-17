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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author zwollner
 * 
 */
public class CommandManager implements CommandExecutor {

	private static final String NO_PERMISSION_MSG = "You do not have the required permissions to do that.";

	// private static SnowControl plugin;
	//
	// public CommandManager(SnowControl instance) {
	// plugin = instance;
	// }

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("snowcontrol") && args.length > 0) {
			if (args[0].equalsIgnoreCase("reload")) {
				if (sender instanceof Player) {// If run by player check
												// permissions
					if (sender.hasPermission("snowcontrol.reload")) {
						Config.getInstance().reload();
						sender.sendMessage(MessageUtil.RELOAD_MESSAGE);
					} else {
						sender.sendMessage(NO_PERMISSION_MSG);
					}
				} else {
					Config.getInstance().reload();
					sender.sendMessage(MessageUtil.RELOAD_MESSAGE);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("addReplace")) {
				if (sender instanceof Player) {
					if (sender.hasPermission("snowcontrol.addReplace")) {
						SnowControl.pendingCommand.put((Player) sender, SnowControl.COMMAND_REPLACE);
						sender.sendMessage(MessageUtil.getToAddToMsg(Config.CONFIG_CAN_REPLACE));
					} else {
						sender.sendMessage(NO_PERMISSION_MSG);
					}
				}
				return true;
			} else if (args[0].equalsIgnoreCase("addAccum")) {
				if (sender instanceof Player) {
					if (sender.hasPermission("snowcontrol.addAccum")) {
						SnowControl.pendingCommand.put((Player) sender, SnowControl.COMMAND_ACCUMULATE);
						sender.sendMessage(MessageUtil.getToAddToMsg(Config.CONFIG_CAN_ACCUMULATE_ON));
					} else {
						sender.sendMessage(NO_PERMISSION_MSG);
					}
				}
				return true;
			} else if (args[0].equalsIgnoreCase("addFall")) {
				if (sender instanceof Player) {
					if (sender.hasPermission("snowcontrol.addFall")) {
						SnowControl.pendingCommand.put((Player) sender, SnowControl.COMMAND_FALLTHROUGH);
						sender.sendMessage(MessageUtil.getToAddToMsg(Config.CONFIG_CAN_FALL_THROUGH));
					} else {
						sender.sendMessage(NO_PERMISSION_MSG);
					}
				}
				return true;
			}
		}
		return false;
	}
}
