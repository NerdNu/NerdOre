package nu.nerd.nerdore.blockstate;

import java.util.logging.Logger;

import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;

import nu.nerd.nerdore.ClearRule;

// ----------------------------------------------------------------------------
/**
 * A predicate-like interface that returns true for blocks that should be
 * replaced.
 */
public interface Matcher {
    // ------------------------------------------------------------------------
    /**
     * Load configuration from the specified section that represents the
     * required attributes of a BlockState instance for it to match the
     * predicate.
     * 
     * @param section the section; can be null.
     * @param logger a Logger to log errors.
     * @return true if successful.
     */
    public boolean load(ConfigurationSection section, Logger logger);

    // ------------------------------------------------------------------------
    /**
     * Return true if the BlockState should be affected by the {@link ClearRule}
     * that owns this Matcher.
     * 
     * @param state the BlockState.
     * @param message if not-null, this StringBuilder is used to log details of
     *        the matching BlockState.
     */
    public boolean matches(BlockState state, StringBuilder message);
} // class Matcher