package me.customWelcomePlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CustomWelcomePlugin extends JavaPlugin implements Listener, CommandExecutor {

    private Map<String, Long> playerJoinTimes = new HashMap<>();
    private Location lobbyLocation;

    @Override
    public void onEnable() {
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(this, this);

        // Register commands
        this.getCommand("launchfireball").setExecutor(this);
        this.getCommand("lobby").setExecutor(this);
        this.getCommand("l").setExecutor(this);
        this.getCommand("setlobby").setExecutor(this);

        // Load lobby location from the config
        loadLobbyLocation();

        // Log plugin status
        getLogger().info("CustomWelcomePlugin Enabled!");
    }

    @Override
    public void onDisable() {
        // Log plugin shutdown
        getLogger().info("CustomWelcomePlugin Disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        long currentTime = System.currentTimeMillis();
        String playerName = player.getName();

        // Check if the player is joining for the first time or has been away for more than 5 minutes
        if (!playerJoinTimes.containsKey(playerName) || (currentTime - playerJoinTimes.get(playerName)) > 300000) {
            // Send the welcome message for first-time or players who have been away for more than 5 minutes
            player.sendMessage(ChatColor.GOLD + "Welcome to PracticePlace Network, " + ChatColor.AQUA + player.getName() + ChatColor.GOLD + "!");
            playFirework(player.getLocation());
            player.sendMessage(ChatColor.GREEN + "Enjoy your time on the server!");

            // Update the player's join time
            playerJoinTimes.put(playerName, currentTime);
        } else {
            // For returning players who are back within 5 minutes, only a general welcome
            player.sendMessage(ChatColor.YELLOW + "Welcome back, " + ChatColor.AQUA + player.getName() + ChatColor.YELLOW + "!");
        }

        // Broadcast a general welcome message to all players (optional)
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Launched PracticePlace");
    }

    // Method to spawn fireworks at the given location
    private void playFirework(Location location) {
        if (location.getWorld() == null) return;

        // Spawn a firework at the location
        Firework firework = location.getWorld().spawn(location, Firework.class);

        // Create a firework effect (Red color with a Green fade)
        FireworkEffect effect = FireworkEffect.builder()
                .with(Type.BALL)
                .withColor(Color.RED)
                .withFade(Color.GREEN)
                .trail(true)
                .flicker(true)
                .build();

        // Apply the effect to the firework
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(effect);
        meta.setPower(1); // Set the power (height) of the firework
        firework.setFireworkMeta(meta);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            switch (command.getName().toLowerCase()) {
                case "launchfireball":
                    // Handle launching a fireball
                    if (player.hasPermission("customwelcomeplugin.launchfireball")) {
                        player.launchProjectile(Fireball.class);
                        player.sendMessage(ChatColor.RED + "You launched a fireball!");
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    }
                    return true;

                case "setlobby":
                    // Handle setting the lobby location
                    if (player.hasPermission("customwelcomeplugin.setlobby")) {
                        lobbyLocation = player.getLocation();
                        saveLobbyLocation(lobbyLocation);
                        player.sendMessage(ChatColor.GREEN + "Lobby location set!");
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to set the lobby location.");
                    }
                    return true;

                case "lobby":
                case "l":
                    // Handle teleporting to the lobby
                    if (player.hasPermission("customwelcomeplugin.lobby")) {
                        if (lobbyLocation != null) {
                            player.teleport(lobbyLocation);
                            player.sendMessage(ChatColor.GREEN + "Teleported to the lobby.");
                        } else {
                            player.sendMessage(ChatColor.RED + "Lobby location is not set.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to teleport to the lobby.");
                    }
                    return true;

                default:
                    return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
    }

    // Load the lobby location from the configuration file
    private void loadLobbyLocation() {
        FileConfiguration config = this.getConfig();
        if (config.contains("lobby")) {
            lobbyLocation = new Location(
                    Bukkit.getWorld(config.getString("lobby.world")),
                    config.getDouble("lobby.x"),
                    config.getDouble("lobby.y"),
                    config.getDouble("lobby.z"),
                    (float) config.getDouble("lobby.yaw"),
                    (float) config.getDouble("lobby.pitch")
            );
        }
    }

    // Save the lobby location to the configuration file
    private void saveLobbyLocation(Location location) {
        FileConfiguration config = this.getConfig();
        config.set("lobby.world", location.getWorld().getName());
        config.set("lobby.x", location.getX());
        config.set("lobby.y", location.getY());
        config.set("lobby.z", location.getZ());
        config.set("lobby.yaw", location.getYaw());
        config.set("lobby.pitch", location.getPitch());
        saveConfig();
    }
}
