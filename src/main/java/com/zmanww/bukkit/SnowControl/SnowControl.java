package com.zmanww.bukkit.SnowControl;

import java.util.logging.Level;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class SnowControl extends JavaPlugin implements Listener {
	public static SnowControl plugin;

	private final PlayerListener playerListener = new PlayerListener(this);
	private final WorldListener worldListener = new WorldListener(this);

	public static int snowMonitorTaskID;

	public void onEnable() {
		plugin = this;
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(worldListener, this);

		this.getLogger().info(
				"Scheduling Monitor to start in 10sec... repeating every " + Config.getInstance().getSnowFallDelay()
						+ "sec.");
		snowMonitorTaskID = getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new SnowMonitor(plugin),
				10L * 20L, Config.getInstance().getSnowFallDelay() * 20L);

	}

	public void onDisable() {
		this.saveConfig();
	}

	public void debugLog(String string) {
		if (Config.getInstance().debugEnabled()) {
			this.getLogger().log(Level.INFO, "<DEBUG> " + string);
		}

	}
}