package tld.sima.ropesplugin.events;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;
import tld.sima.ropesplugin.Main;
import tld.sima.ropesplugin.custominventory.CustomInventory;

public class EventsManager implements Listener {
	
	Main plugin = Main.getPlugin(Main.class);
	Set<UUID> delay = new HashSet<UUID>();
	
	@EventHandler
	public void onLogout(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		plugin.returnInventoryMap().remove(uuid);
		plugin.returnRopeMap().remove(uuid); 
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Vex) {
			Vex vex = (Vex) event.getEntity();
			if ((vex.getCustomName() != null) && vex.getCustomName().equals("Rope") && !vex.hasAI()) {
				if (event.getDamager() instanceof Player) {
					Player player = (Player)event.getDamager();
					if (player.getInventory().getItemInMainHand().equals(new ItemStack(Material.POISONOUS_POTATO))) {
						vex.remove();
						return;
					}
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Vex) {
			Vex vex = (Vex) event.getEntity();
			if ((vex.getCustomName() != null) && vex.getCustomName().equals("Rope") && !vex.hasAI()) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityFire(EntityCombustEvent event) {
		if (event.getEntity() instanceof Vex) {
			Vex vex = (Vex) event.getEntity();
			if ((vex.getCustomName() != null) && vex.getCustomName().equals("Rope") && !vex.hasAI()) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Vex) {
			Vex vex = (Vex) event.getRightClicked();
			if ((vex.getCustomName() != null) && vex.getCustomName().equals("Rope") && !vex.hasAI()) {
				CustomInventory i = new CustomInventory();
				i.createInventory(event.getPlayer(), vex);
				
				plugin.returnInventoryMap().put(event.getPlayer().getUniqueId(), vex.getUniqueId());
			}
		}
	}
	
	@EventHandler
	public void inventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			String mainMenu = (ChatColor.DARK_BLUE + "Rope Entity Menu");
			
			Inventory open;
			ItemStack item;
			try {
				open = event.getClickedInventory();
				item = event.getCurrentItem();
			} catch (NullPointerException e) {
				return;
			}
			Player player = (Player) event.getWhoClicked();
			try {
				if (open.getName() == null || event == null) {
					return;
				}
			}catch (NullPointerException e) {
				return;
			}
			
			if (open.getName().equals(mainMenu)) {
				event.setCancelled(true);
				if((item.equals(null)) || (item == null)){
					player.sendMessage("Clicked something null!");
					return;
				}else if ( !item.hasItemMeta()){
					player.sendMessage("Clicked something without meta!");
					return;
				}else if (item.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Move entity")){
					player.closeInventory();
					ConversationFactory cf = new ConversationFactory(plugin);
					MoveConversation posconv = new MoveConversation();
					posconv.importData((Vex) Bukkit.getEntity(plugin.returnInventoryMap().get(player.getUniqueId())), player);
					Conversation conv = cf.withFirstPrompt(posconv).withLocalEcho(true).buildConversation(player);
					conv.begin();
				}else if (item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Remove Entity")) {
					player.closeInventory();
					Vex vex = (Vex) Bukkit.getEntity(plugin.returnInventoryMap().get(player.getUniqueId()));
					Vex otherVex;
					if (vex.isLeashed()) {
						otherVex = (Vex) vex.getLeashHolder();
						otherVex.remove();
					}else {
						List<Entity> list = vex.getNearbyEntities(50,50,50);
						for (Entity entity: list) {
							if (entity instanceof Vex) {
								Vex oldVex = (Vex) entity;
								if (oldVex.isLeashed()) {
									if (oldVex.getLeashHolder().getUniqueId().equals(vex.getUniqueId())) {
										oldVex.remove();
										break;
									}
								}
							}
						}
					}
					vex.remove();
				}
			
			}
		}
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		ItemStack tool = new ItemStack(Material.STICK);
		ItemMeta toolMeta = tool.getItemMeta();
		toolMeta.setDisplayName(ChatColor.GREEN + "Rope Placer");
		tool.setItemMeta(toolMeta);
		
		if (player.getInventory().getItemInMainHand().isSimilar(tool)) {
			if (!delay.contains(player.getUniqueId())) {
				delay.add(player.getUniqueId());
				
				if (!plugin.returnRopeMap().containsKey(player.getUniqueId())) {
					player.sendMessage(ChatColor.GOLD + "First entity placed");
					Location loc = null;
					try {
						loc = event.getClickedBlock().getLocation().clone();
					} catch(NullPointerException e){
						player.sendMessage(ChatColor.RED + "Something went wrong trying to find the block you right clicked");
						return;
					}
					
					loc.add(0.5, 1, 0.5);
					Vex vex = (Vex) loc.getWorld().spawnEntity(loc, EntityType.VEX);
					vex.setAI(false);
					vex.setInvulnerable(true);
					vex.setCustomName("Rope");
					vex.setSilent(true);
					vex.getEquipment().setItemInMainHand(new ItemStack(Material.STICK));
					PotionEffect invisible = new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 1, false, false);
					vex.addPotionEffect(invisible);
					
					plugin.returnRopeMap().put(player.getUniqueId(), vex.getUniqueId());
				}else {

					Vex oldVex = (Vex) Bukkit.getEntity(plugin.returnRopeMap().get(player.getUniqueId()));
					if (oldVex != null) {
						Location loc = null;
						try {
							loc = event.getClickedBlock().getLocation().clone();
						} catch(NullPointerException e){
							player.sendMessage(ChatColor.RED + "Something went wrong trying to find the block you right clicked");
							return;
						}
						
						loc.add(0.5, 1, 0.5);
						Vex vex = (Vex) loc.getWorld().spawnEntity(loc, EntityType.VEX);
						vex.setAI(false);
						vex.setInvulnerable(true);
						vex.setCustomName("Rope");
						vex.setSilent(true);
						vex.getEquipment().setItemInMainHand(new ItemStack(Material.STICK));
						vex.getEquipment().setItemInMainHand(null);
						PotionEffect invisible = new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 1, false, false);
						vex.addPotionEffect(invisible);
	
						player.sendMessage(ChatColor.GOLD + "Second entity placed");
						vex.setLeashHolder(oldVex);
						
						plugin.returnRopeMap().remove(player.getUniqueId());
					}else {
						player.sendMessage("Something happened to the first source. Cancelling.");
						plugin.returnRopeMap().remove(player.getUniqueId());
					}
				}
				new BukkitRunnable() {
					public void run() {
						delay.remove(player.getUniqueId());
					}
				}.runTaskLater(this.plugin, 5);
			}
		}
	}
}
