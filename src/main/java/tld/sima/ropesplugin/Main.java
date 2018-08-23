package tld.sima.ropesplugin;

import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
	
	@Override
	public void onEnable() {
		this.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Ropes Plugin is alive");
	}
	
	@Override
	public void onDisable() {
		
	}
	
}
