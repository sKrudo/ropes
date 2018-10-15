package tld.sima.ropesplugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class StorageManager {
	private Main plugin = Main.getPlugin(Main.class);

	private FileConfiguration storagecfg;
	private File storagefile;
	
	public void setup() {
		// Create plugin folder if doesn't exist.
		if(!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdir();
		}

		// Create main file
		String FileLocation = plugin.getDataFolder().toString() + File.separator + "Storage" + ".yml";
		File tmp = new File(FileLocation);
		storagefile = tmp;
		// Check if file exists
		if (!storagefile.exists()) {
			try {
				storagefile.createNewFile();
			}catch(IOException e) {
				e.printStackTrace();
				Bukkit.getServer().getConsoleSender().sendMessage(net.md_5.bungee.api.ChatColor.RED + "Storage file unable to be created!");
			}
		}
		storagecfg = YamlConfiguration.loadConfiguration(storagefile);
		createStorageValues();
	}
	
	private void createStorageValues() {
		storagecfg.addDefault("Rope.Parent.IDs", new ArrayList<String>());
		storagecfg.options().copyDefaults(true);
		savecfg();
	}

	private boolean savecfg() {
		try {
			storagecfg.save(storagefile);
		} catch (IOException e) {
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Unable to save storage file!" );
			return false;
		}
		return true;
	}
	
	public void finalSave(HashMap<UUID, UUID> list) {
		// Get List of UUIDS from HashMap and convert to String
		List<String> strings = new ArrayList<String>();
		for (UUID uuid : list.keySet()) {
			if (!(Bukkit.getServer().getEntity(uuid) == null)) {
				strings.add(uuid.toString());
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "Radius of " + strings.get(strings.size()-1) + ": " + ChatColor.WHITE + list.get(uuid));
			}else {
				Bukkit.getServer().getConsoleSender().sendMessage("Entity not found! Removed!");
			}
		}

		// Get list from file and duplicate to ensure I don't remove current item by accident
		List<String> oldStrings = new ArrayList<String>(storagecfg.getStringList("Rope.Parent.IDs"));

		// Update initial list with the updated list of uuids that actually exist
		storagecfg.set("Rope.Parent.IDs", new ArrayList<String>(strings));

		// Ensure that whatever is on the file actually exists on the server. Remove if it isn't.
		for (String string : oldStrings) {
			if (!strings.contains(string)) {
				storagecfg.set("Rope." + string, null);
			}else {
				strings.remove(string);
			}
		}

		for (String string : strings) {
			if(!oldStrings.contains(string)) {
				storagecfg.set("Rope." + string + ".child", list.get(UUID.fromString(string)).toString());
			}
		}
		savecfg();
	}

	public void removeUUID(UUID uuid) {
		List<String> list = storagecfg.getStringList("Rope.Parent.IDs");
		if (list.contains(uuid.toString())) {
			list.remove(uuid.toString());
			storagecfg.set("Rope.Parent.IDs", list);
			storagecfg.set("Rope." + uuid.toString(), null);
			savecfg();
		}
	}

	public void inputUUID(UUID parentuuid, UUID childuuid) {
		List<String> list = storagecfg.getStringList("Rope.Parent.IDs");
		list.add(parentuuid.toString());
		storagecfg.set("Rope.Parent.IDs", list);
		storagecfg.set("Rope."+parentuuid.toString()+".child", childuuid.toString());
		
		savecfg();
	}

	public HashMap<UUID, UUID> getList(){
		List<String> list = storagecfg.getStringList("Rope.Parent.IDs");
		HashMap<UUID, UUID> returnList = new HashMap<UUID, UUID>();
		UUID child;
		for (String string : list) {
			child = UUID.fromString(storagecfg.getString("Rope." + string + ".child"));
			if (Bukkit.getEntity(child) == null) {
				continue;
			}
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "Child of " + string + ": " + ChatColor.WHITE + child.toString());
			returnList.put(UUID.fromString(string), child);
		}
		return returnList;
	}
}
