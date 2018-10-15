package tld.sima.ropesplugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import tld.sima.ropesplugin.events.CommandManager;
import tld.sima.ropesplugin.events.EventsManager;
import tld.sima.ropesplugin.events.PacketHandler;

public class Main extends JavaPlugin {
	
	private HashMap<UUID, UUID> ropemap = new HashMap<UUID, UUID>();
	private HashMap<UUID, UUID> inventorymap = new HashMap<UUID, UUID>();
	private HashMap<UUID, UUID> fullListOfRopes = new HashMap<UUID, UUID>();
	private Set<Location> locSet = new HashSet<Location>();
	private StorageManager stmgr;
	private PacketHandler pkhdlr;

	@Override
	public void onEnable() {

		this.getCommand("rope").setExecutor(new CommandManager());
		
		stmgr = new StorageManager();
		stmgr.setup();
		fullListOfRopes = stmgr.getList();
		for (UUID uuid : fullListOfRopes.keySet()) {
			Entity entity = Bukkit.getEntity(uuid);
			if (entity != null) {
				locSet.add(entity.getLocation());
			}else {
				fullListOfRopes.remove(uuid);
			}
		}
		this.pkhdlr = new PacketHandler();
		for (Player player : Bukkit.getOnlinePlayers()) {
			pkhdlr.listen(player);
		}
		getServer().getPluginManager().registerEvents(new EventsManager(), this);

		this.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Ropes Plugin is enabled");
	}
	
	@Override
	public void onDisable() {
		stmgr.finalSave(fullListOfRopes);
		

		for (Player player : Bukkit.getOnlinePlayers()) {
			pkhdlr.silence(player);
		}
	}
	
	public HashMap<UUID, UUID> returnRopeMap(){
		return ropemap;
	}

	public HashMap<UUID, UUID> returnInventoryMap(){
		return inventorymap;
	}
	
	public HashMap<UUID, UUID> returnFullMap(){
		return fullListOfRopes;
	}
	
	public Set<Location> returnLocSet(){
		return locSet;
	}
	
	public PacketHandler getPacketHandler() {
		return pkhdlr;
	}
}
