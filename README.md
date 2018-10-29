NerdOre
=======
NerdOre is a Bukkit plugin that replaces blocks and generates ores.

The plugin requires a minimum Bukkit API version of "1.13". It processes 
a square, centred on the origin of a world, one chunk at a time, first
replacing blocks and then generating ore deposits. You can define a
default configuration for all worlds, or specific configurations on a
per-world basis. The plugin can only process one world at a time.

This plugin uses ore deposit generation algorithms derived from 
[OrePlus](https://github.com/jaquadro/OrePlus). As of Bukkit 1.13,
the `BlockPopulator` interface is so idiosyncratic that I could not
find a reliable way to update the OrePlus plugin to work with the
current server version. Eschewing `BlockPopulator` and loading
already-generated chunks works without the hassle and affords extra
convenience, in that the world can be processed multiple times with
different configurations.


Usage Examples
--------------
 * List rules in the current world:
```
/nerdore rules
```

 * To generate ores in the nether:
```
/mv tp world_nether
/nerdore index 0
/nerdore start
```

 * If the server restarts before processing is complete, continue processing
   from where NerdOre left off:
```
/nerdore start
```


Command Reference
-----------------

 * `/nerdore help` - Show usage help.
 * `/nerdore reload` - Reload the configuration.
 * `/nerdore notify` - Toggle notification broadcasts.
 * `/nerdore debug` - Toggle debug logging.
 * `/nerdore start` - Start processing.
 * `/nerdore stop` - Stop processing.
 * `/nerdore status` - Show the current running state, side and index.
 * `/nerdore period [<num>]` - Set or show the period in ticks.
 * `/nerdore seed [<num>]` - Set or show the ore generation seed.
 * `/nerdore side [<num>]` - Set or show the side length in blocks. Note 
   that the side value is the *"diameter"*, not the *"radius"* of the square.
 * `/nerdore index [<num>]` - Set or show the next converted index 
   (non-negative) in the current world.
 * `/nerdore rules` - List the rules for the player's current world.


Processing Rule Configuration
-----------------------------
### Overview

The `rules` section of `config.yml` defines the processing steps that are
performed in each processed chunk. The `rules` section can contain multiple
sub-sections named after the worlds that they affect, as well as a `default`
sub-section, that contains rules that affect worlds that do not have their
own sub-section.

The `rules` section has this structure (by example):
```
rules:
  default:
    # Rules that apply in any worlds that don't have explicit rule sections...
    
  world:
    # Rules that apply in the overworld...

  world_nether:
    # Rules that apply in the nether...
```

The following types of rules are supported:

 * Clear rules: these clear blocks of a specified type by replacing them with
   another type of block.

 * Generate rules: these generate ore deposits of a specified type of block.

Clear and Generate rules allow substantially the same configuration settings
that were supported by OrePlus. But pay attention to their descriptions below
for subtle differences and additions.

### Common Properties of Clear and Ore Rules

Clear and Generate rules both support properties with the following names: 
`enabled`, `min-height`, `max-height`, `probability`, `biomes` and `block`.
Note, however, that the interpretation of some of these properties - 
particularly `probability` - is different, depending on the type of the rule.


### Clear Rules

Clear rules have the following properties:

| Property      | Default  | Description |
| :---          | :---     | :--- |
| `enabled`     | true     | Whether the rule is enabled (`true` or `false`). |
| `min-height`  | 0        | |
| `max-height`  | 0        | |
| `probability` | 1.0      | |
| `biomes`      | `[]`     | A list of biomes where the rule applies. If empty, the rule affects all biomes. |

Example: replace half of the coal ore blocks between Y0 and Y64 (includsive)
with clay blocks:
```
rules:
  world:
    clear:
    - block: COAL_ORE
      replacement: CLAY
      probability: 0.5
      min-height: 0
      max-height: 64
```


### Generate Rules

Generate rules have the following properties:

| Property      | Default      | Description |
| :---          | :---         | :--- |
| `block`       |              | The [Bukkit Material](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) of the type of ore to generate. |
| `min-size`    | 8            | The minimum number of blocks to place. |
| `max-size`    | `min-size`   | The maximum number of blocks to place. If less than `min-size`, it will be set to `min-size`. |
| `min-rounds`  | 1            | The minimum number of times to attempt to generate a deposit, per chunk. |
| `max-rounds`  | `min-rounds` | The maximum number of times to attempt to generate a deposit, per chunk. If less than `min-rounds`, it will be set to `min-rounds`. |
| `enabled`     | true         | Whether the rule is enabled (`true` or `false`). |
| `min-height`  | 0            | The minimum Y coordinate of the centroid of the deposit, in the range [0,255]. |
| `max-height`  | 64           | The minimum Y coordinate of the centroid of the deposit, in the range [0,255]. If less than `min-height`, it will be set to `min-height`. |
| `probability` | 1.0          | The probability, in the range [0.0,1.0] that a given round/attempt will generate an ore deposit. |
| `biomes`      | `[]`         | A list of biomes where the rule applies. If empty, the rule affects all biomes. |

Example: Generate 4-10 block gold ore deposits 30 times per chunk from Y5 to Y60
in desert biomes:
```
    - block: GOLD_ORE
      min-size: 4
      max-size: 10
      min-rounds: 30
      min-height: 5
      max-height: 60
      biomes:
      - DESERT
      - DESERT_HILLS
      - DESERT_LAKES
```


General Configuration
---------------------
To avoid mistakes, settings marked with `*` should be edited using the 
corresponding command, rather than directly in `config.yml`.

| Setting | Description |
| :--- | :--- |
| `debug.config` | If true, log the configuration on reload. |
| `debug.processing` | If true, log processing steps. |
| `debug.deposits` | If true, log coordinates and type of generated ore deposits. |
| `notify` | If true, periodic progress notifications are sent to players with the `nerdore.notify` permission. |
| `notify-ticks` | The period, in ticks (1/20th of a second), between progress notifications. |
| `period-ticks` | The period, in ticks, between processing distinct chunks. |
| `seed`* - Seed used to generate ores. If 0, the seed of the current world is used instead. |
| `side`* - The side length of the square of blocks to process. |
| `world`* - The current world being processed. |
| `indices` - A map from world name to most recently processed index in that world. |
| `replaceable-materials` - A list of [Bukkit API Material](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) names of block types that can be replaced by generated ores. 


Permissions
-----------

 * `nerdore.admin` - Permission to administer the plugin.
 * `nerdore.notify` - Permission to receive progress notification broadcasts.

