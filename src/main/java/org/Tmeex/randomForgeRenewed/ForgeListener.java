package org.Tmeex.randomForgeRenewed;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ForgeListener implements Listener {
    private final RandomForgeRenewed plugin;
    private final AttributeHandler attributeHandler;

    // 固定且合法的UUID和名称（确保全局唯一）
    private UUID generateUniqueUUID() {
        return UUID.randomUUID();
    }

    public ForgeListener(RandomForgeRenewed plugin) {
        this.plugin = plugin;
        this.attributeHandler = new AttributeHandler(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 检测Shift+右键空气
        if (event.getAction() != Action.RIGHT_CLICK_AIR ||
                !event.getPlayer().isSneaking()) return;

        Player player = event.getPlayer();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!isWeapon(weapon)) return;

        // 阻止原版交互
        event.setCancelled(true);

        // 强化逻辑
        Optional<ConfigManager.ForgingMaterial> material = plugin.getConfigManager().getMaterials().stream()
                .filter(m -> hasEnoughMaterials(player, m))
                .findFirst();

        if (material.isEmpty()) return;

        if (tryForge(player, weapon, material.get())) {
            consumeMaterials(player, material.get());
        }
    }

    private boolean tryForge(Player player, ItemStack weapon, ConfigManager.ForgingMaterial material) {
        Random random = ThreadLocalRandom.current();

        // 成功率检查
        if (random.nextDouble() > material.getChance()) {
            handleFailure(weapon);
            player.sendMessage("§c强化失败！");
            return false;
        }

        // 获取当前攻击加成
        double currentAttack = getCurrentBonus(weapon, "attack_bonus");
        double newAttack = currentAttack + random.nextDouble(material.getMinAttack(), material.getMaxAttack());

        // 更新属性和Lore
        attributeHandler.updateAttackAttribute(weapon, newAttack);
        updateAttackLore(weapon, newAttack);

        player.sendMessage("§a强化成功！当前攻击加成: +" + String.format("%.1f", newAttack));
        return true;
    }



    private double getCurrentBonus(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        return meta.getPersistentDataContainer()
                .getOrDefault(attributeHandler.getKey(key), PersistentDataType.DOUBLE, 0.0);
    }

    private void updateAttackLore(ItemStack item, double attack) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        // 保留非强化相关Lore
        if (meta.hasLore()) {
            lore.addAll(meta.getLore().stream()
                    .filter(line -> !line.startsWith("§6✦ 攻击加成"))
                    .toList());
        }

        // 更新攻击Lore
        lore.add("§6✦ 攻击加成: +" + String.format("%.1f", attack));
        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    private void handleFailure(ItemStack item) {
        if (plugin.getConfigManager().shouldPreventBreak()) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable damageable) {
                int loss = plugin.getConfigManager().getDurabilityLoss();
                int newDamage = Math.min(
                        damageable.getDamage() + loss,
                        item.getType().getMaxDurability()
                );
                damageable.setDamage(newDamage);
                item.setItemMeta(meta);
            }
        } else {
            item.setAmount(0);
        }
    }

    private boolean isWeapon(ItemStack item) {
        if (item == null) return false;
        String type = item.getType().name();
        return type.endsWith("_SWORD") || type.endsWith("_AXE");
    }

    private boolean hasEnoughMaterials(Player player, ConfigManager.ForgingMaterial material) {
        return player.getInventory().contains(Material.matchMaterial(material.getType()), material.getAmount());
    }

    private void consumeMaterials(Player player, ConfigManager.ForgingMaterial material) {
        player.getInventory().removeItem(new ItemStack(
                Material.matchMaterial(material.getType()),
                material.getAmount()
        ));
    }
}