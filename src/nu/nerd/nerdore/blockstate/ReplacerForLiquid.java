package nu.nerd.nerdore.blockstate;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * A {@link Replacer} implementation that updates the BlockState of liquid
 * source blocks.
 */
public class ReplacerForLiquid implements Replacer {
    // ------------------------------------------------------------------------
    /**
     * @see nu.nerd.nerdore.blockstate.Replacer#load(org.bukkit.configuration.ConfigurationSection,
     *      java.util.logging.Logger)
     */
    @Override
    public boolean load(ConfigurationSection section, Logger logger) {
        if (section != null) {
            _updatePhysics = section.getBoolean("update-physics");
        }
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * @see nu.nerd.nerdore.blockstate.Replacer#apply(BlockState, Random,
     *      StringBuilder)
     * 
     *      NOTE: BlockState.update(true,true) does not make liquids flow as of
     *      late Oct 2018 - early Nov 2018, per
     *      https://hub.spigotmc.org/jira/browse/SPIGOT-3623
     * 
     *      So we need to get sneaky. This also doesn't work:
     * 
     *      <pre>
     *      block.setType(block.getType(), true);
     *      </pre>
     * 
     *      It is, however, sufficient to clear the block to AIR and then set it
     *      back to liquid.
     */
    @Override
    public void apply(BlockState state, Random random, StringBuilder message) {
        if (_updatePhysics) {
            Block block = state.getBlock();
            Material type = block.getType();
            block.setType(Material.AIR, true);
            block.setType(type, true);
            if (message != null) {
                message.append(" (update physics)");
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "(update-physics: " + _updatePhysics + ")";
    }

    // ------------------------------------------------------------------------
    /**
     * If true, the liquid block receives a physics update.
     */
    protected boolean _updatePhysics;
} // class ReplacerForLiquid