package org.Tmeex.randomForgeRenewed;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ForgeListener implements Listener {
    private final RandomForgeRenewed plugin;

    public ForgeListener(RandomForgeRenewed plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAnvilUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null ||
                !event.getClickedBlock().getType().name().contains("ANVIL")) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isWeapon(item)) return;

        Optional<ConfigManager.ForgingMaterial> material = plugin.getConfigManager().getMaterials().stream()
                .filter(m -> hasEnoughMaterials(player, m))
                .findFirst();

        if (!material.isPresent()) return;

        if (tryForge(player, item, material.get())) {
            consumeMaterials(player, material.get());
        }
    }

    private boolean tryForge(Player player, ItemStack weapon, ConfigManager.ForgingMaterial material) {
        Random random = ThreadLocalRandom.current();
        if (random.nextDouble() > material.getChance()) {
            handleFailure(weapon);
            player.sendMessage("§c强化失败！");
            return false;
        }

        addAttackDamage(weapon, material);
        player.sendMessage("§a强化成功！攻击力增加了！");
        return true;
    }

    private void addAttackDamage(ItemStack item, ConfigManager.ForgingMaterial material) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 获取现有属性（可能为null）
        Multimap<Attribute, AttributeModifier> attributes = meta.getAttributeModifiers();

        // 创建新的属性映射（使用Guava的ArrayListMultimap）
        Multimap<Attribute, AttributeModifier> newAttributes = attributes != null ?
                ArrayListMultimap.create(attributes) :
                ArrayListMultimap.create();

        // 创建新修饰符
        AttributeModifier modifier = new AttributeModifier(
                UUID.randomUUID(),
                "forge_attack",
                ThreadLocalRandom.current().nextDouble(material.getMinAttack(), material.getMaxAttack()),
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
        );

        // 添加新属性
        newAttributes.put(Attribute.GENERIC_ATTACK_DAMAGE, modifier);

        // 设置回ItemMeta
        meta.setAttributeModifiers(newAttributes);
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
    // 这个类用于失败扣除物品耐久

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