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
