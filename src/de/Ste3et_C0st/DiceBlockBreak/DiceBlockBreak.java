package de.Ste3et_C0st.DiceBlockBreak;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

public class DiceBlockBreak extends JavaPlugin{

	private static DiceBlockBreak instance;
	
	public void onEnable() {
		instance = this;
		Bukkit.getPluginManager().registerEvents(new BlockBreakEvent(), getInstance());
	}
	
	public void onDisable() {
		instance = null;
		ProtocolLibrary.getProtocolManager().removePacketListeners(this);
	}
	
	public static DiceBlockBreak getInstance() {
		return instance;
	}
}
