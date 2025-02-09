package org.Tmeex.randomForgeRenewed;

import org.bukkit.command.defaults.ReloadCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class RandomForgeRenewed extends JavaPlugin {

    public ConfigManager configManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager = new ConfigManager(this);
        getServer().getPluginManager().registerEvents(new ForgeListener(this), this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
