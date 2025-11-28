# ExpanderBarrier (Expbor) Plugin - Улучшенная версия

Advanced World Border Expander plugin with multiple features for Minecraft servers. Includes hidden achievement, unified command structure, and persistent player data.

## Features

### 1. Unified Command Structure
- `/eb` - Main command (unified for all functions)
- `/expbor` - Alternative main command
- `/expandborder` - Full English command
- `/расбор` - Short Russian command
- `/расширитьбарьер` - Full Russian command
- `/рб` - Short Russian alias

### 2. Command Arguments System
- `/eb` - Expands by 1 block (default)
- `/eb 16` - Expands by 16 blocks
- `/eb leaderboard` or `/eb top` - Shows leaderboard
- `/eb achievements` - Shows player achievements

### 3. Hidden Achievement
- "Тень барьера" - Secret achievement for 5000 total expansion blocks
- Appears in achievements list only after earning it
- Special message: "§6[§eСистема§6] §fВы получили скрытое достижение: §d"Тень барьера"!"

### 4. Permissions System
- `expbor.expand` - Basic permission to use the command
- `expbor.expand.1` - Permission to expand by 1 block
- `expbor.expand.2` - Permission to expand by 2 blocks
- `expbor.expand.4` - Permission to expand by 4 blocks
- `expbor.expand.8` - Permission to expand by 8 blocks
- `expbor.expand.16` - Permission to expand by 16 blocks
- `expbor.command.leaderboard` - Permission to use leaderboard command
- `expbor.command.achievements` - Permission to use achievements command

### 5. Data Persistence
- Player expansion data saved in YAML files (players/uuid.yml)
- Achievement data preserved between server restarts
- Total expansion blocks tracked per player

### 6. Achievements System
- "Начинающий расширяльщик" - 10 blocks expanded
- "Любитель барьеров" - 25 blocks expanded
- "Мастер границ" - 50 blocks expanded
- "Покоритель мира" - 100 blocks expanded
- "Бог расширения" - 1000 blocks expanded
- "Тень барьера" - 5000 total blocks expanded (hidden achievement)

### 7. Configuration
- Configurable diamond cost per block
- Configurable max expand size
- Configurable cooldown
- Configurable achievement requirements
- Configurable messages
- Sound settings (disabled by default)

### 8. Server Announcements
- Broadcasts when a player expands the border
- Customizable announcement format

### 9. Cooldown System
- Configurable cooldown between uses
- Prevents spamming the command

### 10. Sound Effects
- Sound effects disabled by default (requirement implemented)
- Configurable sound type, volume, and pitch

## Configuration

The plugin includes a `config.yml` file with the following settings:

```yaml
# Конфигурация ExpanderBarrier плагина
# Настройки для русскоязычных команд
enable-russian-commands: true

# Максимальное расширение за одну команду
max-expand-size: 16

# Цена за расширение (алмазы на 1 блок)
diamond-cost-per-block: 2

# Настройки оповещений
enable-server-announce: true
announce-format: "§c! §f[§6%level%§f] §f%player% §eрасширил барьер на %size% блоков."

# Настройки звука - отключаем по умолчанию (требование 4)
enable-sound: false
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
  invalid-argument: "§cНеверный аргумент. Используйте: /eb [размер] или /eb"
  no-permission: "§cУ вас нет разрешения на это действие."
  size-too-large: "§cМаксимальное расширение за раз: %max% блоков."
  cooldown-message: "§cПодождите %seconds% секунд перед повторным использованием."
  leaderboard-header: "§6§lТаблица лидеров по расширению барьера:"
  leaderboard-format: "§e%rank%. §f%player% §7- §6%total% блоков"
  no-achievements-yet: "§7У вас пока нет достижений."
  achievement-unlocked: "§6Поздравляем! §fВы получили достижение: §e%achievement%§f!"
  achievements-list-header: "§6§lВаши достижения:"
  achievement-format: "§f%achievement% §7- §e%progress%/%required% блоков"
  hidden-achievement-format: "§d%achievement% §7- §e%progress%/%required% блоков"
  hidden-achievement-unlocked: "§6[§eСистема§6] §fВы получили скрытое достижение: §d\"%achievement%\"!"
```

## How to Use

1. Place the JAR file in your server's `plugins` folder
2. Restart or reload your server
3. The plugin will generate a `config.yml` file with default settings and a `players/` directory
4. Configure the settings to your liking
5. Players can now use the unified `/eb` command for all functions

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

- `/eb` - Expand the world border by 1 block (default)
- `/eb 16` - Expand the world border by 16 blocks
- `/eb leaderboard` or `/eb top` - Show leaderboard
- `/eb achievements` - Show achievements
- Aliases: `/expbor`, `/expandborder`, `/расбор`, `/расширитьбарьер`, `/рб`