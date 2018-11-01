package nu.nerd.nerdore;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import nu.nerd.nerdore.blockstate.Matcher;
import nu.nerd.nerdore.blockstate.MatcherForLiquid;
import nu.nerd.nerdore.blockstate.MatcherForSpawner;
import nu.nerd.nerdore.blockstate.Replacer;
import nu.nerd.nerdore.blockstate.ReplacerForLiquid;
import nu.nerd.nerdore.blockstate.ReplacerForSpawner;

// ----------------------------------------------------------------------------
/**
 * A rule that clears a block by replacing it with another material.
 */
public class ClearRule extends Rule {
    // ------------------------------------------------------------------------
    /**
     * Create a {@link Matcher} instance appropriate to the specified Material.
     * 
     * @param material the Material.
     * @return the Matcher or null.
     */
    public static Matcher createBlockStateMatcher(Material material) {
        if (material == Material.SPAWNER) {
            return new MatcherForSpawner();
        } else if (material == Material.LAVA || material == Material.WATER) {
            return new MatcherForLiquid();
        }
        return null;
    }

    // ------------------------------------------------------------------------
    /**
     * Create a {@link Replacer} instance appropriate to the specified Material.
     * 
     * @param material the Material.
     * @return the Replacer or null.
     */
    public static Replacer createBlockStateReplacer(Material material) {
        if (material == Material.SPAWNER) {
            return new ReplacerForSpawner();
        } else if (material == Material.LAVA || material == Material.WATER) {
            return new ReplacerForLiquid();
        }
        return null;
    }

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * 
     * @param section the configuration section to load.
     */
    public ClearRule(ConfigurationSection section) {
        load(section);

        Logger logger = NerdOre.PLUGIN.getLogger();
        _removedMaterial = new CustomMaterial(section.getString("block", ""));
        _matcher = createBlockStateMatcher(_removedMaterial.getType());

        ConfigurationSection blockStateSection = section.getConfigurationSection("block-state");
        if (_matcher == null) {
            if (blockStateSection != null) {
                logger.severe("block-state for " + _removedMaterial.getType() + " will be ignored.");
            }
        } else {
            if (!_matcher.load(blockStateSection, logger)) {
                logger.severe("Failed to load block-state for " + _removedMaterial.getType() + ".");
                _matcher = null;
            }
        }

        _replacementMaterial = new CustomMaterial(section.getString("replacement", "STONE"));
        ConfigurationSection replacementStateSection = section.getConfigurationSection("replacement-state");
        _replacer = createBlockStateReplacer(_replacementMaterial.getType());
        if (_replacer == null) {
            if (replacementStateSection != null) {
                logger.severe("replacement-state for " + _replacementMaterial.getType() + " will be ignored.");
            }
        } else {
            if (!_replacer.load(replacementStateSection, logger)) {
                logger.severe("Failed to load replacement-state for " + _replacementMaterial.getType() + ".");
                _replacer = null;
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if this ClearRule is applicable to the specified Block.
     * 
     * @param block the Block.
     * @return true if this ClearRule is applicable to the specified Block.
     */
    public boolean matches(Block block) {
        return _removedMaterial.getType() == block.getType() &&
               (_matcher == null || _matcher.test(block.getState()));
    }

    // ------------------------------------------------------------------------
    /**
     * Apply this ClearRule to the specified Block.
     * 
     * @param block the Block.
     * @param random the RNG to use.
     */
    public void apply(Block block, Random random) {
        block.setType(_replacementMaterial.getType());
        if (_replacer != null) {
            _replacer.apply(block.getState(), random);
        }
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
        String matcherDescription = _matcher == null ? "" : " " + _matcher.toString();
        String replacerDescription = _replacer == null ? "" : " " + _replacer.toString();
        return String.format("Clear %4.1f%% of %s%s with %s%s %s.",
                             getProbability() * 100,
                             getRemovedMaterial().getType().toString(),
                             matcherDescription,
                             getReplacementMaterial().getType().toString(),
                             replacerDescription,
                             super.toString());
    }

    // ------------------------------------------------------------------------
    /**
     * The material that will be removed.
     */
    protected CustomMaterial _removedMaterial;

    /**
     * If not null, matches the block state of removeable blocks.
     */
    protected Matcher _matcher;

    /**
     * The replacement material that will be added.
     */
    protected CustomMaterial _replacementMaterial;

    /**
     * If not null, holds and applies the new block state of the replacement
     * block.
     */
    protected Replacer _replacer;
} // class ClearRule
