package me.yourname.worldborderexpander;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WorldBorderExpander extends JavaPlugin {
    
    private FileConfiguration config;
    private Map<UUID, Long> cooldowns = new HashMap<>();
    private Map<UUID, Integer> playerExpansions = new HashMap<>();
    private Map<UUID, Set<String>> playerAchievements = new HashMap<>();
    
    // Достижения
    private final Map<String, Integer> achievementRequirements = new LinkedHashMap<>();
    
    // Добавляем скрытое достижение
    private static final String HIDDEN_ACHIEVEMENT_NAME = "Тень барьера";
    private static final int HIDDEN_ACHIEVEMENT_THRESHOLD = 5000;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        
        // Инициализация требований достижений
        achievementRequirements.put("Начинающий расширяльщик", config.getInt("achievements.beginner"));
        achievementRequirements.put("Любитель барьеров", config.getInt("achievements.enthusiast"));
        achievementRequirements.put("Мастер границ", config.getInt("achievements.master"));
        achievementRequirements.put("Покоритель мира", config.getInt("achievements.conqueror"));
        achievementRequirements.put("Бог расширения", config.getInt("achievements.god"));
        
        // Загрузка данных игроков из YAML файлов
        loadPlayerData();
        
        getLogger().info("ExpanderBarrier включён!");
    }

    @Override
    public void onDisable() {
        savePlayerData();
        getLogger().info("ExpanderBarrier выключен!");
    }
    
    private void loadPlayerData() {
        // Создаем папку players, если её нет
        File playersDir = new File(getDataFolder(), "players");
        if (!playersDir.exists()) {
            playersDir.mkdirs();
        }
        
        // Загружаем данные всех игроков из YAML файлов
        File[] playerFiles = playersDir.listFiles();
        if (playerFiles != null) {
            for (File file : playerFiles) {
                if (file.getName().endsWith(".yml")) {
                    try {
                        String uuidStr = file.getName().replace(".yml", "");
                        UUID uuid = UUID.fromString(uuidStr);
                        
                        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
                        
                        // Загружаем количество расширенных блоков
                        int expansions = playerConfig.getInt("total_expansions", 0);
                        playerExpansions.put(uuid, expansions);
                        
                        // Загружаем достижения
                        Set<String> achievements = new HashSet<>();
                        List<String> achievementsList = playerConfig.getStringList("achievements");
                        achievements.addAll(achievementsList);
                        playerAchievements.put(uuid, achievements);
                        
                    } catch (Exception e) {
                        getLogger().warning("Ошибка при загрузке данных игрока " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private void savePlayerData() {
        // Сохраняем данные всех игроков в YAML файлы
        File playersDir = new File(getDataFolder(), "players");
        if (!playersDir.exists()) {
            playersDir.mkdirs();
        }
        
        for (UUID uuid : playerExpansions.keySet()) {
            try {
                File playerFile = new File(playersDir, uuid.toString() + ".yml");
                YamlConfiguration playerConfig = new YamlConfiguration();
                
                playerConfig.set("total_expansions", playerExpansions.get(uuid));
                
                Set<String> achievements = playerAchievements.get(uuid);
                if (achievements != null) {
                    playerConfig.set("achievements", new ArrayList<>(achievements));
                }
                
                playerConfig.save(playerFile);
            } catch (IOException e) {
                getLogger().warning("Ошибка при сохранении данных игрока " + uuid.toString() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdLabel = label.toLowerCase();
        
        // Проверяем, является ли команда одной из разрешенных
        if (isExpanderCommand(cmdLabel)) {
            // Если нет аргументов - расширяем на 1 блок (по умолчанию)
            if (args.length == 0) {
                return expandBorder(sender, 1);
            }
            
            // Проверяем первый аргумент
            String firstArg = args[0].toLowerCase();
            
            // Подкоманды
            switch (firstArg) {
                case "leaderboard":
                case "top":
                    showLeaderboard(sender);
                    return true;
                    
                case "achievements":
                case "achs":
                case "ach":
                    showAchievements(sender);
                    return true;
                    
                default:
                    // Пытаемся расширить на указанное количество блоков
                    try {
                        int size = Integer.parseInt(firstArg);
                        return expandBorder(sender, size);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                            config.getString("messages.invalid-argument", "§cНеверный аргумент. Используйте: /eb [размер] или /eb")));
                        return true;
                    }
            }
        }
        
        return false;
    }
    
    private boolean isExpanderCommand(String label) {
        return label.equals("eb") || 
               label.equals("expbor") || 
               label.equals("expandborder") ||
               label.equals("расбор") ||
               label.equals("расширитьбарьер") ||
               label.equals("рб");
    }
    
    private boolean expandBorder(CommandSender sender, int size) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§cТолько игроки могут использовать эту команду."));
            return true;
        }

        Player player = (Player) sender;
        
        // Проверка кулдауна
        if (hasCooldown(player)) {
            int remaining = getCooldownSeconds(player);
            String message = config.getString("messages.cooldown-message", "§cПодождите %seconds% секунд перед повторным использованием.");
            message = message.replace("%seconds%", String.valueOf(remaining));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        if (size < 1) {
            size = 1;
        }
        if (size > config.getInt("max-expand-size", 16)) {
            String message = config.getString("messages.size-too-large", "§cМаксимальное расширение за раз: %max% блоков.");
            message = message.replace("%max%", String.valueOf(config.getInt("max-expand-size", 16)));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        // Проверка пермиссии для конкретного размера
        if (!player.hasPermission("expbor.expand." + size) && size > 1) {
            String message = config.getString("messages.no-permission", "§cУ вас нет разрешения на это действие.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        // Проверка пермиссии для базового расширения
        if (!player.hasPermission("expbor.expand")) {
            String message = config.getString("messages.no-permission", "§cУ вас нет разрешения на это действие.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }
        
        int cost = size * config.getInt("diamond-cost-per-block", 2);
        ItemStack diamonds = new ItemStack(Material.DIAMOND, cost);
        
        if (player.getInventory().containsAtLeast(diamonds, cost)) {
            World world = player.getWorld();
            player.getInventory().removeItem(diamonds);
            world.getWorldBorder().setSize(world.getWorldBorder().getSize() + size);
            
            // Добавляем к общему количеству расширений игрока
            UUID playerId = player.getUniqueId();
            playerExpansions.put(playerId, playerExpansions.getOrDefault(playerId, 0) + size);
            
            // Проверяем достижения
            checkAchievements(player);
            
            // Отправляем сообщение игроку
            String successMessage = config.getString("messages.expanded-success", "§aГраница мира расширена на %size% блоков за %cost% алмазов.");
            successMessage = successMessage.replace("%size%", String.valueOf(size))
                                          .replace("%cost%", String.valueOf(cost));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', successMessage));
            
            // Оповещение всем игрокам
            if (config.getBoolean("enable-server-announce", true)) {
                String announce = config.getString("announce-format", "§c! §f[§6%level%§f] §f%player% §eрасширил барьер на %size% блоков.");
                announce = announce.replace("%player%", player.getName())
                                   .replace("%size%", String.valueOf(size))
                                   .replace("%level%", String.valueOf(getPlayerLevel(player)));
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', announce));
            }
            
            // Удаляем звук нотного блока (требование 4)
            // Ранее здесь был код проигрывания звука, теперь он удален
            
            // Устанавливаем кулдаун
            setCooldown(player);
            
        } else {
            String message = config.getString("messages.need-diamonds", "§cУ вас недостаточно алмазов (нужно %cost%).");
            message = message.replace("%cost%", String.valueOf(cost));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
        return true;
    }
    
    private void showLeaderboard(CommandSender sender) {
        String header = config.getString("messages.leaderboard-header", "§6§lТаблица лидеров по расширению барьера:");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
        
        // В реальном плагине здесь будет сортировка из базы данных
        // Пока используем тестовые данные
        List<Map.Entry<UUID, Integer>> sortedPlayers = new ArrayList<>(playerExpansions.entrySet());
        sortedPlayers.sort(Map.Entry.<UUID, Integer>comparingByValue().reversed());
        
        int size = config.getInt("leaderboard-size", 10);
        int rank = 1;
        
        for (Map.Entry<UUID, Integer> entry : sortedPlayers) {
            if (rank > size) break;
            
            Player player = Bukkit.getPlayer(entry.getKey());
            String playerName = player != null ? player.getName() : "Unknown";
            int total = entry.getValue();
            
            String format = config.getString("messages.leaderboard-format", "§e%rank%. §f%player% §7- §6%total% блоков");
            format = format.replace("%rank%", String.valueOf(rank))
                          .replace("%player%", playerName)
                          .replace("%total%", String.valueOf(total));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', format));
            rank++;
        }
        
        if (sortedPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§7Пока никто не расширял барьер."));
        }
    }
    
    private void showAchievements(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§cТолько игроки могут использовать эту команду."));
            return;
        }
        
        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();
        
        String header = config.getString("messages.achievements-list-header", "§6§lВаши достижения:");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
        
        Set<String> unlocked = playerAchievements.getOrDefault(playerId, new HashSet<>());
        int totalExpansions = playerExpansions.getOrDefault(playerId, 0);
        
        boolean hasAchievements = false;
        for (Map.Entry<String, Integer> achievement : achievementRequirements.entrySet()) {
            String name = achievement.getKey();
            int required = achievement.getValue();
            
            if (unlocked.contains(name)) {
                String format = config.getString("messages.achievement-format", "§f%achievement% §7- §e%progress%/%required% блоков");
                format = format.replace("%achievement%", name)
                              .replace("%progress%", String.valueOf(totalExpansions))
                              .replace("%required%", String.valueOf(required));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', format));
                hasAchievements = true;
            } else if (totalExpansions >= required) {
                // Игрок заслужил достижение, но еще не получил
                String format = config.getString("messages.achievement-format", "§f%achievement% §7- §e%progress%/%required% блоков");
                format = format.replace("%achievement%", name)
                              .replace("%progress%", String.valueOf(totalExpansions))
                              .replace("%required%", String.valueOf(required));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§aНовое! " + format));
                hasAchievements = true;
            } else {
                String format = config.getString("messages.achievement-format", "§f%achievement% §7- §e%progress%/%required% блоков");
                format = format.replace("%achievement%", name)
                              .replace("%progress%", String.valueOf(totalExpansions))
                              .replace("%required%", String.valueOf(required));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', format));
            }
        }
        
        // Проверяем скрытое достижение
        if (unlocked.contains(HIDDEN_ACHIEVEMENT_NAME)) {
            String hiddenFormat = config.getString("messages.hidden-achievement-format", "§d%achievement% §7- §e%progress%/%required% блоков");
            hiddenFormat = hiddenFormat.replace("%achievement%", HIDDEN_ACHIEVEMENT_NAME)
                                      .replace("%progress%", String.valueOf(totalExpansions))
                                      .replace("%required%", String.valueOf(HIDDEN_ACHIEVEMENT_THRESHOLD));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', hiddenFormat));
            hasAchievements = true;
        } else if (totalExpansions >= HIDDEN_ACHIEVEMENT_THRESHOLD) {
            // Игрок заслужил скрытое достижение, но еще не получил
            String hiddenFormat = config.getString("messages.hidden-achievement-format", "§d%achievement% §7- §e%progress%/%required% блоков");
            hiddenFormat = hiddenFormat.replace("%achievement%", HIDDEN_ACHIEVEMENT_NAME)
                                      .replace("%progress%", String.valueOf(totalExpansions))
                                      .replace("%required%", String.valueOf(HIDDEN_ACHIEVEMENT_THRESHOLD));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§aНовое! " + hiddenFormat));
            hasAchievements = true;
        }
        
        if (!hasAchievements) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-achievements-yet", "§7У вас пока нет достижений.")));
        }
    }
    
    private void checkAchievements(Player player) {
        UUID playerId = player.getUniqueId();
        int totalExpansions = playerExpansions.getOrDefault(playerId, 0);
        Set<String> unlocked = playerAchievements.getOrDefault(playerId, new HashSet<>());
        
        // Проверяем обычные достижения
        for (Map.Entry<String, Integer> achievement : achievementRequirements.entrySet()) {
            String name = achievement.getKey();
            int required = achievement.getValue();
            
            if (totalExpansions >= required && !unlocked.contains(name)) {
                // Игрок получил новое достижение
                unlocked.add(name);
                playerAchievements.put(playerId, unlocked);
                
                String message = config.getString("messages.achievement-unlocked", "§6Поздравляем! §fВы получили достижение: §e%achievement%§f!");
                message = message.replace("%achievement%", name);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                
                // Удаляем звук при получении достижения (требование 4)
                // Ранее здесь был код проигрывания звука, теперь он удален
            }
        }
        
        // Проверяем скрытое достижение
        if (totalExpansions >= HIDDEN_ACHIEVEMENT_THRESHOLD && !unlocked.contains(HIDDEN_ACHIEVEMENT_NAME)) {
            // Игрок получил скрытое достижение
            unlocked.add(HIDDEN_ACHIEVEMENT_NAME);
            playerAchievements.put(playerId, unlocked);
            
            // Отправляем специальное сообщение о скрытом достижении
            String message = config.getString("messages.hidden-achievement-unlocked", "§6[§eСистема§6] §fВы получили скрытое достижение: §d\"%achievement%\"!");
            message = message.replace("%achievement%", HIDDEN_ACHIEVEMENT_NAME);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            
            // Удаляем звук при получении достижения (требование 4)
            // Ранее здесь был код проигрывания звука, теперь он удален
        }
    }
    
    private boolean hasCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        long lastUsed = cooldowns.get(playerId);
        long cooldownSeconds = config.getLong("cooldown-seconds", 5);
        long currentTime = System.currentTimeMillis() / 1000;
        
        return (currentTime - lastUsed) < cooldownSeconds;
    }
    
    private int getCooldownSeconds(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long lastUsed = cooldowns.get(playerId);
        long cooldownSeconds = config.getLong("cooldown-seconds", 5);
        long currentTime = System.currentTimeMillis() / 1000;
        
        long elapsed = currentTime - lastUsed;
        return (int) Math.max(0, cooldownSeconds - elapsed);
    }
    
    private void setCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis() / 1000;
        cooldowns.put(playerId, currentTime);
    }
    
    private int getPlayerLevel(Player player) {
        // В реальном плагине это будет получение уровня из другого плагина
        // Пока просто возвращаем 1
        return 1;
    }
}
