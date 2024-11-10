package me.customWelcomePlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomWelcomePlugin extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private FileConfiguration messages;
    private Map<UUID, Long> lastJoinTimes = new HashMap<>();

    @Override
    public void onEnable() {
        // Save the default configuration files (config.yml and messages.yml)
        saveDefaultConfig();
        createMessagesConfig();

        // Load the configurations
        this.config = getConfig();
        this.messages = getMessagesConfig();

        // Register commands
        this.getCommand("setlobby").setExecutor(new SetLobbyCommand());
        this.getCommand("lobby").setExecutor(new LobbyCommand());
        this.getCommand("launchFirework").setExecutor(new LaunchFireworkCommand());
        this.getCommand("reload").setExecutor(new ReloadCommand());

        // Register the event listener
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("CustomWelcomePlugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomWelcomePlugin disabled.");
    }

    // Ensure the messages.yml file exists
    private void createMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false); // Create the messages file from the plugin resources
        }
    }

    // Load the messages configuration file
    private FileConfiguration getMessagesConfig() {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
    }

    // Player join event handling with PlaceholderAPI support
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Use PlaceholderAPI to replace placeholders in messages
        String welcomeMessage = PlaceholderAPI.setPlaceholders(player, messages.getString("welcome-message"));
        String returningMessage = PlaceholderAPI.setPlaceholders(player, messages.getString("returning-message"));

        if (!lastJoinTimes.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', welcomeMessage));
            player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Firework.class); // Firework effect
            lastJoinTimes.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', returningMessage));
        }
    }

    // Command to set the lobby location
    private class SetLobbyCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("customwelcomeplugin.setlobby")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Location loc = player.getLocation();
                    config.set("lobby.world", loc.getWorld().getName());
                    config.set("lobby.x", loc.getX());
                    config.set("lobby.y", loc.getY());
                    config.set("lobby.z", loc.getZ());
                    config.set("lobby.yaw", loc.getYaw());
                    config.set("lobby.pitch", loc.getPitch());
                    saveConfig();
                    player.sendMessage(ChatColor.GREEN + PlaceholderAPI.setPlaceholders(player, messages.getString("setlobby-success")));
                }
            } else {
                sender.sendMessage(ChatColor.RED + PlaceholderAPI.setPlaceholders((Player) sender, messages.getString("setlobby-no-permission")));
            }
            return true;
        }
    }

    // Command to teleport the player to the lobby
    private class LobbyCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (config.contains("lobby.world")) {
                    String world = config.getString("lobby.world");
                    double x = config.getDouble("lobby.x");
                    double y = config.getDouble("lobby.y");
                    double z = config.getDouble("lobby.z");
                    float yaw = (float) config.getDouble("lobby.yaw");
                    float pitch = (float) config.getDouble("lobby.pitch");

                    Location lobbyLocation = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                    player.teleport(lobbyLocation);
                    player.sendMessage(ChatColor.GREEN + PlaceholderAPI.setPlaceholders(player, messages.getString("teleport-lobby-success")));
                } else {
                    player.sendMessage(ChatColor.RED + PlaceholderAPI.setPlaceholders(player, messages.getString("teleport-lobby-no-location")));
                }
            }
            return true;
        }
    }

    // Command to launch a firework
    private class LaunchFireworkCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("customwelcomeplugin.launchfirework")) {
                    player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Firework.class);
                    player.sendMessage(ChatColor.GREEN + PlaceholderAPI.setPlaceholders(player, messages.getString("launchfirework-success")));
                } else {
                    player.sendMessage(ChatColor.RED + PlaceholderAPI.setPlaceholders(player, messages.getString("launchfirework-no-permission")));
                }
            }
            return true;
        }
    }

    // Command to reload the configuration
    private class ReloadCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("customwelcomeplugin.reload")) {
                reloadConfig();
                messages = getMessagesConfig();
                sender.sendMessage(ChatColor.GREEN + PlaceholderAPI.setPlaceholders((Player) sender, messages.getString("reload-success")));
            } else {
                sender.sendMessage(ChatColor.RED + PlaceholderAPI.setPlaceholders((Player) sender, messages.getString("reload-fail")));
            }
            return true;
        }
    }
}
