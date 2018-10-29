package nu.nerd.nerdore;

import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * A rule that generates ore deposits.
 */
public class OreRule extends Rule {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * 
     * @param section the configuration sectiopn to load.
     */
    public OreRule(ConfigurationSection section) {
        load(section);
        _material = new CustomMaterial(section.getString("block", ""));
        _minSize = section.getInt("min-size", 8);
        _maxSize = section.getInt("max-size", _minSize);
        _minRounds = section.getInt("min-rounds", 1);
        _maxRounds = section.getInt("max-rounds", _minRounds);

        // NOTE: invalid material name logged by CustomMaterial.
        if (_minSize < 1) {
            NerdOre.PLUGIN.getLogger().severe("min-size below 1: " + _minSize);
            _minSize = 1;
        }
        if (_maxSize < _minSize) {
            NerdOre.PLUGIN.getLogger().severe("max-size clamped to at least min-size.");
            _maxSize = _minSize;
        }

        if (_minRounds < 1) {
            NerdOre.PLUGIN.getLogger().severe("min-rounds below 1: " + _minRounds);
            _minRounds = 1;
        }
        if (_maxRounds < _minRounds) {
            NerdOre.PLUGIN.getLogger().severe("max-rounds clamped to at least min-rounds.");
            _maxRounds = 1;
        }
    }

    // ------------------------------------------------------------------------
    /**
     * @see Rule#isValid()
     */
    @Override
    public boolean isValid() {
        return _material.isValid();
    }

    // ------------------------------------------------------------------------
    /**
     * Return the material of generated ores.
     * 
     * @return the material of generated ores.
     */
    public CustomMaterial getMaterial() {
        return _material;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the minimum size of the deposit.
     * 
     * @return the minimum size of the deposit.
     */
    public int getMinSize() {
        return _minSize;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the maximum size of the deposit.
     * 
     * @return the maximum size of the deposit.
     */
    public int getMaxSize() {
        return _maxSize;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the minimum number of times to attempt generation within each
     * chunk.
     * 
     * @return the minimum number of times to attempt generation within each
     *         chunk.
     */
    public int getMinRounds() {
        return _minRounds;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the maximum number of times to attempt generation within each
     * chunk.
     * 
     * @return the maximum number of times to attempt generation within each
     *         chunk.
     */
    public int getMaxRounds() {
        return _maxRounds;
    }

    // ------------------------------------------------------------------------
    /**
     * Return a string representation of this rule.
     * 
     * @return a string representation of this rule.
     */
    @Override
    public String toString() {
        String rounds = (getMinRounds() == getMaxRounds()) ? "" + getMinRounds()
                                                           : getMinRounds() + " to " + getMaxRounds();
        String size = (getMinSize() == getMaxSize()) ? "" + getMinSize()
                                                     : getMinSize() + " to " + getMaxSize();
        return String.format("Generate %4.1f%% of %s round(s) of %s size %s %s.",
                             getProbability() * 100,
                             rounds,
                             getMaterial().getType().toString(),
                             size,
                             super.toString());
    }

    // ------------------------------------------------------------------------
    /**
     * The material of generated ores.
     */
    protected CustomMaterial _material;

    /**
     * The minimum size of the deposit.
     */
    protected int _minSize;

    /**
     * The maximum size of the deposit.
     */
    protected int _maxSize;

    /**
     * The minimum number of times to attempt generation within each chunk.
     */
    protected int _minRounds;

    /**
     * The maximum number of times to attempt generation within each chunk.
     */
    protected int _maxRounds;
} // class OreRule
