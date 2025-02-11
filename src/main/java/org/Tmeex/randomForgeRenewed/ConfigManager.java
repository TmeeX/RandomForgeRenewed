package org.Tmeex.randomForgeRenewed;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.*;

public class ConfigManager {
    private final RandomForgeRenewed plugin;
    private final List<ForgingMaterial> materials = new ArrayList<>();
    private boolean preventBreak;
    private int durabilityLoss;

    public ConfigManager(RandomForgeRenewed plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        materials.clear();
        preventBreak = config.getBoolean("prevent-break", true);
        durabilityLoss = config.getInt("durability-loss-on-failure", 10);

        ConfigurationSection materialsSection = config.getConfigurationSection("materials");
        if (materialsSection != null) {
            for (String key : materialsSection.getKeys(false)) {
                ConfigurationSection matSection = materialsSection.getConfigurationSection(key);
                if (matSection != null) {
                    materials.add(new ForgingMaterial(
                            matSection.getString("type"),
                            matSection.getInt("amount"),
                            matSection.getDouble("chance", 0.7),
                            matSection.getDouble("attack.min"),
                            matSection.getDouble("attack.max"),
                            matSection.getDouble("speed.min", 1.0), // 默认值
                            matSection.getDouble("speed.max", 1.0)
                    ));
                }
            }
        }
    }

    public List<ForgingMaterial> getMaterials() {
        return Collections.unmodifiableList(materials);
    }

    public boolean shouldPreventBreak() {
        return preventBreak;
    }

    public int getDurabilityLoss() {
        return durabilityLoss;
    }

    public static class ForgingMaterial {
        private final String type;
        private final int amount;
        private final double chance;
        private final double minAttack;
        private final double maxAttack;
        private final double minSpeed;
        private final double maxSpeed;

        public ForgingMaterial(String type, int amount, double chance, double minAttack, double maxAttack, double minSpeed, double maxSpeed) {
            this.type = type;
            this.amount = amount;
            this.chance = chance;
            this.minAttack = minAttack;
            this.maxAttack = maxAttack;
            this.minSpeed = minSpeed;
            this.maxSpeed = maxSpeed;
        }

        public double getMaxAttack() {
            return maxAttack;
        }

        public double getMinAttack() {
            return minAttack;
        }

        public String getType() {
            return type;
        }

        public int getAmount() {
            return amount;
        }

        public double getChance() {
            return chance;
        }

        public double getMinSpeed() {
            return minSpeed;
        }

        public double getMaxSpeed() {
            return maxSpeed;
        }

        // Getters...
    }
}