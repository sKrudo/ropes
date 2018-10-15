package tld.sima.ropesplugin.events;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_13_R2.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_13_R2.PacketPlayOutSpawnEntityLiving;
import tld.sima.ropesplugin.Main;

public class PacketHandler {
	
	private Main plugin = Main.getPlugin(Main.class);
	
	public void listen(Player player) {
		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
			@Override
			public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
//				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Packet Read: " + ChatColor.GOLD + packet.toString());if (plugin.returnSignUUID().contains(player.getUniqueId())&& (packet instanceof PacketPlayInUpdateSign)) {
				super.channelRead(channelHandlerContext, packet);
			}

			@Override
			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				
				if (packet instanceof PacketPlayOutSpawnEntity) {
					PacketPlayOutSpawnEntity ppoe = (PacketPlayOutSpawnEntity) packet;
					
					Field field = ppoe.getClass().getDeclaredField("b");
					field.setAccessible(true);
					Object value = field.get(ppoe);
					UUID uuid = (UUID) value;
					field.setAccessible(false);
					
//					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Packet Read: " + ChatColor.DARK_RED + packet.toString());
					
					if (plugin.returnFullMap().containsKey(uuid)) {
//						Bukkit.getConsoleSender().sendMessage("Ping?");
						UUID uuidchild = plugin.returnFullMap().get(uuid);
						Entity vex = ((CraftEntity) Bukkit.getEntity(uuid)).getHandle();
						Entity vexchild = ((CraftEntity) Bukkit.getEntity(uuidchild)).getHandle();
						PacketPlayOutAttachEntity ppoae = new PacketPlayOutAttachEntity(vex, vexchild);
						CraftPlayer p = (CraftPlayer) player;
						
						p.getHandle().playerConnection.sendPacket(ppoae);
					}
				}else if (packet instanceof PacketPlayOutSpawnEntityLiving) {
					PacketPlayOutSpawnEntityLiving pposel = (PacketPlayOutSpawnEntityLiving) packet;
					
					Field field = pposel.getClass().getDeclaredField("b");
					field.setAccessible(true);
					Object value = field.get(pposel);
					UUID uuid = (UUID) value;
					
//					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Packet Read: " + ChatColor.DARK_RED + packet.toString());
					
					if (plugin.returnFullMap().containsKey(uuid)) {
//						Bukkit.getConsoleSender().sendMessage("Ping?");
						UUID uuidchild = plugin.returnFullMap().get(uuid);
						Entity vex = ((CraftEntity) Bukkit.getEntity(uuid)).getHandle();
						Entity vexchild = ((CraftEntity) Bukkit.getEntity(uuidchild)).getHandle();

						super.write(context, packet, channelPromise); 
						new BukkitRunnable() {
							public void run() {
								PacketPlayOutAttachEntity ppoae = new PacketPlayOutAttachEntity(vexchild, vex);
								((CraftPlayer) player).getHandle().playerConnection.sendPacket(ppoae);
							}
						}.runTaskLater(plugin, 20);
						return;
					}
				}
				super.write(context, packet, channelPromise); 
			}
		};
		ChannelPipeline channelPipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
		channelPipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
    }

    public void silence(Player player) {
    	final Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
    	channel.eventLoop().submit(() -> {
    		channel.pipeline().remove(player.getName());
    		return null;
        });
    }
}
