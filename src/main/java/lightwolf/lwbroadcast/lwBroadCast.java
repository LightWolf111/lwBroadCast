package lightwolf.lwbroadcast;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class lwBroadCast extends JavaPlugin implements CommandExecutor {

    private Economy econ;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // Сохраняем конфиг по умолчанию, если его еще нет
        config = getConfig(); // Загружаем конфиг
        if (!setupEconomy()) {
            getLogger().severe(config.getString("error_message"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Регистрируем команды
        getCommand("ad").setExecutor(this);
        getCommand("buy").setExecutor(this);
        getCommand("bc").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        String commandName = cmd.getName().toLowerCase();

        if (args.length < 1) {
            player.sendMessage(config.getString("usage." + commandName));
            return true;
        }

        String message = String.join(" ", args);
        int cost = config.getInt("cost." + commandName);
        String permission = config.getString("permissions." + commandName);
        String prefix = translateColorCodes(config.getString("prefix." + commandName));

        if (player.hasPermission(permission)) {
            if (econ.getBalance(player) >= cost) {
                econ.withdrawPlayer(player, cost);
                String formattedMessage = prefix + message + " §6§l∫ §7Написал: §e" + player.getName();
                getServer().broadcastMessage(formattedMessage);
            } else {
                player.sendMessage(config.getString("messages.not_enough_money"));
            }
        } else {
            player.sendMessage(config.getString("messages.no_permission"));
        }

        return true;
    }

    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
