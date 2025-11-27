package me.yourname.worldborderexpander;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Sound;

import java.util.*;

public class WorldBorderExpander extends JavaPlugin {
    
    private FileConfiguration config;
    private Map<UUID, Long> cooldowns = new HashMap<>();
    private Map<UUID, Integer> playerExpansions = new HashMap<>();
    private Map<UUID, Set<String>> playerAchievements = new HashMap<>();
    
    // Достижения
    private final Map<String, Integer> achievementRequirements = new LinkedHashMap<>();
    
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
        
        // Загрузка данных игроков (в реальном плагине это будет из файла/БД)
        loadPlayerData();
        
        getLogger().info("ExpanderBarrier включён!");
    }

    @Override
    public void onDisable() {
        savePlayerData();
        getLogger().info("ExpanderBarrier выключен!");
    }
    
    private void loadPlayerData() {
        // В реальном плагине это будет загрузка из YAML/БД
        // Пока просто инициализируем пустыми значениями
    }
    
    private void savePlayerData() {
        // В реальном плагине это будет сохранение в YAML/БД
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("expbor") || 
            label.equalsIgnoreCase("eb") || 
            label.equalsIgnoreCase("расширить") || 
            label.equalsIgnoreCase("барьер")) {
            
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
            
            int size = 1; // По умолчанию
            
            if (args.length > 0) {
                try {
                    size = Integer.parseInt(args[0]);
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
                } catch (NumberFormatException e) {
                    String message = config.getString("messages.invalid-argument", "§cНеверный аргумент. Используйте: /expbor [размер] или /expbor");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    return true;
                }
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
                    // В реальном плагине здесь можно было бы подставить уровень игрока
                    announce = announce.replace("%player%", player.getName())
                                       .replace("%size%", String.valueOf(size))
                                       .replace("%level%", String.valueOf(getPlayerLevel(player)));
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', announce));
                }
                
                // Проигрываем звук
                if (config.getBoolean("enable-sound", true)) {
                    String soundType = config.getString("sound-type", "BLOCK_NOTE_BLOCK_HARP");
                    float volume = (float) config.getDouble("sound-volume", 1.0);
                    float pitch = (float) config.getDouble("sound-pitch", 1.0);
                    player.playSound(player.getLocation(), soundType, volume, pitch);
                }
                
                // Устанавливаем кулдаун
                setCooldown(player);
                
            } else {
                String message = config.getString("messages.need-diamonds", "§cУ вас недостаточно алмазов (нужно %cost%).");
                message = message.replace("%cost%", String.valueOf(cost));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
            return true;
        }
        
        else if (command.getName().equalsIgnoreCase("expborleaderboard") || 
                 label.equalsIgnoreCase("ebtop") || 
                 label.equalsIgnoreCase("топ")) {
            
            showLeaderboard(sender);
            return true;
        }
        
        else if (command.getName().equalsIgnoreCase("expborachievements") || 
                 label.equalsIgnoreCase("ebach") || 
                 label.equalsIgnoreCase("достижения")) {
            
            showAchievements(sender);
            return true;
        }
        
        return false;
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
        
        if (!hasAchievements) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-achievements-yet", "§7У вас пока нет достижений.")));
        }
    }
    
    private void checkAchievements(Player player) {
        UUID playerId = player.getUniqueId();
        int totalExpansions = playerExpansions.getOrDefault(playerId, 0);
        Set<String> unlocked = playerAchievements.getOrDefault(playerId, new HashSet<>());
        
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
                
                // Проигрываем звук при получении достижения
                if (config.getBoolean("enable-sound", true)) {
                    String soundType = config.getString("sound-type", "BLOCK_NOTE_BLOCK_HARP");
                    float volume = (float) config.getDouble("sound-volume", 1.0);
                    float pitch = (float) config.getDouble("sound-pitch", 1.5); // Высокий тон для достижений
                    player.playSound(player.getLocation(), soundType, volume, pitch);
                }
            }
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
