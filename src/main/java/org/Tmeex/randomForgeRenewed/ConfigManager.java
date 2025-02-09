package org.Tmeex.randomForgeRenewed;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final RandomForgeRenewed plugin;
    private FileConfiguration config;

    // 配置项存储
    private boolean preventBreak;
    private int durabilityLoss;
    private List<Map<String, Object>> materials = new ArrayList<>();

    public ConfigManager(RandomForgeRenewed plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // 加载配置
        preventBreak = config.getBoolean("prevent-break", true);
        durabilityLoss = config.getInt("durability-loss-on-failure", 10);

        materials.clear();
        for (Object materialObj : config.getList("materials", new ArrayList<>())) {
            if (materialObj instanceof Map) {
                materials.add((Map<String, Object>) materialObj);
            }
        }
    }

    public boolean shouldPreventBreak() {
        return preventBreak;
    }

    public int getDurabilityLoss() {
        return durabilityLoss;
    }

    public List<Map<String, Object>> getMaterials() {
        return new ArrayList<>(materials);
    }
}
