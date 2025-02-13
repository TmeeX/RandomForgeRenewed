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
    private final RandomForgeRenewed plugin;

    public AttributeHandler(RandomForgeRenewed plugin) {
        this.plugin = plugin;
    }

    public void updateAttackAttribute(ItemStack item, double totalAttack) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 获取并复制原版属性
        Multimap<Attribute, AttributeModifier> attributes = ArrayListMultimap.create();
        if (meta.hasAttributeModifiers()) {
            attributes.putAll(meta.getAttributeModifiers());
        }

        // 移除旧攻击属性
        attributes.removeAll(Attribute.GENERIC_ATTACK_DAMAGE);

        // 添加新攻击属性（仅保留一个修饰符）
        attributes.put(Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(
                        ATTACK_UUID,
                        "forge_attack",
                        totalAttack,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                ));

        // 保存数据
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(getKey("attack_bonus"), PersistentDataType.DOUBLE, totalAttack);

        meta.setAttributeModifiers(attributes);
        item.setItemMeta(meta);
    }

    NamespacedKey getKey(String key) {
        return new NamespacedKey(plugin, key);
    }

    // 清理 by deepseek
    public void clearLegacyData(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.remove(getKey("speed_bonus")); // 清理历史数据
        item.setItemMeta(meta);
    }
}
