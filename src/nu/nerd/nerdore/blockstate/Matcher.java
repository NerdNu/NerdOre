package nu.nerd.nerdore.blockstate;

import java.util.function.Predicate;
import java.util.logging.Logger;

import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * A predicate interface that returns true for blocks that should be replaced.
 */
public interface Matcher extends Predicate<BlockState> {
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

} // class Matcher