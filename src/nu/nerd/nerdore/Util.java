package nu.nerd.nerdore;

import java.util.Random;

// ----------------------------------------------------------------------------
/**
 * Utility functions.
 */
public class Util {
    // ------------------------------------------------------------------------
    /**
     * Return a random integer in the range [min,max].
     *
     * @param random the Random to use.
     * @param min the minimum possible value.
     * @param max the maximum possible value.
     * @return a random integer in the range [min,max].
     */
    public static int nextInt(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

} // class Util