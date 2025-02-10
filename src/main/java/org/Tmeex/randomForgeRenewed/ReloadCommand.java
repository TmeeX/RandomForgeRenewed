package org.Tmeex.randomForgeRenewed;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {
    private final RandomForgeRenewed plugin;

    public ReloadCommand(RandomForgeRenewed plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c该命令只能由玩家执行！");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("randomforgerenewed.reload")) {
            player.sendMessage("§c你没有权限！");
            return true;
        }

        plugin.getConfigManager().reload();
        player.sendMessage("§a配置已重载！");
        return true;
    }
}
