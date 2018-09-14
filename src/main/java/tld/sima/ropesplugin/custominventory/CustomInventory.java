package tld.sima.ropesplugin.custominventory;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tld.sima.ropesplugin.Main;

public class CustomInventory {
	
	Main plugin = Main.getPlugin(Main.class);

	private ItemStack createItem(ItemStack item, String disName, String loreName, boolean ifLore) {
		
		ItemMeta itemM = item.getItemMeta();
		itemM.setDisplayName(disName);
		
		if (ifLore) {
			ArrayList<String> itemL = new ArrayList<String>();
			itemL.add(loreName);
			itemM.setLore(itemL);
		}
		itemM.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemM.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		item.setItemMeta(itemM);
		return item;
	}
	
	public void createInventory(Player player, Vex vex) {
		Inventory i = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE + "Rope Entity Menu");
		
		Location loc = vex.getLocation();
		ItemStack location = createItem(new ItemStack(Material.STONE), ChatColor.WHITE + "Move entity", ChatColor.GRAY + "Current Location: " + ChatColor.WHITE + loc.getX() + " " + loc.getY() + " " + loc.getZ(), true);
		i.setItem(0, location);
		
		ItemStack removeentity = createItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 0), ChatColor.RED + "Remove Entity", "", false);
		i.setItem(8, removeentity);
		
		player.openInventory(i);
	}
	
}
