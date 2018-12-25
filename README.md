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
 * `/nerdore location` - Show the player's current world and biome.


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

Clear rules are defined in a `clear` sub-section of the section corresponding
to the world name (or `default`) in which they apply. The `clear` sub-section
is a list of dictionaries, each of which defines one processing action; these
actions are applied to the chunk in the order they are defined in the
configuration, skipping any rules that are not enabled.

Clear rules have the following properties:

| Property            | Default  | Description |
| :---                | :---     | :--- |
| `block`             |          | The [Bukkit Material](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) of the type of block to clear. |
| `block-state`       | {}       | A map of properties that place further restrictions on the state of blocks that will be matched for replacement, beyond just their Material. If empty or omitted, only the block's Material is considered. The properties listed under `block-state` depend on the `block`. See the tables below. |
| `replacement`       | STONE    | The [Bukkit Material](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) of the type of block to place there instead. |
| `replacement-state` | {}       | A map that defines properties of the replacement block beyond just its Material. The properties listed under `replacement-state` depend on the `replacement`. See the tables below. |
| `enabled`           | true     | Whether the rule is enabled (`true` or `false`). |
| `min-height`        | 0        | The minimum Y coordinate of affected blocks, in the range [0,255]. |
| `max-height`        | 255      | The maximum Y coordinate of affected blocks, in the range [0,255]. If less than `min-height`, it will be set to `min-height`.|
| `probability`       | 1.0      | The  probability, in the range [0.0,1.0], that a given *block* will be replaced. |
| `biomes`            | `[]`     | A list of biomes where the rule applies. If empty, the rule affects all biomes. |
| `logged`            | false    | If true, the coordinates, old Material and new Material of each block cleared are logged to the console. (Be careful: this could result in a huge volume of log messages.) |

Example: replace half of the coal ore blocks between Y0 and Y64 (inclusive)
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

#### `block-state` and `replacement-state` for Spawners

When `block` is `SPAWNER`, `block-state` can contain the following properties:

| Property                | Type     | Description |
| :---                    | :---     | :--- |
| `spawned-types`         | list     | A list of [Bukkit EntityType](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html)s of entities that the spawner can spawn. If empty, the rule matches all spawners for replacement, regardless of what they spawn. If the list is not empty, then only spawners that spawn one of the listed types will be replaced. |

When `replacement` is `SPAWNER`, `replacement-state` defines
[CreatureSpawner BlockState](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/CreatureSpawner.html)
properties for the `SPAWNER` block that will be placed. The following properties
are supported:

| Property                | Type    | Description |
| :---                    | :---    | :--- |
| `spawned-types`         | list    | If not the empty list, then a [Bukkit EntityType](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html) is selected at random from the list, and the spawner is configured to spawn that type. The list can contain duplicate entries, allowing the random selection to be biased to those types that appear more frequently. |
| `required-player-range` | integer | If set, then this configures the maximum range a player can be for the spawner to be active as a whole number of blocks. |
| `min-delay-ticks`       | integer | If set, then this configures the minimum delay in ticks until the next round of spawns. |
| `max-delay-ticks`       | integer | If set, then this configures the maximum delay in ticks until the next round of spawns. |
| `spawn-count`           | integer | If set, then this configures the number of mobs to attempt to spawn. |
| `spawn-range`           | integer | If set, then this configures the radius of the horizontal square around the spawner where mobs spawn, measured in blocks. |
| `max-nearby-entities`   | integer | If set, then this configures the maximum number of similar entities within spawning range of the spawner without it shutting down. |

It is envisaged that the most common use of `block-state` and `replacement-state`
will be to change the type of spawned mobs by replacing `SPAWNER` with `SPAWNER`.
For example, to turn 25% of skeleton and zombie spawners in the overworld into
creeper spawners or husk spawners in a 2:1 ratio:
```
rules:
  world:
    clear:
    - block: SPAWNER
      block-state:
        spawned-types: [SKELETON, ZOMBIE]
      probability: 0.25
      replacement: SPAWNER
      replacement-state:
        spawned-types: [CREEPER, CREEPER, HUSK]
```

However, there is no requirement to replace a spawner with another spawner.
You could turn certain types of spawners into (for example) air, or you could
convert a specific marker block (e.g. stained glass) into a spawner. For a more
concrete example, here is the rule to turn the cauldron in witch huts (in swamp
biomes) into a witch spawner:
```
rules:
  world:
    clear:
    - block: CAULDRON
      replacement: SPAWNER
      replacement-state:
        spawned-types: [WITCH]
      biomes: [SWAMP, SWAMP_HILLS]
```


#### `block-state` and `replacement-state` for Water or Lava

When `block` is `WATER` or `LAVA`, `block-state` can contain the following properties:

| Property                | Type     | Description |
| :---                    | :---     | :--- |
| `unstable`              | boolean  | If true, only unstable source blocks (those that could flow on a block update) are targeted for replacement. For the purpose of this check, a liquid block is considered unstable if it has AIR or CAVE_AIR to the side of it, or below it. If false, all blocks of the matching Material (`WATER` or `LAVA`) will be replaced, subject to the other matching criteria of the rule (e.g. biome). |

When `replacement` is `WATER` or `LAVA`, `replacement-state` can have the following properties:

| Property                | Type    | Description |
| :---                    | :---    | :--- |
| `update-physics`        | boolean | If true, the replacement block will receive a physics update, thereby causing liquids to flow. |

For example, to make lava blocks flow in the nether:
```
rules:
  world_nether:
    clear:
    - block: LAVA
      block-state:
        unstable: true
      replacement: LAVA
      replacement-state:
        update-physics: true
```


### Generate Rules

Generate rules are defined in a `generate` sub-section of the section
corresponding to the world name (or `default`) in which they apply. The
`generate` sub-section is a list of dictionaries, each of which defines one
processing action; these actions are applied to the chunk in the order they 
are defined in the configuration, skipping any rules that are not enabled.

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
| `max-height`  | 255          | The maximum Y coordinate of the centroid of the deposit, in the range [0,255]. If less than `min-height`, it will be set to `min-height`. |
| `probability` | 1.0          | The probability, in the range [0.0,1.0], that a given *attempt* will generate an ore deposit. |
| `biomes`      | `[]`         | A list of biomes where the rule applies. If empty, the rule affects all biomes. |
| `logged`      | false        | If true, details of each generated deposit are logged to the console. |

Example: generate 4-10 block gold ore deposits 30 times per chunk from Y5 to Y60
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
| `notify` | If true, periodic progress notifications are sent to players with the `nerdore.notify` permission. |
| `notify-ticks` | The period, in ticks (1/20th of a second), between progress notifications. |
| `period-ticks` | The period, in ticks, between processing distinct chunks. |
| `seed`* | Seed used to generate ores. If 0, the seed of the current world is used instead. |
| `side`* | The side length of the square of blocks to process. |
| `world`* | The current world being processed. |
| `indices` | A map from world name to most recently processed index in that world. |
| `replaceable-materials` | A list of [Bukkit API Material](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) names of block types that can be replaced by generated ores. 


Permissions
-----------

 * `nerdore.admin` - Permission to administer the plugin.
 * `nerdore.notify` - Permission to receive progress notification broadcasts.

