# ExpanderBarrier (Expbor) Plugin

Advanced World Border Expander plugin with multiple features for Minecraft servers.

## Features

### 1. Command Aliases
- `/expbor` - Main command
- `/eb` - Short alias
- `/расширить` - Russian command
- `/барьер` - Russian command

### 2. Expand with Size Parameter
- `/expbor` - Expands by 1 block (default)
- `/expbor 8` - Expands by 8 blocks
- `/eb 16` - Expands by 16 blocks

### 3. Permissions System
- `expbor.expand` - Basic permission to use the command
- `expbor.expand.1` - Permission to expand by 1 block
- `expbor.expand.2` - Permission to expand by 2 blocks
- `expbor.expand.4` - Permission to expand by 4 blocks
- `expbor.expand.8` - Permission to expand by 8 blocks
- `expbor.expand.16` - Permission to expand by 16 blocks
- `expbor.command.leaderboard` - Permission to use leaderboard command
- `expbor.command.achievements` - Permission to use achievements command

### 4. Leaderboard System
- `/expbor leaderboard` or `/ebtop` or `/топ` - Shows top 10 players by expansion
- Shows total blocks expanded by each player

### 5. Achievements System
- `/expbor achievements` or `/ebach` or `/достижения` - Shows player achievements
- Achievements:
  - "Начинающий расширяльщик" - 10 blocks expanded
  - "Любитель барьеров" - 25 blocks expanded
  - "Мастер границ" - 50 blocks expanded
  - "Покоритель мира" - 100 blocks expanded
  - "Бог расширения" - 1000 blocks expanded

### 6. Configuration
- Configurable diamond cost per block
- Configurable max expand size
- Configurable cooldown
- Configurable achievement requirements
- Configurable messages
- Sound settings

### 7. Server Announcements
- Broadcasts when a player expands the border
- Customizable announcement format

### 8. Cooldown System
- Configurable cooldown between uses
- Prevents spamming the command

### 9. Sound Effects
- Plays sound when expanding successfully
- Plays special sound when achieving an achievement
- Configurable sound type, volume, and pitch

## Configuration

The plugin includes a `config.yml` file with the following settings:

```yaml
# Настройки для русскоязычных команд
enable-russian-commands: true

# Максимальное расширение за одну команду
max-expand-size: 16

# Цена за расширение (алмазы на 1 блок)
diamond-cost-per-block: 2

# Настройки оповещений
enable-server-announce: true
announce-format: "§c! §f[§6%level%§f] §f%player% §eрасширил барьер на %size% блоков."

# Настройки звука
enable-sound: true
sound-type: "block.note_block.harp"
sound-volume: 1.0
sound-pitch: 1.0

# Настройки таблицы лидеров
leaderboard-size: 10

# Настройки достижений
achievements:
  beginner: 10
  enthusiast: 25
  master: 50
  conqueror: 100
  god: 1000

# Настройки кулдауна (в секундах)
cooldown-seconds: 5

# Сообщения
messages:
  need-diamonds: "§cУ вас недостаточно алмазов (нужно %cost%)."
  expanded-success: "§aГраница мира расширена на %size% блоков за %cost% алмазов."
  invalid-argument: "§cНеверный аргумент. Используйте: /expbor [размер] или /expbor"
  no-permission: "§cУ вас нет разрешения на это действие."
  size-too-large: "§cМаксимальное расширение за раз: %max% блоков."
  cooldown-message: "§cПодождите %seconds% секунд перед повторным использованием."
  leaderboard-header: "§6§lТаблица лидеров по расширению барьера:"
  leaderboard-format: "§e%rank%. §f%player% §7- §6%total% блоков"
  no-achievements-yet: "§7У вас пока нет достижений."
  achievement-unlocked: "§6Поздравляем! §fВы получили достижение: §e%achievement%§f!"
  achievements-list-header: "§6§lВаши достижения:"
  achievement-format: "§f%achievement% §7- §e%progress%/%required% блоков"
```

## How to Use

1. Place the JAR file in your server's `plugins` folder
2. Restart or reload your server
3. The plugin will generate a `config.yml` file with default settings
4. Configure the settings to your liking
5. Players can now use the `/expbor` command to expand the world border

## Compilation

To compile the plugin from source:

1. Make sure you have Java JDK 8 or higher installed
2. Make sure you have Maven installed
3. Navigate to the project directory in your terminal
4. Run the following command to compile the plugin:
   ```
   mvn clean package
   ```
5. The compiled JAR file will be located in the `target/` directory as `ExpanderBarrier-1.0.jar`
6. Place the JAR file in your Minecraft server's `plugins` folder

## Commands Summary

- `/expbor [размер]` - Expand the world border by specified number of blocks (default 1)
- `/eb [размер]` - Short alias for expbor
- `/расширить [размер]` - Russian command for expand
- `/барьер [размер]` - Russian command for barrier
- `/expbor leaderboard` - Show leaderboard
- `/ebtop` - Short alias for leaderboard
- `/топ` - Russian command for leaderboard
- `/expbor achievements` - Show achievements
- `/ebach` - Short alias for achievements
- `/достижения` - Russian command for achievements