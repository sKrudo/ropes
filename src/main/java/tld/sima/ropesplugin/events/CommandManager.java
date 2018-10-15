package tld.sima.ropesplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class CommandManager implements CommandExecutor {

	String cmd1 = "rope";
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (command.getName().equalsIgnoreCase(cmd1)) {
				ItemStack tool = new ItemStack(Material.STICK);
				ItemMeta toolMeta = tool.getItemMeta();
				toolMeta.setDisplayName(ChatColor.GREEN + "Rope Placer");
				tool.setItemMeta(toolMeta);
				Player player = (Player) sender;
				if (!player.getInventory().contains(tool)) {
					player.getInventory().addItem(tool);
				}
				return true;
			}
		}else {
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "You have to be a player to use this command");
			return true;
		}
		return false;
	}
	
}
