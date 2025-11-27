package me.yourname.worldborderexpander;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldBorderExpander extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("WorldBorderExpander включён!");
    }

    @Override
    public void onDisable() {
        getLogger().info("WorldBorderExpander выключен!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("expandborder")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Только игроки могут использовать эту команду.");
                return true;
            }

            Player player = (Player) sender;
            World world = player.getWorld();

            ItemStack diamonds = new ItemStack(Material.DIAMOND, 2);
            if (player.getInventory().containsAtLeast(diamonds, 2)) {
                player.getInventory().removeItem(diamonds);
                world.getWorldBorder().setSize(world.getWorldBorder().getSize() + 1);
                player.sendMessage("Граница мира расширена на 1 блок за 2 алмаза.");
            } else {
                player.sendMessage("У вас недостаточно алмазов (нужно 2).");
            }
            return true;
        }
        return false;
    }
}
