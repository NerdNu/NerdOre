package nu.nerd.nerdore.blockstate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

// ----------------------------------------------------------------------------
/**
 * A {@link Replacer} implementation that updates the BlockState of spawners.
 */
public class ReplacerForSpawner implements Replacer {
    // ------------------------------------------------------------------------
    /**
     * @see nu.nerd.nerdore.blockstate.Replacer#load(org.bukkit.configuration.ConfigurationSection,
     *      java.util.logging.Logger)
     */
    @Override
    public boolean load(ConfigurationSection section, Logger logger) {
        if (section == null) {
            logger.severe("Spawner replacement-state not specified. A default spawner will be placed.");
            return true;
        }

        List<String> spawnedTypeNames = section.getStringList("spawned-types");
        if (spawnedTypeNames == null) {
            if (section.contains("spawned-types")) {
                logger.severe("Looks like spawned-types is not a list of strings.");
            }
        } else {
            for (String spawnedTypeName : spawnedTypeNames) {
                try {
                    _spawnedTypes.add(EntityType.valueOf(spawnedTypeName.toUpperCase()));
                } catch (IllegalArgumentException ex) {
                    logger.severe("Invalid spawner mob type: " + spawnedTypeName);
                    return false;
                }
            }
        }
        if (!loadProperty(section, logger, "required-player-range",
                          (x) -> {
                              _requiredPlayerRange = x;
                          },
                          (sec, key) -> {
                              return sec.getInt(key);
                          },
                          (x) -> (x >= 1))) {
            return false;
        }
        if (!loadProperty(section, logger, "min-delay-ticks",
                          (x) -> {
                              _minDelayTicks = x;
                          },
                          (sec, key) -> {
                              return sec.getInt(key);
                          },
                          (x) -> (x >= 1))) {
            return false;
        }
        if (!loadProperty(section, logger, "max-delay-ticks",
                          (x) -> {
                              _maxDelayTicks = x;
                          },
                          (sec, key) -> {
                              return sec.getInt(key);
                          },
                          (x) -> _minDelayTicks == null || (x >= _minDelayTicks))) {
            return false;
        }
        if (!loadProperty(section, logger, "spawn-count",
                          (x) -> {
                              _spawnCount = x;
                          },
                          (sec, key) -> {
                              return sec.getInt(key);
                          },
                          (x) -> (x >= 1))) {
            return false;
        }
        if (!loadProperty(section, logger, "spawn-range",
                          (x) -> {
                              _spawnRange = x;
                          },
                          (sec, key) -> {
                              return sec.getInt(key);
                          },
                          (x) -> (x >= 1))) {
            return false;
        }
        if (!loadProperty(section, logger, "max-nearby-entities",
                          (x) -> {
                              _maxNearbyEntities = x;
                          },
                          (sec, key) -> {
                              return sec.getInt(key);
                          },
                          (x) -> (x >= 1))) {
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * @see nu.nerd.nerdore.blockstate.Replacer#apply(BlockState, Random,
     *      StringBuilder)
     */
    @Override
    public void apply(BlockState state, Random random, StringBuilder message) {
        CreatureSpawner spawnerState = (CreatureSpawner) state;
        if (message != null) {
            message.append(" (");
        }
        String separator = "";
        if (!_spawnedTypes.isEmpty()) {
            EntityType newType = _spawnedTypes.get(random.nextInt(_spawnedTypes.size()));
            spawnerState.setSpawnedType(newType);
            if (message != null) {
                message.append(separator).append(newType);
                separator = ", ";
            }
        }
        if (_requiredPlayerRange != null) {
            spawnerState.setRequiredPlayerRange(_requiredPlayerRange);
            if (message != null) {
                message.append(separator).append("required-player-range: ").append(_requiredPlayerRange);
                separator = ", ";
            }
        }
        if (_minDelayTicks != null) {
            spawnerState.setMinSpawnDelay(_minDelayTicks);
            if (message != null) {
                message.append(separator).append("min-delay-ticks: ").append(_minDelayTicks);
                separator = ", ";
            }
        }
        if (_maxDelayTicks != null) {
            spawnerState.setMaxSpawnDelay(_maxDelayTicks);
            if (message != null) {
                message.append(separator).append("max-delay-ticks: ").append(_maxDelayTicks);
                separator = ", ";
            }
        }
        if (_spawnCount != null) {
            spawnerState.setSpawnCount(_spawnCount);
            if (message != null) {
                message.append(separator).append("spawn-count: ").append(_spawnCount);
                separator = ", ";
            }
        }
        if (_spawnRange != null) {
            spawnerState.setSpawnRange(_spawnRange);
            if (message != null) {
                message.append(separator).append("spawn-range: ").append(_spawnRange);
                separator = ", ";
            }
        }
        if (_maxNearbyEntities != null) {
            spawnerState.setMaxNearbyEntities(_maxNearbyEntities);
            if (message != null) {
                message.append(separator).append("max-nearby-entities: ").append(_maxNearbyEntities);
                separator = ", ";
            }
        }
        if (message != null) {
            message.append(')');
        }
        state.update();
    }

    // ------------------------------------------------------------------------
    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        String types = _spawnedTypes.isEmpty() ? "any"
                                               : _spawnedTypes.stream()
                                               .map(t -> t.toString())
                                               .collect(Collectors.joining(","));
        StringBuilder s = new StringBuilder();
        s.append("(spawned-types: ").append(types);
        if (_requiredPlayerRange != null) {
            s.append(", required-player-range: ").append(_requiredPlayerRange);
        }
        if (_minDelayTicks != null) {
            s.append(", min-delay-ticks: ").append(_minDelayTicks);
        }
        if (_maxDelayTicks != null) {
            s.append(", max-delay-ticks: ").append(_maxDelayTicks);
        }
        if (_spawnCount != null) {
            s.append(", spawn-count: ").append(_spawnCount);
        }
        if (_spawnRange != null) {
            s.append(", spawn-range: ").append(_spawnRange);
        }
        if (_maxNearbyEntities != null) {
            s.append(", max-nearby-entities: ").append(_maxNearbyEntities);
        }
        s.append(')');
        return s.toString();
    }

    // ------------------------------------------------------------------------
    /**
     * A list of potential entity types that the spawner could be configured to
     * spawn.
     * 
     * One type will be selected at random. To bias the selection towards a
     * specific type, add it to the list more than once.q
     */
    protected ArrayList<EntityType> _spawnedTypes = new ArrayList<>();

    /**
     * The maximum range a player can be for the spawner to be active.
     */
    protected Integer _requiredPlayerRange;

    /**
     * The minimum delay in ticks until the next round of spawns.
     */
    protected Integer _minDelayTicks;

    /**
     * The maximum delay in ticks until the next round of spawns.
     */
    protected Integer _maxDelayTicks;

    /**
     * The number of mobs to attempt to spawn.
     */
    protected Integer _spawnCount;

    /**
     * The radius of the horizontal square around the spawner where mobs spawn,
     * measured in blocks.
     */
    protected Integer _spawnRange;

    /**
     * The maximum number of similar entities within spawning range of the
     * spawner without it shutting down.
     */
    protected Integer _maxNearbyEntities;

} // class ReplacerForSpawner