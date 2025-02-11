package org.Tmeex.randomForgeRenewed;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;

public class AttributeHandler {
    private static final UUID ATTACK_UUID = UUID.fromString("8fc2ad20-5a40-403a-8bf2-fe22af1c9ddb");
    private static final UUID SPEED_UUID = UUID.fromString("03fdaeab-b194-4960-b7e2-7869ae34fae0");
    private final RandomForgeRenewed plugin;


    public AttributeHandler(RandomForgeRenewed plugin) {
        this.plugin = plugin;
    }

    public void updateAttributes(ItemStack item, double attackBonus, double speedBonus) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 保存原始基础值
        double baseAttack = getBaseValue(item, Attribute.GENERIC_ATTACK_DAMAGE);
        double baseSpeed = getBaseValue(item, Attribute.GENERIC_ATTACK_SPEED);

        // 创建新属性映射
        Multimap<Attribute, AttributeModifier> attributes = ArrayListMultimap.create();

        // 添加基础攻击属性
        attributes.put(Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(
                        ATTACK_UUID,
                        "base_attack",
                        baseAttack,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                ));

        // 添加强化攻击属性
        attributes.put(Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "forge_attack",
                        attackBonus,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                ));

        // 添加攻速属性
        attributes.put(Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(
                        SPEED_UUID,
                        "forge_speed",
                        speedBonus,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                ));

        // 保存数据
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(getKey("attack_bonus"), PersistentDataType.DOUBLE, attackBonus);
        pdc.set(getKey("speed_bonus"), PersistentDataType.DOUBLE, speedBonus);

        meta.setAttributeModifiers(attributes);
        item.setItemMeta(meta);
    }

    private double getBaseValue(ItemStack item, Attribute attribute) {
        // 获取原版基础值（通过临时移除所有修饰符）
        ItemMeta meta = item.getItemMeta();
        Multimap<Attribute, AttributeModifier> original = meta.getAttributeModifiers();
        meta.setAttributeModifiers(null);
        item.setItemMeta(meta);

        double base = item.getType().getDefaultAttributeModifiers(EquipmentSlot.HAND)
                .get(attribute).stream()
                .mapToDouble(AttributeModifier::getAmount)
                .sum();

        meta.setAttributeModifiers(original);
        item.setItemMeta(meta);
        return base;
    }

    public NamespacedKey getKey(String key) {
        return new NamespacedKey(plugin, key);
    }
}
