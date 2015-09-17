# SnowControl

Makes snow more "real" and configurable, by allowing it to fall "through" leaves (and any other object you want), in addition to allowing snow to accumulate and melt.

Also, when breaking a block with snow on it, the snow will "fall" (similar to sand or gravel) and pile up on any snow that's underneath.

Snow will only accumulate while it's snowing, and can pile up as much as you want.

Snow will also melt (configurable) during the day if it's not snowing. It can melt down to one layer (default), or down to nothing.

![Screenshot](https://dl.dropbox.com/u/85882/SC-screen.png)
#### WARNING 
This plugin is very CPU intensive, and if you have a very large server with many players always on, this plugin may cause lag.
Also, if your server hardware is old and out dated it may also have issues running this plugin.
Having said this, my real server where this is running is a Dual core Xeon @ 3.00GHz with 4GB ram, and haven't had any issues.

### Installation
Download jar and place it in your plugins folder. (http://wiki.bukkit.org/Installing_Plugins)

[![Build Status](https://buildhive.cloudbees.com/job/zwollner/job/SnowControl/badge/icon)](https://buildhive.cloudbees.com/job/zwollner/job/SnowControl/) There is always a development version <a href="https://buildhive.cloudbees.com/job/zwollner/job/SnowControl/lastSuccessfulBuild/com.zmanww$SnowControl/">HERE</a>. Please keep in mind that this build may not be working 100%, or even tested, use at your own risk.





### Commands/Permissions
```YAML
commands:
  snowcontrol:
    description: Primary command for SnowControl.
    aliases: [sc]
    usage:
      - /<command> reload - Force the plugin to reload the config file.
      - /<command> addReplace - Adds next clicked block to 'CanReplace' list.
      - /<command> addAccum - Adds next clicked block to 'CanAccumulateOn' list.
      - /<command> addFall - Adds next clicked block to 'CanFallThrough' list.
permissions:
  snowcontrol.reload:
    description: Force the plugin to reload the config file.
    default: op
  snowcontrol.addReplace:
    description: Adds next clicked block to 'CanReplace' list.
    default: op
  snowcontrol.addAccum:
    description: Adds next clicked block to 'CanAccumulateOn' list.
    default: op
  snowcontrol.addFall:
    description: Adds next clicked block to 'CanFallThrough' list.
    default: op
```

### Configuration Defaults
"SnowFall.CheckEvery" is how often each snow eligible block will be checked. It is not recommended to set less than 5 seconds, or if you have a large server with many players, the higher the better.
And then every time a block is checked, there is a chance it can accumulate, or melt based on the config below.
```YAML
###
#  Settings for Snow Fall
#  blocks can be listed as ID's or their names, 
#  but names must be exactly as seen here: 
#  http://jd.bukkit.org/apidocs/org/bukkit/Material.html
###
SnowFall: #this is only in biomes where snow falls naturally
  CheckEvery: 10 #number in seconds
  AccumulationEnabled: true
  AccumulationChance: 1 #percent 0-100
  MaxAccumulationDefault: 8 # number of 'layers' of snow
  MaxAccumulationOverride: # Must match marerial name exactly
    LEAVES: 3
  MeltingEnabled: true
  MeltingChance: 0.5 #percent 0-100
  MeltDownCompletely: false #If true, snow could disappear during the day
  MinLightLevelToMelt: 11
  ChanceToFallThrough: 80 #percent 0-100
  CanFallThrough: #Air is always assumed
    - LEAVES
  CanReplace: #Air is always assumed
    -
  CanAccumulateOn:
    - BEDROCK
    - BRICK
    - CACTUS
    - CLAY
    - CLAY_BRICK
    - DIAMOND
    - DIAMOND_BLOCK
    - DIAMOND_ORE
    - DIRT
    - EMERALD
    - EMERALD_BLOCK
    - EMERALD_ORE
    - ENDER_STONE
    - GOLD_BLOCK
    - GOLD_ORE
    - GRASS
    - HUGE_MUSHROOM_1
    - HUGE_MUSHROOM_2
    - IRON_BLOCK
    - IRON_ORE
    - LAPIS_BLOCK
    - LAPIS_ORE
    - LEAVES
    - LOG
    - MELON_BLOCK
    - MOSSY_COBBLESTONE
    - NETHER_BRICK
    - NETHERRACK
    - OBSIDIAN
    - PUMPKIN
    - SAND
    - SANDSTONE
    - SMOOTH_BRICK
    - SOIL
    - STONE
    - WOOD
    - WOOL
```

#### Wish I had time to Do List
* Make thrown snowballs create snow patches.
* Make thrown snowballs cause damage.
* Make thrown snowballs cause slowdown (freezing effect).
* Make deeper snow slow down players and/or mobs.
* Create custom snow related recipes (ideas welcome)
* Enable for snow on mountains not in a snowy biome

##
*This plugin utilizes Hidendra's plugin metrics system, which means some anonymous information will be collected and sent to mcstats.org.
This allows me to track how many servers are actually running the plugin, and the larger user base I have the more likely I am to add features.
If you wish to opt out of this service, it can be done by editing plugins/Plugin Metrics/config.yml*

<img src="http://i.mcstats.org/SnowControl/Global+Statistics.borderless.png">
##
