package me.customWelcomePlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomWelcomePlugin extends JavaPlugin implements Listener {

    private File scoreboardConfigFile;
    private FileConfiguration scoreboardConfig;
    private Map<String, Long> playerJoinTimes = new HashMap<>();
    private Location lobbyLocation;
    private int titleIndex = 0;  // Used to keep track of which title is being displayed
    private List<String> animatedTitles;
    private int animationTaskId;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        loadLobbyLocation();
        loadScoreboardConfig();
        startScoreboardAnimation();
        getLogger().info("CustomWelcomePlugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomWelcomePlugin Disabled!");
        Bukkit.getScheduler().cancelTask(animationTaskId);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        long currentTime = System.currentTimeMillis();

        // Custom welcome message logic based on join time
        if (!playerJoinTimes.containsKey(player.getName()) || (currentTime - playerJoinTimes.get(player.getName())) > 300000) {
            player.sendMessage(ChatColor.GOLD + "Welcome to OneHypixelCracked Network, " + ChatColor.AQUA + player.getName() + ChatColor.GOLD + "!");
            playFirework(player.getLocation());
            playerJoinTimes.put(player.getName(), currentTime);
        } else {
            player.sendMessage(ChatColor.YELLOW + "Welcome back, " + ChatColor.AQUA + player.getName() + ChatColor.YELLOW + "!");
        }

        // Show the scoreboard to the player
        showScoreboard(player);
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

    private void showScoreboard(Player player) {
        // Get current animated title
        String title = ChatColor.translateAlternateColorCodes('&', animatedTitles.get(titleIndex));
        List<String> lines = scoreboardConfig.getStringList("lines");

        // Create a new scoreboard and objective
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("customWelcome", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(title); // Set the animated title

        // Set scoreboard lines with placeholders
        int score = lines.size();
        for (String line : lines) {
            String processedLine = line
                    .replace("%player_name%", player.getName())
                    .replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()))
                    .replace("%rank%", getRank(player))
                    .replace("%ping%", String.valueOf(getPing(player)))
                    .replace("%lobby_name%", (lobbyLocation != null ? lobbyLocation.getWorld().getName() : "Lobby"));

            objective.getScore(ChatColor.translateAlternateColorCodes('&', processedLine)).setScore(score);
            score--;
        }

        // Assign the scoreboard to the player
        player.setScoreboard(scoreboard);
    }

    private String getRank(Player player) {
        return "Member";  // Replace with actual rank fetching logic
    }

    private int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void loadScoreboardConfig() {
        scoreboardConfigFile = new File(getDataFolder(), "scoreboard.yml");
        if (!scoreboardConfigFile.exists()) {
            scoreboardConfigFile.getParentFile().mkdirs();
            saveResource("scoreboard.yml", false);
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardConfigFile);
        animatedTitles = scoreboardConfig.getStringList("title");  // Load the animated title list
    }

    private void startScoreboardAnimation() {
        animationTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            // Update title index for the next title
            titleIndex = (titleIndex + 1) % animatedTitles.size();

            // Update the scoreboard for all players
            for (Player player : Bukkit.getOnlinePlayers()) {
                showScoreboard(player);
            }
        }, 0L, 20L);  // Update every 20 ticks (1 second)
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

    // Command handling for /setlobby, /l, /lobby, and /launchfirework
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        switch (command.getName().toLowerCase()) {
            case "setlobby":
                if (player.hasPermission("customwelcomeplugin.setlobby")) {
                    lobbyLocation = player.getLocation();
                    saveLobbyLocation(lobbyLocation);
                    player.sendMessage(ChatColor.GREEN + "Lobby location set!");
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to set the lobby location.");
                }
                return true;

            case "l":
            case "lobby":
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

            case "launchfirework":
                if (player.hasPermission("customwelcomeplugin.launchfirework")) {
                    playFirework(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Firework launched!");
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to launch a firework.");
                }
                return true;

            default:
                return false;
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
