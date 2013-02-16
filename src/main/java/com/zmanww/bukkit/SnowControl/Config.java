package com.zmanww.bukkit.SnowControl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.World;

public class Config {
	protected static final SnowControl plugin = SnowControl.plugin;
	private static Config instance;

	public List<Material> canReplace = new ArrayList<Material>();
	public List<Material> canFallThrough = new ArrayList<Material>();
	public List<Material> canAccumulateOn = new ArrayList<Material>();
	public List<String> enabledWorlds = new ArrayList<String>();

	private Config() {
		if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
			plugin.saveDefaultConfig();
		}
		plugin.getConfig().options().copyDefaults(true);
		loadKeys();
	}

	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}
		return instance;
	}

	public void reload() {
		plugin.reloadConfig(); // Force reload
		instance = null;// Force all objects to reload.

		// restart the Monitor
		plugin.getServer().getScheduler().cancelTask(SnowControl.snowMonitorTaskID);
		plugin.getLogger()
				.info("Restarting the Monitor in 5sec... repeating every " + Config.getInstance().getSnowFallDelay()
						+ "sec.");
		SnowControl.snowMonitorTaskID = plugin
				.getServer()
				.getScheduler()
				.scheduleSyncRepeatingTask(plugin, new SnowMonitor(plugin), 5L * 20L,
						Config.getInstance().getSnowFallDelay() * 20L);
	}

	private void loadKeys() {
		canFallThrough = stringToMaterial(plugin.getConfig().getStringList("SnowFall.CanFallThrough"));
		if (!canFallThrough.contains(Material.AIR)) {
			canFallThrough.add(Material.AIR);
		}

		canReplace = stringToMaterial(plugin.getConfig().getStringList("SnowFall.CanReplace"));
		if (!canReplace.contains(Material.AIR)) {
			canReplace.add(Material.AIR);
		}

		canAccumulateOn = stringToMaterial(plugin.getConfig().getStringList("SnowFall.CanAccumulateOn"));
		if (!canAccumulateOn.contains(Material.SNOW_BLOCK)) {
			canAccumulateOn.add(Material.SNOW_BLOCK);
		}

		if (plugin.getConfig().isSet(("SnowFall.EnabledWorlds"))) {
			enabledWorlds = plugin.getConfig().getStringList("SnowFall.EnabledWorlds");
		} else {
			for (World world : plugin.getServer().getWorlds()) {
				if (!world.getName().contains("_nether") && !world.getName().contains("_the_end")) {
					enabledWorlds.add(world.getName());
				}
			}
			plugin.getConfig().set("SnowFall.EnabledWorlds", enabledWorlds);
			plugin.saveConfig();
		}

	}

	public void addReplaceable(Material mat) {
		if (!canReplace.contains(mat)) {
			canReplace.add(mat);

			plugin.getConfig().set("SnowFall.CanReplace", MaterialToString(canReplace));
			plugin.saveConfig();
		}
	}

	public void addAccumulate(Material mat) {
		if (!canAccumulateOn.contains(mat)) {
			canAccumulateOn.add(mat);

			plugin.getConfig().set("SnowFall.CanAccumulateOn", MaterialToString(canAccumulateOn));
			plugin.saveConfig();
		}
	}

	public void addFallThrough(Material mat) {
		if (!canFallThrough.contains(mat)) {
			canFallThrough.add(mat);

			plugin.getConfig().set("SnowFall.CanFallThrough", MaterialToString(canFallThrough));
			plugin.saveConfig();
		}
	}

	private List<String> MaterialToString(List<Material> tempList) {
		List<String> retVal = new ArrayList<String>();
		if (tempList != null) {
			for (Material mat : tempList) {
				if (mat != null && StringUtils.isNotEmpty(mat.name())) {
					retVal.add(mat.name());
				}
			}
		}
		return retVal;
	}

	private List<Material> stringToMaterial(List<String> tempList) {
		List<Material> retVal = new ArrayList<Material>();
		if (tempList != null) {
			for (String str : tempList) {
				str = StringUtils.trimToEmpty(str);
				if (StringUtils.isNumeric(str)) {
					retVal.add(Material.getMaterial(Integer.parseInt(str)));
				} else if (!StringUtils.isBlank(str)) {
					retVal.add(Material.getMaterial(str));
				}
			}
		}
		return retVal;
	}

	public boolean debugEnabled() {
		return plugin.getConfig().getBoolean("debug", false);
	}

	public boolean isAccumulationEnabled() {
		return plugin.getConfig().getBoolean("SnowFall.AccumulationEnabled", true);
	}

	public boolean isMeltingEnabled() {
		return plugin.getConfig().getBoolean("SnowFall.MeltingEnabled", true);
	}

	public boolean meltDownCompletely() {
		return plugin.getConfig().getBoolean("SnowFall.MeltDownCompletely", false);
	}

	public float getChanceToAccumulate() {
		return (float) (plugin.getConfig().getDouble("SnowFall.AccumulationChance", 10) / 100);
	}

	public float getChanceToMelt() {
		return (float) (plugin.getConfig().getDouble("SnowFall.MeltingChance", 10) / 100);
	}

	public float getChanceToFallThrough() {
		return (float) (plugin.getConfig().getDouble("SnowFall.ChanceToFallThrough", 50) / 100);
	}

	public long getSnowFallDelay() {
		return plugin.getConfig().getLong("SnowFall.CheckEvery", 10);
	}

	public byte getMaxAccumulation() {
		return getMaxAccumulation(null);
	}

	public byte getMaxAccumulation(Material mat) {
		int retVal = plugin.getConfig().getInt("SnowFall.MaxAccumulationDefault", 7);
		if (mat != null) {
			retVal = plugin.getConfig().getInt("SnowFall.MaxAccumulationOverride." + mat.toString(), retVal);
		}
		if (retVal < 0) {
			retVal = 0;
		}

		return (byte) retVal;
	}

	public int getMinLightLevel() {
		return plugin.getConfig().getInt("SnowFall.MinLightLevelToMelt", 12);
	}

}
