package tld.sima.ropesplugin.events;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.PacketPlayOutAttachEntity;
import tld.sima.ropesplugin.Main;
import tld.sima.ropesplugin.custominventory.CustomInventory;

public class EventsManager implements Listener {
	
	Main plugin = Main.getPlugin(Main.class);
	Set<UUID> delay = new HashSet<UUID>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getPacketHandler().listen(event.getPlayer());
    }
    
	@EventHandler
	public void onLogout(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		plugin.returnInventoryMap().remove(uuid);
		if (plugin.returnRopeMap().containsKey(uuid) && (Bukkit.getEntity(plugin.returnRopeMap().get(uuid)) != null)) {
			Bukkit.getEntity(plugin.returnRopeMap().get(uuid)).remove();
		}
		plugin.returnRopeMap().remove(uuid); 
        plugin.getPacketHandler().silence(event.getPlayer());
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Vex) {
			Vex vex = (Vex) event.getEntity();
			if (plugin.returnFullMap().containsKey(vex.getUniqueId()) || plugin.returnFullMap().containsValue(vex.getUniqueId())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Vex) {
			Vex vex = (Vex) event.getEntity();
			if (plugin.returnFullMap().containsKey(vex.getUniqueId()) || plugin.returnFullMap().containsValue(vex.getUniqueId())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityFire(EntityCombustEvent event) {
		if (event.getEntity() instanceof Vex) {
			Vex vex = (Vex) event.getEntity();
			if (plugin.returnFullMap().containsKey(vex.getUniqueId()) || plugin.returnFullMap().containsValue(vex.getUniqueId())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Vex) {
			Vex vex = (Vex) event.getRightClicked();
			if (plugin.returnFullMap().containsKey(vex.getUniqueId()) || plugin.returnFullMap().containsValue(vex.getUniqueId())) {
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
				}
				String name = item.getItemMeta().getDisplayName();
				if (name.contains("Move Entity")){
					player.sendMessage("ping");
					player.closeInventory();
					ConversationFactory cf = new ConversationFactory(plugin);
					MoveConversation posconv = new MoveConversation();
					posconv.importData((Vex) Bukkit.getEntity(plugin.returnInventoryMap().get(player.getUniqueId())), player);
					Conversation conv = cf.withFirstPrompt(posconv).withLocalEcho(true).buildConversation(player);
					conv.begin();
				}else if (name.equals(ChatColor.RED + "Remove Entity")) {
					player.closeInventory();
					Vex vex = (Vex) Bukkit.getEntity(plugin.returnInventoryMap().get(player.getUniqueId()));
					Vex otherVex;
					if (plugin.returnFullMap().containsKey(vex.getUniqueId())) {
						otherVex = (Vex) Bukkit.getEntity(plugin.returnFullMap().get(vex.getUniqueId()));
						plugin.returnFullMap().remove(vex.getUniqueId());
					}else {
						UUID vexUUID = vex.getUniqueId();
						UUID otherVexUUID = null;
						for (Entry<UUID, UUID> uuid : plugin.returnFullMap().entrySet()) {
							if (Objects.equals(vexUUID, uuid.getValue())) {
								otherVexUUID = uuid.getKey();
							}
						}
						plugin.returnFullMap().remove(otherVexUUID);
						if (otherVexUUID != null) {
							otherVex = (Vex) Bukkit.getEntity(otherVexUUID);
						}else {
							player.sendMessage(ChatColor.RED + "Something weird is going on...");
							vex.remove();
							return;
						}
					}
					vex.remove();
					otherVex.remove();
				}else {
					player.sendMessage("pong");
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
				Location loc = null;
				try {
					loc = event.getClickedBlock().getLocation().clone();
				} catch(NullPointerException e){
					player.sendMessage(ChatColor.RED + "Something went wrong trying to find the block you right clicked");
					return;
				}
				loc.add(0.5, 1, 0.5);
				Vex vex = spawnVex(loc);
				
				if (plugin.returnRopeMap().containsKey(player.getUniqueId()) && (Bukkit.getEntity(plugin.returnRopeMap().get(player.getUniqueId()))!= null)) {
					Vex parentVex = (Vex) Bukkit.getEntity(plugin.returnRopeMap().get(player.getUniqueId()));
					
					player.sendMessage(ChatColor.GOLD + "Rope created!");
					
					Entity vexparent = ((CraftEntity) parentVex).getHandle();
					Entity vexchild = ((CraftEntity) vex).getHandle();
					double radius = 64;
					PacketPlayOutAttachEntity ppoae = new PacketPlayOutAttachEntity(vexchild, vexparent);
					for (org.bukkit.entity.Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
						if (e instanceof Player) {
							((CraftPlayer) e).getHandle().playerConnection.sendPacket(ppoae);
						}
					}
					plugin.returnFullMap().put(parentVex.getUniqueId(), vex.getUniqueId());
					
					plugin.returnRopeMap().remove(player.getUniqueId());
				}else {
					player.sendMessage(ChatColor.GOLD + "First entity for the rope placed!");
					plugin.returnRopeMap().put(player.getUniqueId(), vex.getUniqueId());
				}
				new BukkitRunnable() {
					public void run() {
						delay.remove(player.getUniqueId());
					}
				}.runTaskLater(this.plugin, 5);
			}
		}
	}

	private Vex spawnVex(Location loc) {
		Vex vex = (Vex) loc.getWorld().spawnEntity(loc, EntityType.VEX);
		vex.setAI(false);
		vex.setCustomName("Rope");
		vex.setSilent(true);
		PotionEffect invisible = new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 1, false, false);
		vex.addPotionEffect(invisible);

		new BukkitRunnable() {
			public void run() {
				vex.getEquipment().setItemInMainHand(null);
				vex.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
			}
		}.runTaskLaterAsynchronously(plugin, 1);
		return vex;
	}
}
