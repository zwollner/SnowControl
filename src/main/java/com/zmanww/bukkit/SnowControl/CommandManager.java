/**
 * 
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

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("snowcontrol")) {
			if (sender instanceof Player) {// If run by player check permissions
				if (sender.hasPermission("snowcontrol.reload")) {
					Config.getInstance().reload();
					sender.sendMessage("SnowControl config has been reloaded");
				} else {
					sender.sendMessage(NO_PERMISSION_MSG);
				}
			} else {
				Config.getInstance().reload();
				sender.sendMessage("SnowControl config has been reloaded");
			}
			return true;
		}
		return false;
	}
}
