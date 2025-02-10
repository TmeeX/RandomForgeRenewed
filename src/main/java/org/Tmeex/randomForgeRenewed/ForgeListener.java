package org.Tmeex.randomForgeRenewed;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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
        double attackValue = ThreadLocalRandom.current()
                .nextDouble(material.getMinAttack(), material.getMaxAttack());
        // 待改,attackAttribute已经弃用
        AttributeInstance attackAttribute = (AttributeInstance) meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE);

        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(
                        UUID.randomUUID(),
                        "forge_attack",
                        attackValue,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                ));
        item.setItemMeta(meta);
    }

    private void handleFailure(ItemStack item) {
        if (plugin.getConfigManager().shouldPreventBreak()) {
            int damage = item.getDurability() + plugin.getConfigManager().getDurabilityLoss();
            item.setDurability((short) Math.min(damage, item.getType().getMaxDurability()));
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