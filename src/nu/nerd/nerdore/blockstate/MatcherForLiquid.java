package nu.nerd.nerdore.blockstate;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * A {@link Replacer} implementation that updates the BlockState of liquid
 * source blocks.
 * 
 * It requires that there is AIR or CAVE_AIR to the side of or below the block,
 * and that the block is a source block (level 0) rather than a flow.
 */
public class MatcherForLiquid implements Matcher {
    // ------------------------------------------------------------------------
    /**
     * @see nu.nerd.nerdore.blockstate.Matcher#load(org.bukkit.configuration.ConfigurationSection,
     *      java.util.logging.Logger)
     */
    @Override
    public boolean load(ConfigurationSection section, Logger logger) {
        if (section != null) {
            _unstable = section.getBoolean("unstable");
        }
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * @see Matcher#matches(BlockState, StringBuilder)
     */
    @Override
    public boolean matches(BlockState state, StringBuilder message) {
        if (!_unstable) {
            return true;
        }

        Levelled levelled = (Levelled) state.getBlockData();
        if (levelled.getLevel() != 0) {
            return false;
        }

        Block block = state.getBlock();
        Block[] checked = {
            block.getRelative(BlockFace.NORTH),
            block.getRelative(BlockFace.SOUTH),
            block.getRelative(BlockFace.WEST),
            block.getRelative(BlockFace.EAST),
            block.getRelative(BlockFace.DOWN) };
        for (Block neighbour : checked) {
            if (neighbour != null && (neighbour.getType() == Material.AIR ||
                                      neighbour.getType() == Material.CAVE_AIR)) {
                if (message != null) {
                    message.append(" (unstable)");
                }
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------
    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "(unstable: " + _unstable + ")";
    }

    // ------------------------------------------------------------------------
    /**
     * If true, only source blocks that could flow will be affected. If false,
     * all blocks of the affected material will be replaced.
     */
    protected boolean _unstable;
} // class MatcherForLiquid