package net.wurstclient.commands;

import net.minecraft.item.Item; 
import net.minecraft.item.ItemStack; 
import net.minecraft.item.Items; 
import net.minecraft.nbt.NbtString; 
import net.wurstclient.command.CmdError; 
import net.wurstclient.command.CmdException; 
import net.wurstclient.command.CmdSyntaxError; 
import net.wurstclient.command.Command; 
import net.wurstclient.hacks.AutoChatHack.AutoChatSection; 
import net.wurstclient.util.ChatUtils; 
 
public class AutoChatCmd extends Command {

	public AutoChatCmd() { 
		super("autochat", "automatic chat", 
				".autochat <add/delete> section <sectionname>", 
				".autochat add trigger <sectionname> <trigger>", 
				".autochat add response <sectionname> <response>", 
				".autochat sections", 
				".autochat show <section>" 
				);				 
	} 
	
	@Override 
	public void call(String[] args) throws CmdException 
	{ 
		if (args.length < 3) {
			throw new CmdSyntaxError();
		}
			
		switch(args[0]) {
		
		case "add":
			Add(args);
			break;
		case "delete":
			Delete(args);
			break;
		case "display":
			Display();
			break;
		default:
			throw new CmdSyntaxError();				
		}
	} 
	
	private void Add(String[] args) {
		try {
			switch (args[1]) {
			case "section":
				WURST.getHax().autoChatHack.AddSection(args[2]);
				break;
				
			case "trigger":
				if (args.length < 4) {
					return;
				}
				WURST.getHax().autoChatHack.AddTrigger(args[2], args[3]);
				break;
				
			case "response":
				if (args.length < 4) {
					return;
				}
				WURST.getHax().autoChatHack.AddResponse(args[2],  args[3]);
				break;
			default:
				return;
			}
		}
		catch (Exception ex) {
			ChatUtils.message(ex.getMessage());
		}
	}
	
	private void Delete(String[] args) {
		try {
			switch(args[1]) {
			case "section":
				WURST.getHax().autoChatHack.DeleteSection(args[2]);
				break;
				
			case "triggers":
				WURST.getHax().autoChatHack.DeleteTriggers(args[2]);
				break;
				
			case "responses":
				WURST.getHax().autoChatHack.DeleteResponses(args[2]);
				break;
			default:
				return;
			}			
		}
		catch (Exception ex) {
			ChatUtils.message(ex.getMessage());
		}
	}	
	
	private void Display() {
		String message = "";
		for (AutoChatSection chatSection : WURST.getHax().autoChatHack.GetChatSections()) {
			message += chatSection.name + ", ";
		}
		ChatUtils.message(message);
	}
}
