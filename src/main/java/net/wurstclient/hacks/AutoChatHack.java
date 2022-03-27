package net.wurstclient.hacks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.lang.reflect.Type; 
import java.nio.file.Files; 
import java.nio.file.NoSuchFileException; 

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken; 
import com.mojang.authlib.minecraft.client.ObjectMapper;
import com.mojang.datafixers.types.templates.List;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.events.ChatInputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;

@SearchTags({"auto chat"})
public class AutoChatHack extends Hack
	implements ChatInputListener,  UpdateListener{

	private final Path AUTO_CHAT_FILE = WurstClient.INSTANCE.getWurstFolder().resolve("autochat.json");
	private ArrayList<AutoChatSection> chatSections;
	private ArrayList<String> messageQueue = new ArrayList<String>();
	private Random random;
	private int timer; 
	
	public AutoChatHack() {
		super("AutoChatHack");		
		setCategory(Category.CHAT);
		random = new Random();
		chatSections  = new ArrayList<AutoChatSection>();
	}
	
	@Override 
	public void onEnable()  
	{ 
		EVENTS.add(ChatInputListener.class, this); 
		EVENTS.add(UpdateListener.class, this);
		Load();
	} 
 
	@Override 
	public void onDisable()  
	{ 
		EVENTS.remove(ChatInputListener.class, this); 
		EVENTS.remove(UpdateListener.class, this);
		if (chatSections == null) {
			chatSections = new ArrayList<AutoChatSection>();
		}

		Save();
	} 
	
	@Override
	public void onReceivedMessage(ChatInputEvent event) {
		try {
			String message = event.getComponent().getString(); 
			 
			if (message.startsWith(ChatUtils.WURST_PREFIX)) 
				return; 
	 
			for (AutoChatSection chatSection : chatSections)  
			{
				for (String trigger : chatSection.triggers) {
					if (message.contains(trigger))  
					{ 
						messageQueue.add(chatSection.responses.get(random.nextInt(chatSection.responses.size()))); 
					} 
				}
			} 		
		}
		catch (Exception ex) {
			ChatUtils.message(ex.getMessage());
		}
	}

	@Override
	public void onUpdate() {
		if (timer > -1) { 
			timer--; 
			return; 
		}
		
		timer = 15 + random.nextInt(35);
		
		if (messageQueue.isEmpty() ) {
			return;
		}
 
		MC.player.sendChatMessage(messageQueue.get(0)); 
		messageQueue.remove(0); 
	}
	
	private void Load() {
		try {
			Gson gson = new Gson();
			File file = AUTO_CHAT_FILE.toFile();
			
			// create file if not exists
			file.createNewFile();			
			
			FileInputStream stream = new FileInputStream(file);			
			String json = new String(stream.readAllBytes());	
			stream.close();
			
			Type autoChatSectionListType = new TypeToken<ArrayList<AutoChatSection>>() {}.getType();
			chatSections = gson.fromJson(json, autoChatSectionListType);							
		} 
		catch (Exception ex) {
			ChatUtils.message("Error reading autochat file : " + ex.getMessage());			
		}		
	}
	
	private void Save() {
		try {
			Gson gson = new Gson();
			File file = AUTO_CHAT_FILE.toFile();
			
			// create file if not exists
			file.createNewFile();
			
			String json = gson.toJson(chatSections);				
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(json.getBytes());
			stream.close();
		}
		catch (Exception ex) {
			ChatUtils.message("Error saving to autochat file: " + ex.getMessage());
		}
	}
	
	public void AddSection(String sectionName) { 		 
		if (GetSectionIndex(sectionName) == -1) { 
			chatSections.add(new AutoChatSection(sectionName)); 
		} 
		Save(); 
	} 
 
	public void AddTrigger(String sectionName, String trigger) throws Exception { 
		if (GetSectionIndex(sectionName) > -1) { 
			chatSections.get(GetSectionIndex(sectionName)).triggers.add(trigger); 
		}
		else {
			throw new Exception("Section not found");
		}
		Save();
	} 
 
	public void AddResponse(String sectionName, String response) throws Exception  
	{ 
		if (GetSectionIndex(sectionName) > -1) { 
			chatSections.get(GetSectionIndex(sectionName)).responses.add(response); 
		} 
		else {
			throw new Exception("Section not found");
		}
		Save();
	} 
	
	public void DeleteSection(String sectionName) throws Exception {
		if (GetSectionIndex(sectionName) > -1) { 
			chatSections.remove(chatSections.get(GetSectionIndex(sectionName)));
		}
		else {
			throw new Exception("Section not found");
		}
		Save();
	}
	
	public void DeleteTriggers(String sectionName) throws Exception {
		if (GetSectionIndex(sectionName) > -1) { 
			chatSections.get(GetSectionIndex(sectionName)).triggers.clear(); 
		}
		else {
			throw new Exception("Section not found");
		}
		Save();
	}
	
	public void DeleteResponses(String sectionName) throws Exception {
		if (GetSectionIndex(sectionName) > -1) { 
			chatSections.get(GetSectionIndex(sectionName)).responses.clear(); 
		}
		else {
			throw new Exception("Section not found");
		}
		Save();
	}
	 
	public int GetSectionIndex(String sectionName) { 
		int index = -1; 
		for (AutoChatSection chatSection : chatSections) { 
			if (chatSection.name.equals(sectionName)) { 
				return chatSections.indexOf(chatSection); 
			} 
		} 
		return index; 
	}
	
	public ArrayList<AutoChatSection> GetChatSections() {
		return this.chatSections;
	}	

	public class AutoChatSection {
		
		public AutoChatSection() {
			this.name = "";
			triggers = new ArrayList<String>();
			responses = new ArrayList<String>();
		}
		
		public AutoChatSection(String name) {
			this.name = name;
			triggers = new ArrayList<String>();
			responses = new ArrayList<String>();
		}
		
		public String name;
		public ArrayList<String> triggers;
		public ArrayList<String> responses;
	}
}
