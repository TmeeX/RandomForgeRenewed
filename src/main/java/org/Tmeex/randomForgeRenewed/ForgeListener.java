package org.Tmeex.randomForgeRenewed;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ForgeListener implements Listener {
    private final RandomForgeRenewed plugin;

    public ForgeListener(RandomForgeRenewed plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAnvilUse(PlayerInteractEvent event) {
        // 检查是否右键点击铁砧
        if (event.getClickedBlock() == null ||
                !event.getClickedBlock().getType().name().contains("ANVIL")) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        // 检查是否武器
        if (!isWeapon(item)) return;

        // 检查材料匹配
        if (!checkMaterials(event.getPlayer(), item)) return;

        // 执行强化逻辑
        processForging(event.getPlayer(), item);
    }

    private boolean isWeapon(ItemStack item) {
        // 这里需要根据武器判断逻辑实现
        return item.getType().name().endsWith("_SWORD") ||
                item.getType().name().endsWith("_AXE");
    }

    private boolean checkMaterials(Player player, ItemStack weapon) {
        // 实现材料检查逻辑
        return true;
    }

    private void processForging(Player player, ItemStack weapon) {
        ConfigManager config = plugin.getConfigManager();
        Random random = new Random();

        // 检查强化成功率
        if (random.nextDouble() < getSuccessChance()) {
            // 强化成功，添加属性
            addAttributes(weapon);
            player.sendMessage("§a强化成功！");
        } else {
            // 强化失败处理
            handleFailure(weapon);
            player.sendMessage("§c强化失败...");
        }
    }

    private void addAttributes(ItemStack item) {
        // 使用AttributePlus API添加属性
        ItemMeta meta = item.getItemMeta();
        // 示例：添加攻击伤害属性
        AttributePlus.getInstance().getAttributeManager()
                .addAttribute(meta, "GENERIC_ATTACK_DAMAGE",
                        generateRandomValue("attack"),
                        AttributeModifier.Operation.ADD_NUMBER);
        item.setItemMeta(meta);
    }

    private double generateRandomValue(String attributeType) {
        // 根据配置生成随机数值
        return ThreadLocalRandom.current().nextDouble(3, 5);
    }

    private void handleFailure(ItemStack item) {
        if (plugin.getConfigManager().shouldPreventBreak()) {
            // 扣除耐久
            Damageable damageable = (Damageable) item.getItemMeta();
            damageable.setDamage(damageable.getDamage() +
                    plugin.getConfigManager().getDurabilityLoss());
            item.setItemMeta(damageable);
        } else {
            // 直接销毁
            item.setAmount(0);
        }
    }
}
