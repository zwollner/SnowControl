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

import org.bukkit.ChatColor;

public class MessageUtil {
	public static final String RELOAD_MESSAGE = ChatColor.GREEN + "SnowControl config has been reloaded";

	public static String getAddedToList(String blkName, String listName) {
		ChatColor msgColor = ChatColor.GREEN;
		blkName = ChatColor.RED + blkName;
		listName = ChatColor.RED + listName;
		return msgColor + "Added " + blkName + msgColor + " to " + listName + msgColor + " list.";
	}

	public static String getToAddToMsg(String listName) {
		ChatColor msgColor = ChatColor.GREEN;
		listName = ChatColor.RED + listName;
		return msgColor + "Please hit the block to add to the " + listName + msgColor + " list.";
	}

}
