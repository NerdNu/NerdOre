package nu.nerd.nerdore;

import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * A rule that clears a block by replacing it with another material.
 */
public class ClearRule extends Rule {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * 
     * @param section the configuration section to load.
     */
    public ClearRule(ConfigurationSection section) {
        load(section);
        _removedMaterial = new CustomMaterial(section.getString("block", ""));
        _replacementMaterial = new CustomMaterial(section.getString("replacement", "STONE"));
    }

    // ------------------------------------------------------------------------
    /**
     * @see Rule#isValid()
     */
    @Override
    public boolean isValid() {
        return _removedMaterial.isValid() && _replacementMaterial.isValid();
    }

    // ------------------------------------------------------------------------
    /**
     * Return the material that will be removed.
     * 
     * @return the material that will be removed.
     */
    public CustomMaterial getRemovedMaterial() {
        return _removedMaterial;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the replacement material that will be added.
     * 
     * @return the replacement material that will be added.
     */
    public CustomMaterial getReplacementMaterial() {
        return _replacementMaterial;
    }

    // ------------------------------------------------------------------------
    /**
     * Return a string representation of this rule.
     * 
     * @return a string representation of this rule.
     */
    @Override
    public String toString() {
        return String.format("Clear %4.1f%% of %s with %s %s.",
                             getProbability() * 100,
                             getRemovedMaterial().getType().toString(),
                             getReplacementMaterial().getType().toString(),
                             super.toString());
    }

    // ------------------------------------------------------------------------
    /**
     * The material that will be removed.
     */
    protected CustomMaterial _removedMaterial;

    /**
     * The replacement material that will be added.
     */
    protected CustomMaterial _replacementMaterial;
} // class ClearRule
