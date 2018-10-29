package nu.nerd.nerdore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

// ----------------------------------------------------------------------------
/**
 * Main plugin class.
 */
public class NerdOre extends JavaPlugin {
    /**
     * This plugin as singleton.
     */
    public static NerdOre PLUGIN;

    /**
     * Configuration instance.
     */
    public static Configuration CONFIG = new Configuration();

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
        PLUGIN = this;

        saveDefaultConfig();
        CONFIG.reload();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, _task, 1, 1);
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        CONFIG.save();
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(getName())) {
            if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                return false;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                CONFIG.reload();
                sender.sendMessage(ChatColor.GREEN + getName() + " configuration reloaded.");
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("notify")) {
                CONFIG.NOTIFY = !CONFIG.NOTIFY;
                sender.sendMessage(ChatColor.GREEN + getName() + " notifications " + (CONFIG.NOTIFY ? "ENABLED." : "DISABLED."));
                CONFIG.save();
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("debug")) {
                CONFIG.DEBUG_PROCESSING = !CONFIG.DEBUG_PROCESSING;
                sender.sendMessage(ChatColor.GREEN + getName() + " debug logging " + (CONFIG.DEBUG_PROCESSING ? "ENABLED." : "DISABLED."));
                CONFIG.save();
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
                cmdStart(sender);
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
                cmdStop(sender);
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
                String message = String.format("%s: %s, completed side dimension %d of %d, index %d in %s.",
                                               getName(), (_task.isRunning() ? "RUNNING" : "STOPPED"),
                                               Math.max(0, OreTask.getSideOf(CONFIG.getIndex())),
                                               CONFIG.SIDE, CONFIG.getIndex(), NerdOre.CONFIG.WORLD);
                sender.sendMessage(ChatColor.GREEN + message);
                return true;
            }
            if (args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("period")) {
                cmdPeriod(sender, args);
                return true;
            }
            if (args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("seed")) {
                cmdSeed(sender, args);
                return true;
            }
            if (args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("side")) {
                cmdSide(sender, args);
                return true;
            }
            if (args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("index")) {
                cmdIndex(sender, args);
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("rules")) {
                cmdRules(sender);
                return true;
            }
        }

        sender.sendMessage(ChatColor.RED + "Invalid command syntax. Try \"/" + getName() + " help\" for help.");
        return false;
    } // onCommand

    // ------------------------------------------------------------------------
    /**
     * Handler /nerdore start.
     *
     * @param sender the CommandSender.
     */
    protected void cmdStart(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be in-game to use this command.");
            return;
        }
        World world = ((Player) sender).getLocation().getWorld();

        if (_task.isRunning()) {
            sender.sendMessage(ChatColor.GREEN + getName() + " is already running in " +
                               NerdOre.CONFIG.WORLD + ".");
            if (!NerdOre.CONFIG.WORLD.equals(world.getName())) {
                sender.sendMessage(ChatColor.GREEN + "To start processing in this world, run '/nerdore stop' first.");
            }
        } else {
            NerdOre.CONFIG.WORLD = world.getName();
            _task.setSeed(CONFIG.SEED);
            _task.setRunning(true);
            sender.sendMessage(ChatColor.GREEN + getName() + " STARTED at index " +
                               CONFIG.getIndex() + " in " + NerdOre.CONFIG.WORLD + ".");
            CONFIG.save();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Handle /nerdore stop.
     *
     * @param sender the CommandSender.
     */
    protected void cmdStop(CommandSender sender) {
        if (!_task.isRunning()) {
            sender.sendMessage(ChatColor.GREEN + getName() + " is already stopped.");
        } else {
            _task.setRunning(false);
            sender.sendMessage(ChatColor.GREEN + getName() + " STOPPED at index " +
                               CONFIG.getIndex() + ".");
            CONFIG.save();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Handle /nerdore period [<ticks>].
     *
     * @param sender the CommandSender.
     * @param args command arguments.
     */
    protected void cmdPeriod(CommandSender sender, String[] args) {
        boolean changed = false;
        if (args.length == 2) {
            try {
                int newPeriod = Integer.parseInt(args[1]);
                if (newPeriod > 0) {
                    CONFIG.PERIOD_TICKS = newPeriod;
                    changed = true;
                }
            } catch (NumberFormatException ex) {
            }
            if (!changed) {
                sender.sendMessage(ChatColor.RED + "The argument must be a positive integer.");
            }
        }
        sender.sendMessage(ChatColor.GREEN + getName() + ": the period is " +
                           (changed ? "now " : "") + CONFIG.PERIOD_TICKS + " tick(s).");
        if (changed) {
            CONFIG.save();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Handle /nerdore seed [<blocks>].
     *
     * @param sender the CommandSender.
     * @param args command arguments.
     */
    protected void cmdSeed(CommandSender sender, String[] args) {
        boolean changed = false;
        if (args.length == 2) {
            try {
                CONFIG.SEED = Long.parseLong(args[1]);
                changed = true;
            } catch (NumberFormatException ex) {
            }
            if (!changed) {
                sender.sendMessage(ChatColor.RED + "The argument must be an integer.");
            }
        }
        sender.sendMessage(ChatColor.GREEN +
                           String.format(getName() + ": the ore generation seed is %s%d.",
                                         (changed ? "now " : ""), CONFIG.SEED));
        if (changed) {
            _task.setSeed(CONFIG.SEED);
            CONFIG.save();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Handle /nerdore side [<blocks>].
     *
     * @param sender the CommandSender.
     * @param args command arguments.
     */
    protected void cmdSide(CommandSender sender, String[] args) {
        boolean changed = false;
        if (args.length == 2) {
            try {
                int newSideBlocks = Integer.parseInt(args[1]);
                if (newSideBlocks > 0) {
                    // Convert blocks to chunks rounded up and forced odd.
                    int newSideChunks = (newSideBlocks + 15) / 16;
                    if ((newSideChunks & 1) == 0) {
                        ++newSideChunks;
                    }

                    CONFIG.SIDE = newSideChunks;
                    changed = true;
                }
            } catch (NumberFormatException ex) {
            }
            if (!changed) {
                sender.sendMessage(ChatColor.RED + "The argument must be a positive integer.");
            }
        }
        sender.sendMessage(ChatColor.GREEN +
                           String.format(getName() + ": the side is %s%d chunk(s), %d blocks.",
                                         (changed ? "now " : ""), CONFIG.SIDE, CONFIG.SIDE * 16));
        if (changed) {
            CONFIG.save();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Handle /nerdore index [<blocks>].
     *
     * @param sender the CommandSender.
     * @param args command arguments.
     */
    protected void cmdIndex(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You need to be in-game to use this command.");
            return;
        }

        World world = ((Player) sender).getLocation().getWorld();
        int oldIndex = CONFIG.getIndex(world);

        boolean changed = false;
        if (args.length == 2) {
            try {
                int newIndex = Integer.parseInt(args[1]);
                if (newIndex >= 0) {
                    CONFIG.setIndex(world, newIndex);
                    changed = true;
                }
            } catch (NumberFormatException ex) {
            }
            if (!changed) {
                sender.sendMessage(ChatColor.RED + "The argument must be a non-negative integer.");
            }
        }

        sender.sendMessage(ChatColor.GREEN + getName() + ": the index in " +
                           world.getName() + " is " +
                           (changed ? "now " : "") + CONFIG.getIndex(world) + ".");
        if (changed) {
            sender.sendMessage(ChatColor.GREEN + getName() + ": it used to be " + oldIndex + ".");
            CONFIG.save();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Handle /nerdore rules.
     *
     * @param sender the CommandSender.
     */
    protected void cmdRules(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You need to be in-game to use this command.");
            return;
        }
        String worldName = ((Player) sender).getLocation().getWorld().getName();
        sender.sendMessage(ChatColor.DARK_GREEN + "Clear rules in " + worldName + ":");
        for (ClearRule rule : NerdOre.CONFIG.getClearRules(worldName)) {
            sender.sendMessage(ChatColor.GREEN + rule.toString());
        }
        sender.sendMessage(ChatColor.DARK_GREEN + "Generation rules in " + worldName + ":");
        for (OreRule rule : NerdOre.CONFIG.getOreRules(worldName)) {
            sender.sendMessage(ChatColor.GREEN + rule.toString());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Task to process ores. Runs every tick, irrespective of configured PERIOD_TICKS.
     */
    protected OreTask _task = new OreTask();

} // class NerdOre