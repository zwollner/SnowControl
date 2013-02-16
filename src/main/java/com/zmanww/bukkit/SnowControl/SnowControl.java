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

	public static Map<Player, String> pendingCommand = new HashMap<Player, String>();

	public static int snowMonitorTaskID;

	public void onEnable() {
		plugin = this;
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(worldListener, this);

		getCommand("snowcontrol").setExecutor(new CommandManager());

		this.getLogger().info(
				"Scheduling Monitor to start in 20sec... repeating every " + Config.getInstance().getSnowFallDelay()
						+ "sec.");
		snowMonitorTaskID = getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new SnowMonitor(plugin),
				20L * 20L, Config.getInstance().getSnowFallDelay() * 20L);

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}

	}

	public void onDisable() {
		plugin.getServer().getScheduler().cancelTask(snowMonitorTaskID);
		this.saveConfig();
	}

	public void debugLog(String string) {
		if (Config.getInstance().debugEnabled()) {
			this.getLogger().log(Level.INFO, "<DEBUG> " + string);
		}

	}
}