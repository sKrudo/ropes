package tld.sima.ropesplugin;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import tld.sima.ropesplugin.events.EventsManager;

public class Main extends JavaPlugin {
	
	private HashMap<UUID, UUID> ropemap = new HashMap<UUID, UUID>();
	private HashMap<UUID, UUID> inventorymap = new HashMap<UUID, UUID>();
	
	@Override
	public void onEnable() {

		this.getCommand("rope").setExecutor(new CommandManager());

		getServer().getPluginManager().registerEvents(new EventsManager(), this);
		
		this.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Ropes Plugin is enabled");
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public HashMap<UUID, UUID> returnRopeMap(){
		return ropemap;
	}

	public HashMap<UUID, UUID> returnInventoryMap(){
		return inventorymap;
	}
}
