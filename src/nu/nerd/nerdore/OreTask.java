package nu.nerd.nerdore;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

// ----------------------------------------------------------------------------
/**
 * This task processes one chunk at a time until the specified square centred on
 * the origin of the map is done.
 */
public class OreTask implements Runnable {
    // ------------------------------------------------------------------------
    /**
     * Set the ore generation seed.
     * 
     * @param seed the seed.
     */
    public void setSeed(long seed) {
        if (seed == 0) {
            World world = Bukkit.getWorld(NerdOre.CONFIG.WORLD);
            seed = world.getSeed();
            NerdOre.PLUGIN.getLogger().info("Using world seed: " + seed);
        } else {
            NerdOre.PLUGIN.getLogger().info("Using configured seed: " + seed);
        }

        _random.setSeed(seed);
        _blockRandom.setSeed(seed);
    }

    // ------------------------------------------------------------------------
    /**
     * Specify whether this task should do anything to chunks.
     *
     * @param running if true, chunks are processed.
     */
    public void setRunning(boolean running) {
        _running = running;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if chunks are being processed by this task.
     */
    public boolean isRunning() {
        return _running;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if all chunks have been processed.
     *
     * @return true if all chunks have been processed.
     */
    public boolean isComplete() {
        return NerdOre.CONFIG.getIndex() >= NerdOre.CONFIG.SIDE * NerdOre.CONFIG.SIDE;
    }

    // ------------------------------------------------------------------------
    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        ++_ticks;

        if (_running) {
            if (isComplete()) {
                statusUpdate();
                _running = false;
            } else {
                if (_ticks % NerdOre.CONFIG.PERIOD_TICKS == 0) {
                    processChunk();
                }

                if (_ticks % NerdOre.CONFIG.NOTIFY_TICKS == 0) {
                    statusUpdate();
                }
            }
        }
    } // run

    // ------------------------------------------------------------------------
    /**
     * Return the odd side dimension in chunks of the fully complete area
     * corresponding to the specified index.
     *
     * @return the odd side dimension in chunks of the fully complete area
     *         corresponding to the specified index.
     */
    public static int getSideOf(int index) {
        int side = (int) Math.sqrt(index);
        return (side & 1) == 0 ? side - 1 : side;
    }

    // ------------------------------------------------------------------------
    /**
     * Process the chunk corresponding to the current index.
     */
    protected void processChunk() {
        if (NerdOre.CONFIG.getIndex() < 0 || NerdOre.CONFIG.SIDE < 1) {
            NerdOre.PLUGIN.getLogger().severe("Index or side value is wonky. Giving up.");
            Bukkit.getServer().broadcast(ChatColor.GREEN + "NerdOre: Index or side value is wonky. Giving up.",
                                         "nerdore.notify");
            setRunning(false);
            return;
        }

        if (NerdOre.CONFIG.getIndex() == 0) {
            processChunk(0, 0);
        } else {
            int completedSide = getSideOf(NerdOre.CONFIG.getIndex());
            int newSide = completedSide + 2;

            // X/Z coordinate offset of NW corner of SIDE x SIDE square.
            int coordOffset = -newSide / 2;

            int relativeIndex = NerdOre.CONFIG.getIndex() - completedSide * completedSide;
            if (relativeIndex < newSide) {
                // North row of chunks running west to east.
                processChunk(coordOffset + relativeIndex, coordOffset);
            } else if (relativeIndex < newSide + 2 * (newSide - 2)) {
                // West or east sides, running north to south.
                boolean west = ((relativeIndex - newSide) & 1) == 0;
                int row = 1 + (relativeIndex - newSide) / 2;
                if (west) {
                    // West column..
                    processChunk(coordOffset, coordOffset + row);
                } else {
                    // East column.
                    processChunk(coordOffset + newSide - 1, coordOffset + row);
                }
            } else {
                // South row of chunks running west to east.
                int column = relativeIndex - (newSide + 2 * (newSide - 2));
                processChunk(coordOffset + column, coordOffset + newSide - 1);

            }
        }
        NerdOre.CONFIG.nextIndex();
    } // processChunk

    // ------------------------------------------------------------------------
    /**
     * Process the chunk with chunk coordinates (x,z) in the configured World.
     *
     * @param chunkX the chunk X coordinate.
     * @param chunkZ the chunk Z coordinate.
     */
    protected void processChunk(int chunkX, int chunkZ) {
        long now = System.currentTimeMillis();

        World world = Bukkit.getWorld(NerdOre.CONFIG.WORLD);
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (chunk.load(false)) {
            applyClearRules(chunk);
            applyGenerateRules(chunk);
        } else {
            NerdOre.PLUGIN.getLogger().severe(String.format("Chunk %d at (%d, %d) in %s could not be loaded.",
                                                            NerdOre.CONFIG.getIndex(), chunkX, chunkZ, NerdOre.CONFIG.WORLD));
        }
        if (NerdOre.CONFIG.DEBUG_PROCESSING) {
            NerdOre.PLUGIN.getLogger().info(String.format("Generated index %d chunk (%d, %d) in %s in %d ms.",
                                                          NerdOre.CONFIG.getIndex(), chunkX, chunkZ,
                                                          NerdOre.CONFIG.WORLD, System.currentTimeMillis() - now));
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Apply all {@link ClearRule}s pertinent to the specified Chunk.
     * 
     * @param chunk the Chunk.
     */
    protected void applyClearRules(Chunk chunk) {
        World world = chunk.getWorld();
        for (ClearRule rule : NerdOre.CONFIG.getClearRules(world.getName())) {
            if (!rule.isEnabled()) {
                continue;
            }

            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    if (rule.affectsBiome(world.getBiome(x, z))) {
                        for (int y = rule.getMinHeight(); y <= rule.getMaxHeight(); y++) {
                            Block block = chunk.getBlock(x, y, z);
                            StringBuilder message = rule.isLogged() ? new StringBuilder() : null;
                            if (rule.matches(block, message) &&
                                _blockRandom.nextDouble() <= rule.getProbability()) {
                                rule.apply(block, _blockRandom, message);
                                if (rule.isLogged()) {
                                    NerdOre.PLUGIN.getLogger().info(message.toString());
                                }
                            }
                        }
                    }
                }
            }
        }
    } // applyClearRules

    // ------------------------------------------------------------------------
    /**
     * Apply all {@link OreRule}s pertinent to the specified Chunk.
     * 
     * @param chunk the Chunk.
     */
    protected void applyGenerateRules(Chunk chunk) {
        World world = chunk.getWorld();
        List<OreRule> rules = NerdOre.CONFIG.getOreRules(world.getName());
        if (rules == null) {
            return;
        }

        for (OreRule rule : rules) {
            if (!rule.isEnabled()) {
                continue;
            }

            int rounds = Util.nextInt(_random, rule.getMinRounds(), rule.getMaxRounds());
            for (int i = 0; i < rounds; i++) {
                int x = chunk.getX() * 16 + _random.nextInt(16);
                int y = Util.nextInt(_random, rule.getMinHeight(), rule.getMaxHeight());
                int z = chunk.getZ() * 16 + _random.nextInt(16);

                if (rule.affectsBiome(world.getBiome(x, z)) && _random.nextDouble() < rule.getProbability()) {
                    generate(world, x, y, z, rule);
                }
            }
        }
    } // applyGenerateRules

    // ------------------------------------------------------------------------
    /**
     * Generate one ore deposit at the specified coordinates.
     * 
     * @param world the affected world.
     * @param x the X coordinate.
     * @param y the Y coordinate.
     * @param z the Z coordinate.
     * @param rule the OreRule.
     */
    protected void generate(World world, int x, int y, int z, OreRule rule) {
        Material material = rule.getMaterial().getType();
        int size = Util.nextInt(_blockRandom, rule.getMinSize(), rule.getMaxSize());

        // Sizes less than 3 generate no blocks at all and size 3 generates less
        // than the number of rounds. So proceed as if size is at least 4, but
        // stop generating after placing size blocks.
        int effectiveSize = Math.max(4, size);

        if (rule.isLogged()) {
            Logger logger = NerdOre.PLUGIN.getLogger();
            logger.info("Generate " + size + " x " + material +
                        " at " + x + " " + y + " " + z + " in " + NerdOre.CONFIG.WORLD);
        }

        double rpi = _blockRandom.nextDouble() * Math.PI;

        double x1 = x + Math.sin(rpi) * effectiveSize / 8.0F;
        double x2 = x - Math.sin(rpi) * effectiveSize / 8.0F;
        double z1 = z + Math.cos(rpi) * effectiveSize / 8.0F;
        double z2 = z - Math.cos(rpi) * effectiveSize / 8.0F;

        double y1 = y + _blockRandom.nextInt(3);
        double y2 = y + _blockRandom.nextInt(3);

        int generated = 0;
        for (int i = 0; i <= effectiveSize; i++) {
            double xPos = x1 + (x2 - x1) * i / effectiveSize;
            double yPos = y1 + (y2 - y1) * i / effectiveSize;
            double zPos = z1 + (z2 - z1) * i / effectiveSize;

            double fuzz = _blockRandom.nextDouble() * effectiveSize / 16.0D;
            double fuzzXZ = (Math.sin((float) (i * Math.PI / effectiveSize)) + 1.0F) * fuzz + 1.0D;
            double fuzzY = (Math.sin((float) (i * Math.PI / effectiveSize)) + 1.0F) * fuzz + 1.0D;

            int xStart = (int) Math.floor(xPos - fuzzXZ / 2.0D);
            int yStart = (int) Math.floor(yPos - fuzzY / 2.0D);
            int zStart = (int) Math.floor(zPos - fuzzXZ / 2.0D);

            int xEnd = (int) Math.floor(xPos + fuzzXZ / 2.0D);
            int yEnd = (int) Math.floor(yPos + fuzzY / 2.0D);
            int zEnd = (int) Math.floor(zPos + fuzzXZ / 2.0D);

            for (int ix = xStart; ix <= xEnd; ix++) {
                double xThresh = (ix + 0.5D - xPos) / (fuzzXZ / 2.0D);
                if (xThresh * xThresh < 1.0D) {
                    for (int iy = yStart; iy <= yEnd; iy++) {
                        double yThresh = (iy + 0.5D - yPos) / (fuzzY / 2.0D);
                        if (xThresh * xThresh + yThresh * yThresh < 1.0D) {
                            for (int iz = zStart; iz <= zEnd; iz++) {
                                double zThresh = (iz + 0.5D - zPos) / (fuzzXZ / 2.0D);
                                if (xThresh * xThresh + yThresh * yThresh + zThresh * zThresh < 1.0D) {
                                    Location loc = new Location(world, ix, iy, iz);
                                    if (generateBlock(loc, material) && ++generated >= size) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } // generate

    // ------------------------------------------------------------------------
    /**
     * Generate a block of the specified material at the specified Location, if
     * the chunk can be loaded and the existing block can be replaced.
     * 
     * @param loc the Location.
     * @param material the Material.
     * @return true if a block was changed.
     */
    protected boolean generateBlock(Location loc, Material material) {
        Block block = loc.getBlock();
        if (block != null && NerdOre.CONFIG.REPLACEABLE_MATERIALS.contains(block.getType())) {
            Chunk chunk = block.getChunk();
            if (chunk.isLoaded() || chunk.load()) {
                block.setType(material);
                return true;
            } else {
                NerdOre.PLUGIN.getLogger().severe("Could not generate " + material + " at " +
                                                  loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------
    /**
     * Broadcast a progress update notification to players with the
     * nerdore.notify permission.
     */
    protected void statusUpdate() {
        if (NerdOre.CONFIG.NOTIFY) {
            int side = getSideOf(NerdOre.CONFIG.getIndex());
            int blocks = side * 16;
            String message = String.format("&a%s:%s index %d, %d x %d chunks, %d x %d blocks in %s.",
                                           NerdOre.PLUGIN.getName(), (isComplete() ? " FINISHED" : ""),
                                           NerdOre.CONFIG.getIndex(), side, side, blocks, blocks, NerdOre.CONFIG.WORLD);
            Bukkit.getServer().broadcast(ChatColor.translateAlternateColorCodes('&', message), "nerdore.notify");
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Number of ticks this task has run.
     */
    protected int _ticks;

    /**
     * True if the task is processing chunks.
     *
     * Defaults to false (not running) on plugin initialisation.
     */
    protected boolean _running;

    /**
     * The Random used to generate ores.
     */
    protected Random _random = new Random();

    /**
     * The Random used to generate or clear blocks within an ore deposit.
     * 
     * By using a separate random number generator, we avoid perturbing the
     * gross locations of ore deposits.
     */
    protected Random _blockRandom = new Random();
} // class OreTask