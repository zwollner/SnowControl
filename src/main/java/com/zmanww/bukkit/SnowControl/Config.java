package com.zmanww.bukkit.SnowControl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;

public class Config {
	protected static final SnowControl plugin = SnowControl.plugin;
	private static Config instance;

	public List<Material> canReplace = new ArrayList<Material>();
	public List<Material> canFallThrough = new ArrayList<Material>();
	public List<Material> canAccumulateOn = new ArrayList<Material>();

	private Config() {
		if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
			plugin.saveDefaultConfig();
		}
		plugin.getConfig().options().copyDefaults(true);
		loadKeys();
	}

	private void loadKeys() {
		canFallThrough = stringToMaterial(plugin.getConfig().getStringList("SnowFall.CanFallThrough"));
		canFallThrough.add(Material.AIR);

		canReplace = stringToMaterial(plugin.getConfig().getStringList("SnowFall.CanReplace"));
		canReplace.add(Material.AIR);

		canAccumulateOn = stringToMaterial(plugin.getConfig().getStringList("SnowFall.CanAccumulateOn"));

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

	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}
		return instance;
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
			retVal = plugin.getConfig().getInt("SnowFall.MaxAccumulationOverride." + mat.toString(), 7);
		}
		if (retVal < 0) {
			retVal = 0;
		}

		return (byte) retVal;
	}

}
