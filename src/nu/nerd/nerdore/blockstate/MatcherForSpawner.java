package nu.nerd.nerdore.blockstate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

// ----------------------------------------------------------------------------
/**
 * A {@link Matcher} implementation that matches spawners that should be
 * replaced.
 */
public class MatcherForSpawner implements Matcher {
    // ------------------------------------------------------------------------
    /**
     * @see nu.nerd.nerdore.blockstate.Matcher#load(org.bukkit.configuration.ConfigurationSection,
     *      java.util.logging.Logger)
     */
    @Override
    public boolean load(ConfigurationSection section, Logger logger) {
        if (section == null) {
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
                    logger.severe("Invalid spawner entity type: " + spawnedTypeName);
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * @see Matcher#matches(BlockState, StringBuilder)
     */
    @Override
    public boolean matches(BlockState state, StringBuilder message) {
        CreatureSpawner spawnerState = (CreatureSpawner) state;
        if (_spawnedTypes.isEmpty() || _spawnedTypes.contains(spawnerState.getSpawnedType())) {
            if (message != null) {
                message.append(" (").append(spawnerState.getSpawnedType()).append(')');
            }
            return true;
        }
        return false;
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
        return "(spawned-types: " + types + ")";
    }

    // ------------------------------------------------------------------------
    /**
     * The set of spawned EntityTypes that will match.
     */
    protected LinkedHashSet<EntityType> _spawnedTypes = new LinkedHashSet<>();
} // class MatcherForSpawner