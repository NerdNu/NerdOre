package nu.nerd.nerdore;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * Base class of {@link OreRule} and {@link ClearRule}.
 */
public abstract class Rule {
    // ------------------------------------------------------------------------
    /**
     * Load attribute of this Rule from the specified section.
     * 
     * @param section the ConfigurationSection.
     */
    protected void load(ConfigurationSection section) {
        _enabled = section.getBoolean("enabled", true);
        _minHeight = section.getInt("min-height", 0);
        _maxHeight = section.getInt("max-height", 64);
        _probability = section.getDouble("probability", 1.0);
        _affectedBiomes = loadBiomes(section.getStringList("biomes"));

        Logger logger = NerdOre.PLUGIN.getLogger();
        if (_minHeight < 0) {
            logger.severe("min-height below 0.");
            _minHeight = 0;
        }
        if (_maxHeight < _minHeight) {
            NerdOre.PLUGIN.getLogger().severe("max-height clamped to at least min-height.");
            _maxHeight = _minHeight;
        }
        if (_maxHeight > 255) {
            logger.severe("max-height above 255.");
            _maxHeight = 255;
        }
        if (_probability < 0.0 || _probability > 1.0) {
            logger.severe("probability clamped to [0.0,1.0].");
            _probability = Math.min(1.0, Math.max(0.0, _probability));
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if this rule is enabled.
     * 
     * @return true if this rule is enabled.
     */
    public boolean isEnabled() {
        return _enabled;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if this rule is valid (can be used to process a chunk).
     * 
     * @return true if this rule is valid.
     */
    public abstract boolean isValid();

    // ------------------------------------------------------------------------
    /**
     * Return the minimum Y coordinate of blocks affected by this rule.
     * 
     * @return the minimum Y coordinate of blocks affected by this rule.
     */
    public int getMinHeight() {
        return _minHeight;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the minimum Y coordinate of blocks affected by this rule.
     * 
     * @return the minimum Y coordinate of blocks affected by this rule.
     */
    public int getMaxHeight() {
        return _maxHeight;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the probability of a block being affected by the rule, in the
     * range [0.0,1.0].
     * 
     * @return the probability of a block being affected by the rule, in the
     *         range [0.0,1.0].
     */
    public double getProbability() {
        return _probability;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if the specified biome is affected by this rule.
     * 
     * @param biome the biome to check.
     * @return true if the specified biome is affected by this rule.
     */
    public boolean affectsBiome(Biome biome) {
        return _affectedBiomes.isEmpty() || _affectedBiomes.contains(biome);
    }

    // ------------------------------------------------------------------------
    /**
     * Load a set of Biomes from a configuration string list.
     * 
     * @param biomes the string list of biome names.
     * @return a non-null set of Biomes.
     */
    protected static Set<Biome> loadBiomes(List<String> biomes) {
        Set<Biome> result = EnumSet.noneOf(Biome.class);
        if (biomes != null) {
            for (String name : biomes) {
                try {
                    result.add(Biome.valueOf(name.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    NerdOre.PLUGIN.getLogger().severe("Invalid biome name: " + name);
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------------
    /**
     * Return a partial string representation of the rule attributes for
     * subclasses to use in their toString() implementations.
     * 
     * @return a partial string representation of the rule attributes for
     *         subclasses to use in their toString() implementations.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("for y in [").append(getMinHeight()).append(',');
        s.append(getMaxHeight()).append(']');
        s.append(" in ");
        if (_affectedBiomes.isEmpty()) {
            s.append("all biomes");
        } else {
            s.append(_affectedBiomes.stream().map(Biome::toString).collect(Collectors.joining(", ")));
        }

        if (!isEnabled()) {
            s.append(" (DISABLED)");
        }
        return s.toString();
    }

    // ------------------------------------------------------------------------
    /**
     * True if this rule is enabled.
     */
    protected boolean _enabled;

    /**
     * Minimum Y coordinate of affected blocks.
     */
    protected int _minHeight;

    /**
     * Maximum Y coordinate of affected blocks.
     */
    protected int _maxHeight;

    /**
     * The probability of a block being affected by the rule, in the range
     * [0.0,1.0].
     */
    protected double _probability;

    /**
     * The set of affected Biomes. If empty, all biomes are affected.
     */
    protected Set<Biome> _affectedBiomes;

} // class Rule
