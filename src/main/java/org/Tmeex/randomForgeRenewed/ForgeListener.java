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

    // 固定且合法的UUID和名称（确保全局唯一）
    private UUID generateUniqueUUID() {
        return UUID.randomUUID();
    }

    public ForgeListener(RandomForgeRenewed plugin) {
        this.plugin = plugin;
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
        // 获取现有强化值
        double currentBonus = getCurrentBonus(weapon);
        double newBonus = currentBonus +
                ThreadLocalRandom.current().nextDouble(material.getMinAttack(), material.getMaxAttack());

        // 更新属性
        updateWeaponStats(weapon, newBonus);
        updateWeaponLore(weapon, newBonus);

        player.sendMessage("§a强化成功！当前总攻击加成: +" + String.format("%.1f", newBonus));
        return true;
    }

    private double getCurrentBonus(ItemStack item) {
        // 使用PersistentDataContainer存储数据
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "attack_bonus");
        return pdc.getOrDefault(key, PersistentDataType.DOUBLE, 0.0);
    }

    private void updateWeaponStats(ItemStack item, double totalBonus) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 获取并复制可修改的属性集合
        Multimap<Attribute, AttributeModifier> attributes = meta.getAttributeModifiers();
        Multimap<Attribute, AttributeModifier> mutableAttributes = attributes != null ?
                ArrayListMultimap.create(attributes) :
                ArrayListMultimap.create();

        // 保存原始攻速属性
        Collection<AttributeModifier> originalSpeed = mutableAttributes.get(Attribute.GENERIC_ATTACK_SPEED);

        // 移除旧攻击属性
        mutableAttributes.removeAll(Attribute.GENERIC_ATTACK_DAMAGE);

        // 添加新攻击属性
        UUID attackUUID = UUID.fromString("a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
        AttributeModifier attackModifier = new AttributeModifier(
                attackUUID,
                "forge_attack",
                totalBonus,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
        );
        mutableAttributes.put(Attribute.GENERIC_ATTACK_DAMAGE, attackModifier);

        // 恢复原始攻速属性
        mutableAttributes.replaceValues(Attribute.GENERIC_ATTACK_SPEED, originalSpeed);

        // 设置回ItemMeta
        meta.setAttributeModifiers(mutableAttributes);

        // 保存数据
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, "attack_bonus"),
                PersistentDataType.DOUBLE,
                totalBonus);

        item.setItemMeta(meta);
    }


    private void updateWeaponLore(ItemStack item, double bonus) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        // 移除旧Lore
        if (lore != null) {
            lore.removeIf(line -> line.contains("强化攻击力"));
        }

        // 添加新Lore
        lore.add("§6✦ 强化攻击力: +" + String.format("%.1f", bonus));
        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    private void handleFailure(ItemStack item) {
        if (plugin.getConfigManager().shouldPreventBreak()) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable damageable) {
                int newDamage = damageable.getDamage() + plugin.getConfigManager().getDurabilityLoss();
                damageable.setDamage(Math.min(newDamage, item.getType().getMaxDurability()));
                item.setItemMeta(damageable);
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