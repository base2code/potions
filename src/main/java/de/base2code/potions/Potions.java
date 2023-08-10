package de.base2code.potions;

import de.base2code.potions.commands.PotionCommand;
import de.base2code.potions.listeners.PotionEffectListener;
import de.base2code.potions.manager.BlacklistManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Potions extends JavaPlugin {
    private static Potions instance;

    private BlacklistManager blacklistManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Potions plugin is starting up...");
        saveDefaultConfig();
        reloadConfig();

        getLogger().info("Loading blacklist...");
        try {
            blacklistManager = new BlacklistManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        getLogger().info("Registering commands...");
        this.getCommand("potion").setExecutor(new PotionCommand());

        getLogger().info("Registering listeners...");
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PotionEffectListener(), this);

        getLogger().info("Potions plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving blacklist...");
        getBlacklistManager().save();

        getLogger().info("Potions plugin has been disabled!");
    }

    public static Potions getInstance() {
        return instance;
    }

    public BlacklistManager getBlacklistManager() {
        return blacklistManager;
    }

    public String getMessage(String path) {
        String msg = getConfig().getString("messages." + path);
        if (msg == null) {
            return "§cMessage not found: " + path;
        }
        return msg.replace("&", "§");
    }
}
