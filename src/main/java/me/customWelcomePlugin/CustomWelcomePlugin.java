package me.customWelcomePlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomWelcomePlugin extends JavaPlugin {

    private FileConfiguration config;
    private FileConfiguration messages;
    private Map<UUID, Long> lastJoinTimes = new HashMap<>();
    private ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;

    @Override
    public void onEnable() {
        // Save the default config file if it doesn't exist already
        saveDefaultConfig(); // This will create the config.yml in the plugin's data folder

        createMessagesConfig();

        // Load the configurations
        this.config = getConfig();
        this.messages = getMessagesConfig();

        // Register commands
        this.getCommand("setlobby").setExecutor(new SetLobbyCommand());
        this.getCommand("lobby").setExecutor(new LobbyCommand());
        this.getCommand("launchFirework").setExecutor(new LaunchFireworkCommand());
        this.getCommand("reload").setExecutor(new ReloadCommand());

        // Register the event listener for player join
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);

        // Initialize scoreboard
        this.scoreboardManager = Bukkit.getScoreboardManager();
        this.scoreboard = scoreboardManager.getNewScoreboard();

        getLogger().info("CustomWelcomePlugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomWelcomePlugin disabled.");
    }

    private void createMessagesConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);  // This will copy the messages.yml file from the plugin resources
        }
    }

    private FileConfiguration getMessagesConfig() {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
    }

    // Listener for player join
    private class PlayerJoinListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
            Player player = event.getPlayer();
            if (!lastJoinTimes.containsKey(player.getUniqueId())) {
                String message = messages.getString("welcome-message").replace("%player_name%", player.getName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

                // Firework effect
                player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Firework.class);

                lastJoinTimes.put(player.getUniqueId(), System.currentTimeMillis());
            } else {
                String message = messages.getString("returning-message").replace("%player_name%", player.getName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }

    // Command for setting the lobby
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
                    player.sendMessage(ChatColor.GREEN + messages.getString("setlobby-success"));
                }
            } else {
                sender.sendMessage(ChatColor.RED + messages.getString("setlobby-no-permission"));
            }
            return true;
        }
    }

    // Command for teleporting to lobby
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
                    player.sendMessage(ChatColor.GREEN + messages.getString("teleport-lobby-success"));
                } else {
                    player.sendMessage(ChatColor.RED + messages.getString("teleport-lobby-no-location"));
                }
            }
            return true;
        }
    }

    // Command for launching fireworks
    private class LaunchFireworkCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("customwelcomeplugin.launchfirework")) {
                    player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Firework.class);
                    player.sendMessage(ChatColor.GREEN + messages.getString("launchfirework-success"));
                } else {
                    player.sendMessage(ChatColor.RED + messages.getString("launchfirework-no-permission"));
                }
            }
            return true;
        }
    }

    // Command for reloading the plugin
    private class ReloadCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("customwelcomeplugin.reload")) {
                reloadConfig();
                messages = getMessagesConfig();
                sender.sendMessage(ChatColor.GREEN + messages.getString("reload-success"));
            } else {
                sender.sendMessage(ChatColor.RED + messages.getString("reload-fail"));
            }
            return true;
        }
    }
}
