package tld.sima.ropesplugin.events;

import org.bukkit.Location;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;

import net.md_5.bungee.api.ChatColor;
import tld.sima.ropesplugin.custominventory.CustomInventory;

public class MoveConversation extends StringPrompt {
	
	Vex vex;
	Player player;
	
	public Prompt acceptInput(ConversationContext con, String message) {
		String delims = "[ ]";
		String[] tokens = message.split(delims);
		CustomInventory i = new CustomInventory();
		if (tokens.length != 3) {
			player.sendMessage("Input the wrong number of elements.");
			i.createInventory(player, vex);
			return null;
		}
		double[] offset = new double[3];
		for (int j = 0; j < tokens.length; j++) {
			try {
				offset[j] = Double.parseDouble(tokens[j]);
			}catch(NullPointerException e) {
				offset[j] = 0;
			}catch(NumberFormatException e) {
				player.sendMessage("Input something that wasn't a number.");
				i.createInventory(player, vex);
				return null;
			}
		}
		Location loc = vex.getLocation().clone();
		loc.add(offset[0], offset[1], offset[2]);
		vex.teleport(loc);
		
		i.createInventory(player, vex);
		
		return null;
	}
	
	public void importData(Vex vexin, Player playerin) {
		vex = vexin;
		player = playerin;
	}

	public String getPromptText(ConversationContext arg0) {
		String message = ChatColor.GOLD + "Type by how much you want to move the entity by their " + ChatColor.WHITE + "x y z" + ChatColor.GOLD + " coordinates. Eg:" + ChatColor.WHITE + "0 1 0";
		return message;
	}
}
