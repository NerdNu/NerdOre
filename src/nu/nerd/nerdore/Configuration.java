package nu.nerd.nerdore;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

// ----------------------------------------------------------------------------
/**
 * Configuration wrapper.
 */
public class Configuration {
    /**
     * If true, log configuration on load.
     */
    public boolean DEBUG_CONFIG;

    /**
     * If true, log processing steps.
     */
    public boolean DEBUG_PROCESSING;

    /**
     * If true, generated deposits are logged.
     */
    public boolean DEBUG_DEPOSITS;

    /**
     * If true, players with the nerdore.notify permission will receive
     * broadcasts about NerdOre progress.
     */
    public boolean NOTIFY;

    /**
     * The period, in ticks, between progress notifications.
     */
    public int NOTIFY_TICKS;

    /**
     * Period in ticks between chunk conversions.
     */
    public int PERIOD_TICKS;

    /**
     * The seed used to generate ores.
     */
    public long SEED;

    /**
     * Length of the side of the square to process, in chunks.
     */
    public int SIDE;

    /**
     * Name of the world to process on start.
     */
    public String WORLD;

    /**
     * Map from the world name to the non-negative index (position) of the next
     * chunk to process.
     *
     * Chunk (0,0) is index 0. Surrounding immediate neighbours are 1 through 8,
     * numbered left to right, then top top bottom. The SE diagonal (0,0),
     * (1,1), (2,2) etc is index (N^2 - 1), e.g. SIDE = 5:
     * 
     * <pre>
     *   9  10  11  12  13
     *  14   1   2   3  15
     *  16   4   0   5  17
     *  18   6   7   8  19
     *  20  21  22  23  24
     * </pre>
     */
    public Map<String, Integer> INDICES = new HashMap<>();

    /**
     * The default {@link OreRule}s if not overridden for the world.
     */
    public List<OreRule> DEFAULT_ORE_RULES;

    /**
     * The default {@link ClearRule}s if not overridden for the world.
     */
    public List<ClearRule> DEFAULT_CLEAR_RULES;

    /**
     * World specific {@link OreRule}s.
     */
    public Map<String, List<OreRule>> WORLD_ORE_RULES = new TreeMap<>();

    /**
     * World specific {@link ClearRule}s.
     */
    public Map<String, List<ClearRule>> WORLD_CLEAR_RULES = new TreeMap<>();

    /**
     * The set of Materials that an ore can be generated into by an OreRule.
     */
    public EnumSet<Material> REPLACEABLE_MATERIALS = EnumSet.noneOf(Material.class);

    // ------------------------------------------------------------------------
    /**
     * Load the plugin configuration.
     */
    public void reload() {
        // NOTE: reloadConfig() alters the object returned by getConfig().
        NerdOre.PLUGIN.reloadConfig();
        FileConfiguration config = NerdOre.PLUGIN.getConfig();
        Logger logger = NerdOre.PLUGIN.getLogger();

        DEBUG_CONFIG = config.getBoolean("debug.config");
        DEBUG_PROCESSING = config.getBoolean("debug.processing");
        DEBUG_DEPOSITS = config.getBoolean("debug.deposits");
        NOTIFY = config.getBoolean("notify");
        NOTIFY_TICKS = config.getInt("notify-ticks");
        PERIOD_TICKS = config.getInt("period-ticks");
        SEED = config.getLong("seed");
        SIDE = config.getInt("side");
        WORLD = config.getString("world");

        INDICES.clear();
        ConfigurationSection indicesSection = config.getConfigurationSection("indices");
        for (String worldName : indicesSection.getKeys(false)) {
            INDICES.put(worldName, indicesSection.getInt(worldName));
        }

        WORLD_ORE_RULES.clear();
        WORLD_CLEAR_RULES.clear();
        ConfigurationSection rules = config.getConfigurationSection("rules");
        for (String worldName : rules.getKeys(false)) {
            ConfigurationSection section = rules.getConfigurationSection(worldName);
            List<OreRule> oreRules = loadRulesFromSection(section, "generate", OreRule::new, logger);
            List<ClearRule> clearRules = loadRulesFromSection(section, "clear", ClearRule::new, logger);
            if (worldName.equals("default")) {
                DEFAULT_ORE_RULES = oreRules;
                DEFAULT_CLEAR_RULES = clearRules;
            } else {
                WORLD_ORE_RULES.put(worldName, oreRules);
                WORLD_CLEAR_RULES.put(worldName, clearRules);
            }
        }

        REPLACEABLE_MATERIALS.clear();
        for (String materialName : config.getStringList("replaceable-materials")) {
            try {
                REPLACEABLE_MATERIALS.add(Material.valueOf(materialName.toUpperCase()));
            } catch (IllegalArgumentException ex) {
                logger.severe("Invalid replaceable material: " + materialName);
            }
        }

        if (DEBUG_CONFIG) {
            logger.info("DEBUG_PROCESSING: " + DEBUG_PROCESSING);
            logger.info("DEBUG_DEPOSITS: " + DEBUG_DEPOSITS);
            logger.info("NOTIFY: " + NOTIFY);
            logger.info("PERIOD_TICKS: " + PERIOD_TICKS);
            logger.info("SEED: " + SEED);
            logger.info("SIDE: " + SIDE);
            logger.info("WORLD: " + WORLD);
            logger.info("Indices:");
            for (Entry<String, Integer> entry : INDICES.entrySet()) {
                logger.info(entry.getKey() + ": " + entry.getValue());
            }

            logger.info("Replaceable materials: " +
                        REPLACEABLE_MATERIALS.stream().map(Material::toString)
                        .collect(Collectors.joining(", ")));

            logClearRules(logger, "default");
            for (String worldName : WORLD_CLEAR_RULES.keySet()) {
                logClearRules(logger, worldName);
            }
            logOreRules(logger, "default");
            for (String worldName : WORLD_ORE_RULES.keySet()) {
                logOreRules(logger, worldName);
            }
        }
    } // reload

    // ------------------------------------------------------------------------
    /**
     * Save the configuration to disk.
     */
    public void save() {
        FileConfiguration config = NerdOre.PLUGIN.getConfig();
        config.set("debug.config", DEBUG_CONFIG);
        config.set("debug.processing", DEBUG_PROCESSING);
        config.set("debug.deposits", DEBUG_DEPOSITS);
        config.set("notify", NOTIFY);
        config.set("period-ticks", PERIOD_TICKS);
        config.set("seed", SEED);
        config.set("side", SIDE);
        config.set("world", WORLD);

        ConfigurationSection indicesSection = config.createSection("indices");
        for (Entry<String, Integer> entry : INDICES.entrySet()) {
            indicesSection.set(entry.getKey(), entry.getValue());
        }
        NerdOre.PLUGIN.saveConfig();
    }

    // ------------------------------------------------------------------------
    /**
     * Set the current chunk index in the specified world.
     * 
     * @param world the World.
     * @param index the index value.
     */
    public void setIndex(World world, int index) {
        INDICES.put(world.getName(), index);
    }

    // ------------------------------------------------------------------------
    /**
     * Return the current chunk index in the specified world.
     * 
     * @param world the World.
     * @return the current chunk index in the specified world.
     */
    public int getIndex(World world) {
        return INDICES.getOrDefault(world.getName(), 0);
    }

    // ------------------------------------------------------------------------
    /**
     * Increment the chunk index in the currently processed world.
     */
    public void nextIndex() {
        INDICES.put(WORLD, getIndex() + 1);

    }

    // ------------------------------------------------------------------------
    /**
     * Return the current chunk index in the currently processed world.
     * 
     * @return the current chunk index in the currently processed world.
     */
    public int getIndex() {
        return INDICES.getOrDefault(WORLD, 0);
    }

    // ------------------------------------------------------------------------
    /**
     * Return the {@link OreRule}s that apply to the specified world.
     * 
     * @param worldName the name of the world.
     * @return the {@link OreRule}s that apply to the specified world.
     */
    public List<OreRule> getOreRules(String worldName) {
        return WORLD_ORE_RULES.getOrDefault(worldName, DEFAULT_ORE_RULES);
    }

    // ------------------------------------------------------------------------
    /**
     * Return the {@link ClearRule}s that apply to the specified world.
     * 
     * @param worldName the name of the world.
     * @return the {@link ClearRule}s that apply to the specified world.
     */
    public List<ClearRule> getClearRules(String worldName) {
        return WORLD_CLEAR_RULES.getOrDefault(worldName, DEFAULT_CLEAR_RULES);
    }

    // ------------------------------------------------------------------------
    /**
     * Log the specified set of clear rules to the console.
     * 
     * @param logger the Logger.
     * @param id the name of the set of rules (either "default" or a world name.
     */
    protected void logClearRules(Logger logger, String id) {
        logger.info(id + " clear rules:");
        for (ClearRule rule : getClearRules(id)) {
            logger.info(rule.toString());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Log the specified set of ore rules to the console.
     * 
     * @param logger the Logger.
     * @param id the name of the set of rules (either "default" or a world name.
     */
    protected void logOreRules(Logger logger, String id) {
        logger.info(id + " generate rules:");
        for (OreRule rule : getOreRules(id)) {
            logger.info(rule.toString());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Load a list of {@link OreRule}s or {@link ClearRule}s from a
     * configuration section defining the settings for one world (or all-world
     * defaults).
     * 
     * @param worldSection the section containing a list named ruleListName.
     * @param ruleListName the name of a key in worldSection: either "generate"
     *        to load OreRules, or "clear" to load ClearRules.
     * @param ctor a lambda that calls the constructor of {@link OreRule} or
     *        {@link ClearRule}, as appropriate to ruleListName.
     * @param logger used for logging.
     */
    protected static <R extends Rule> List<R> loadRulesFromSection(ConfigurationSection worldSection,
                                                                   String ruleListName,
                                                                   Function<ConfigurationSection, R> ctor,
                                                                   Logger logger) {
        List<R> result = new ArrayList<R>();
        if (worldSection == null) {
            return result;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> maps = (List<Map<String, Object>>) (List<?>) worldSection.getMapList(ruleListName);
        if (maps != null) {
            for (Map<String, Object> ruleMap : maps) {
                MemoryConfiguration config = new MemoryConfiguration();
                populateConfigurationSection(config, ruleMap);
                R rule = ctor.apply(config);
                if (rule.isValid()) {
                    result.add(rule);
                }
            }
        }

        return result;
    }

    // ------------------------------------------------------------------------
    /**
     * Add a Map<String,Object> of values to a ConfigurationSection, creating
     * sub-sections recursively as needed.
     * 
     * Using ConfigurationSection.addDefaults(Map<String,Object>) does not work
     * when there are subordinate ConfigurationSections, so here we are.
     */
    @SuppressWarnings("unchecked")
    protected static void populateConfigurationSection(ConfigurationSection section, Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>) {
                ConfigurationSection subSection = section.createSection(entry.getKey());
                populateConfigurationSection(subSection, (Map<String, Object>) entry.getValue());
            } else {
                section.set(entry.getKey(), entry.getValue());
            }
        }
    }

} // class Configuration