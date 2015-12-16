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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public class SnowControl extends JavaPlugin implements Listener {
	public static SnowControl plugin;

	private final PlayerListener playerListener = new PlayerListener(this);
	private final WorldListener worldListener = new WorldListener(this);

	public static final String COMMAND_REPLACE = "REPLACE";
	public static final String COMMAND_FALLTHROUGH = "FALLTHROUGH";
	public static final String COMMAND_ACCUMULATE = "ACCUMULATE";

	public static Map<Player, String> pendingCommand = new HashMap<>();

	private SnowMonitor snowMonitor;

	public void onEnable() {
		plugin = this;
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(worldListener, this);
		getCommand("snowcontrol").setExecutor(new CommandManager());
		startScheduler();
		if(Config.getInstance().isMeltingEnabled())
		{
			try
			{
				Metrics metrics = new Metrics(this);
				metrics.start();
			}
			catch(IOException e)
			{
				// Failed to submit the stats :-(
			}
		}
	}

	public void startScheduler()
	{
		if(snowMonitor != null)
		{
			snowMonitor.cancel();
		}
		snowMonitor = new SnowMonitor(this);
		this.getLogger().info("Scheduling Monitor to start in 20sec... repeating every " + Config.getInstance().getSnowFallDelay() + "sec.");
		snowMonitor.runTaskTimer(plugin, 400L, Config.getInstance().getSnowFallDelay() * 20L);
	}

	public void onDisable() {
		if(snowMonitor != null) snowMonitor.cancel();
		this.saveConfig();
	}

	public void debugLog(String string) {
		if (Config.getInstance().debugEnabled()) {
			this.getLogger().log(Level.INFO, "<DEBUG> " + string);
		}

	}
}