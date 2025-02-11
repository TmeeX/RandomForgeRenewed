package org.Tmeex.randomForgeRenewed;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class RandomForgeRenewed extends JavaPlugin {

    public ConfigManager configManager;

    @Override
    public void onEnable() {
        try {
            // 确保配置文件加载
            saveDefaultConfig();

            // 初始化配置管理器
            this.configManager = new ConfigManager(this);

            // 注册事件监听器
            getServer().getPluginManager().registerEvents(new ForgeListener(this), this);

            // 命令注册（带详细错误处理）
            PluginCommand command = getCommand("rfrreload");
            if (command == null) {
                getLogger().severe("严重错误：命令 rfrreload 未注册！");
                getLogger().severe("解决方案：请检查 plugin.yml 是否存在且格式正确");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            command.setExecutor(new ReloadCommand(this));

            getLogger().info("插件加载成功！");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "插件启动失败", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
