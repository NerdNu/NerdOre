package nu.nerd.nerdore.blockstate;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * Represents changes to BlockState loaded from the configuration.
 * 
 * There is a distinct implementation of this interface for each replacement
 * Material.
 */
public interface Replacer {
    // ------------------------------------------------------------------------
    /**
     * Load configuration from the specified section that represents the new
     * attributes of a BlockState instance.
     * 
     * @param section the section; can be null.
     * @param logger a Logger to log errors.
     * @return true if successful.
     */
    public boolean load(ConfigurationSection section, Logger logger);

    // ------------------------------------------------------------------------
    /**
     * Apply the changes to the specified BlockState instance.
     * 
     * @param state the state of the block to alter.
     * @param random the RNG to use.
     */
    public void apply(BlockState state, Random random);

    // ------------------------------------------------------------------------
    /**
     * Common code for loading Replacer properties from a ConfigurationSection.
     * 
     * @param section the section.
     * @param logger the logger for warnings logged to console.
     * @param key the key within the section to load.
     * @param setter a function that sets the Replacer's field, given a value.
     * @param loader a function that loads a key from the configuration section.
     * @param validator a predicate that should return true if the loaded value
     *        is valid.
     * @return true if the property was loaded without an error, or was not set
     *         in the section; false otherwise.
     */
    default <T> boolean loadProperty(ConfigurationSection section, Logger logger,
                                     String key,
                                     Consumer<T> setter,
                                     BiFunction<ConfigurationSection, String, T> loader,
                                     Predicate<T> validator) {
        if (section.contains(key)) {
            T value = loader.apply(section, key);
            if (validator.test(value)) {
                setter.accept(value);
            } else {
                logger.severe("Invalid " + key + " value: " + value);
                return false;
            }
        }
        return true;
    }

} // class Replacer