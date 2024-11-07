# CustomWelcomePlugin

**CustomWelcomePlugin** is a versatile Minecraft plugin that enhances the server experience by sending custom welcome messages, playing fireworks, and providing useful commands like teleportation to a lobby and fireball launching. It’s designed to make your server more engaging for players and easy to manage for server admins.

## Features

- **First-Time Join Welcome**: When a player joins the server for the first time, they’ll receive a personalized welcome message and a colorful firework show at their location.
- **Returning Player Greeting**: Players who have joined before are greeted with a welcome back message.
- **Lobby System**:
- **This Commads Of lobby System**
  - **/setlobby**: Allows server admins to set a lobby location.
  - **/lobby** or **/l**: Teleports players to the set lobby location.
- **Fireball Launching**: Authorized players can launch a fireball with the `/launchfireball` command.
- **Permission-based Access**: Every command can be restricted using permissions, allowing server admins to control who can use them.

## Commands & Permissions

| Command            | Description                                  | Permission                                | Default Access |
|--------------------|----------------------------------------------|-------------------------------------------|----------------|
| `/setlobby`        | Set the server's lobby location.             | `customwelcomeplugin.setlobby`            | OPs only       |
| `/lobby` or `/l`   | Teleport to the set lobby location.          | `customwelcomeplugin.lobby`               | All players    |
| `/launchfireball`  | Launch a fireball at your location.          | `customwelcomeplugin.launchfireball`      | OPs only       |

## Installation

### Prerequisites

- A **Minecraft** server running **Spigot** or **Paper**.
- **Java 8** or higher.
- A **permissions plugin** like **LuckPerms** for managing permissions.

### Steps to Install

1. Download the `CustomWelcomePlugin.jar` file.
2. Place it in the `plugins` directory of your Minecraft server.
3. Restart or reload the server.
4. The plugin will automatically generate a `config.yml` file in the `plugins/CustomWelcomePlugin/` folder.

### Setting Permissions

Use a permissions plugin (like **LuckPerms**) to manage which players can use specific commands:

- `customwelcomeplugin.setlobby` — Permission to set the lobby location.
- `customwelcomeplugin.lobby` — Permission to teleport to the lobby.
- `customwelcomeplugin.launchfireball` — Permission to launch a fireball.

## Configuration

### Lobby Location

The plugin allows you to set a lobby location with the `/setlobby` command. This location is saved in the `config.yml` file, so players can teleport back to it using `/lobby` or `/l`.

#### Example `config.yml`:
```yaml
lobby:
  world: world
  x: 100.5
  y: 65.0
  z: -200.5
  yaw: 90.0
  pitch: 0.0
