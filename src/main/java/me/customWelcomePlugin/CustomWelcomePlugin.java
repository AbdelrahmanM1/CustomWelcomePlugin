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

import java.util.HashSet;
import java.util.Set;

public class CustomWelcomePlugin extends JavaPlugin implements Listener, CommandExecutor {

    private Set<String> firstTimePlayers = new HashSet<>();
    private Location lobbyLocation;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("launchfireball").setExecutor(this);
        this.getCommand("lobby").setExecutor(this);
        this.getCommand("l").setExecutor(this);
        this.getCommand("setlobby").setExecutor(this);

        loadLobbyLocation();
        getLogger().info("CustomWelcomePlugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomWelcomePlugin Disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if the player is joining for the first time
        if (firstTimePlayers.add(player.getName())) {
            // First-time join message
            player.sendMessage(ChatColor.GOLD + "Welcome to PracticePlace Network, " + ChatColor.AQUA + player.getName() + ChatColor.GOLD + "! Have a fun!");
            playFirework(player.getLocation());
        } else {
            // Returning player message
            player.sendMessage(ChatColor.YELLOW + "Welcome back, " + ChatColor.AQUA + player.getName() + ChatColor.YELLOW + "!");
        }

        // Broadcast a welcome message to all players
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Launched PracticePlace");
    }

    private void playFirework(Location location) {
        if (location.getWorld() == null) return;

        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkEffect effect = FireworkEffect.builder()
                .with(Type.BALL)
                .withColor(Color.RED)
                .withFade(Color.GREEN)
                .trail(true)
                .flicker(true)
                .build();

        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(effect);
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            switch (command.getName().toLowerCase()) {
                case "launchfireball":
                    if (player.hasPermission("customwelcomeplugin.launchfireball")) {
                        player.launchProjectile(Fireball.class);
                        player.sendMessage(ChatColor.RED + "You launched a fireball!");
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    }
                    return true;

                case "setlobby":
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
