package org.Tmeex.randomForgeRenewed;

import com.google.common.collect.ArrayListMultimap;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
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

        // 获取当前加成
        double currentAttack = getCurrentBonus(weapon, "attack_bonus");
        double currentSpeed = getCurrentBonus(weapon, "speed_bonus");

        // 生成新加成
        double newAttack = currentAttack + random.nextDouble(material.getMinAttack(), material.getMaxAttack());
        double newSpeed = currentSpeed + random.nextDouble(material.getMinSpeed(), material.getMaxSpeed());

        // 更新属性和Lore
        attributeHandler.updateAttributes(weapon, newAttack, newSpeed);
        updateWeaponLore(weapon, newAttack, newSpeed);

        player.sendMessage("§a强化成功！");
        return true;
    }


    private double getCurrentBonus(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        return meta.getPersistentDataContainer()
                .getOrDefault(attributeHandler.getKey(key), PersistentDataType.DOUBLE, 0.0);
    }

    private void updateWeaponLore(ItemStack item, double attack, double speed) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        // 保留原始Lore（过滤系统生成的属性行）
        if (meta.hasLore()) {
            lore.addAll(meta.getLore().stream()
                    .filter(line -> !line.contains("✦"))
                    .toList());
        }

        // 添加新Lore
        lore.add("§6✦ 攻击加成: +" + String.format("%.1f", attack));
        lore.add("§6✦ 攻速加成: +" + String.format("%.1f", speed));

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