package io.github.noonc.admininventory;


import io.github.noonc.admininventory.InventorySerialization;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class admininventory extends JavaPlugin 
{
	String username, password, host, port, database, url;
	static Connection connection;
	@Override
	public void onEnable() 
	{
		getLogger().info("onEnable has been invoked");
		this.getConfig();
		//FileConfiguration config = this.getConfig();
		//config.addDefault("host", "127.0.0.1");
		//config.addDefault("port", "3306");
		//config.addDefault("database", "admininventory");
		//config.addDefault("user", "root");
		//config.addDefault("pass", "pass");
		//config.addDefault("vanish", false);
		//this.saveDefaultConfig();
		
		
		initSQL();
	}
	@Override
	public void onDisable() 
	{
		getLogger().info("onDisable has been invoked");
		try {
			if (connection!=null && !connection.isClosed()) {
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void initSQL()
	{
		FileConfiguration config = this.getConfig();
		username = config.getString("user");
		password = config.getString("pass");
		host = config.getString("host");
		port = config.getString("port");
		database = config.getString("database");
		url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
		getLogger().info(url);
		getLogger().info(username + ":" + password);
		try {
			connection = DriverManager.getConnection(url, username, password);
			getLogger().info("Connected to the SQL Database");
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		String sql = "CREATE TABLE IF NOT EXISTS survivalInv(placeholder varchar(64));";
		//String sqll = "CREATE TABLE IF NOT EXISTS creativeInv(placeholder varchar(64));";
		
		try {
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.executeUpdate();
			//getLogger().info("Creating Table survivalInv");
		} catch (SQLException e)
		{
			getLogger().info("Failed to Create Table");
			e.printStackTrace();
		}
		//try {
		//	PreparedStatement stmt = connection.prepareStatement(sqll);
		//	stmt.executeUpdate();
		//	getLogger().info("Creating Table creativeInv");
		//} catch (SQLException e)
		//{
		//	getLogger().info("Failed to Create Table");
		//	e.printStackTrace();
		//}
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("adminon")) { 
			if (!(sender instanceof Player)) {
				sender.sendMessage("This is a player command only.");
			} else {
				Player player = (Player) sender;
				player.getGameMode();
				if (!(player.getGameMode() == GameMode.CREATIVE))
				{					
					Inventory inventory = player.getInventory();
					ItemStack[] asd = player.getInventory().getContents();
					InventorySerialization.toBase64(inventory);
					InventorySerialization.itemStackArrayToBase64(asd);
					String convInv = InventorySerialization.getEncode();
					String convItem = InventorySerialization.getIEncode();
					getLogger().info(convInv);
					UUID UUID = player.getUniqueId();
					//initSQL();
					String createCol = "ALTER TABLE survivalInv ADD COLUMN `" + UUID + "` TEXT(10000)";
					boolean suc = false;
					try {
						PreparedStatement stmt = connection.prepareStatement(createCol);
						stmt.executeUpdate(createCol);
						getLogger().info("Creating Column");
						suc = true;
					} catch (SQLException e) {
						e.printStackTrace();
						player.sendMessage(ChatColor.RED + "" +ChatColor.BOLD + "Failed to Save Inventory or an Inventory has already been saved! Do /adminclear to clear current saved inventory!");
						getLogger().info("Failed to Create Column or it already exists");
						suc = false;
						//return false;
					}					
					String sql = "INSERT INTO survivalInv(`"+ UUID +"`) VALUES ('" + convItem + "');";
					boolean success = false;
					try {
						if (suc)
						{
							PreparedStatement stmt = connection.prepareStatement(sql);
							stmt.executeUpdate(sql);
							getLogger().info("Inserting Inv Value");
							success = true;
						}
					} catch (SQLException e) {
						e.printStackTrace();	
						getLogger().info("Failed to Insert Value");
						success = false;
					}
					if (success && suc)
					{
						//player.sendMessage(ChatColor.RED + "Inventory has been saved!");
						player.setGameMode(GameMode.CREATIVE);
						//player.sendMessage(ChatColor.RED + "You gamemode has been changed to Creative!");
						player.sendMessage(ChatColor.AQUA + "You are now in " + ChatColor.RED + ChatColor.BOLD + "ADMIN MODE");
						FileConfiguration config = this.getConfig();
						if (config.getBoolean("vanish")) 
						{
							getLogger().info("Vanish is true");
							Vanish(sender);
						}
						player.getInventory().clear();
					}
					
					
				}
				else
				{
					player.sendMessage(ChatColor.RED + "You are already in Creative! Change to Survival then try again!");
				}
				
				
			}
			return true;
		} 
		if (cmd.getName().equalsIgnoreCase("adminoff")) { 
			if (!(sender instanceof Player)) {
				sender.sendMessage("This is a player command only.");
			} else {
				Player player = (Player) sender;
				if (!(player.getGameMode() == GameMode.SURVIVAL))
				{					
					//player.sendMessage(ChatColor.RED + "You gamemode has been changed to Survival!");
					UUID UUID = player.getUniqueId();
					//initSQL();
					String sql = "SELECT * FROM survivalInv WHERE `" + UUID + "` is not NULL;";
					boolean success = false;
					try {
						PreparedStatement stmt = connection.prepareStatement(sql);
						ResultSet results = stmt.executeQuery();
						String uu = UUID.toString();
						results.next();
						String data = results.getString(uu);
						boolean sc = false;
						ItemStack[] oldItem = null;
						//Inventory oldInv = null;
						getLogger().info("Query was Successfull");
						getLogger().info(data);
						try {
							//InventorySerialization.fromBase64(data);
							//oldInv = InventorySerialization.getInv();
							//getLogger().info(oldInv.toString());
							InventorySerialization.itemStackArrayFromBase64(data);
							oldItem = InventorySerialization.getItems();						
							getLogger().info(oldItem.toString());
							sc = true;
						} catch (IOException e) {
							sc = false;
							e.printStackTrace();
						}
						if (sc)
						{
							//player.openInventory(oldInv);
							//ItemStack[] itemsStacks = oldInv.getContents();
							//Inventory invas = Bukkit.getServer().createInventory(player, 54, "Test");
							//player.getInventory().setContents(itemsStacks);
							//player.updateInventory();
							//player.openInventory(invas);
							player.getInventory().setContents(oldItem);
						}
						success = true;
					} catch (SQLException e) {
						e.printStackTrace();	
						getLogger().info("Query failed");
						player.sendMessage(ChatColor.RED + "No saved inventory available/found, or query failed!");
						success = false;
					}		
					if (success) 
					{
						//player.sendMessage(ChatColor.RED + "Inventory Loaded");	
						player.setGameMode(GameMode.SURVIVAL);
						player.sendMessage(ChatColor.AQUA + "You are now in " + ChatColor.RED + ChatColor.BOLD + "PLAYER MODE");
						FileConfiguration config = this.getConfig();
						if (config.getBoolean("vanish")) 
						{
							getLogger().info("Vanish is true");
							Vanish(sender);
						}
						String dropCol = "ALTER TABLE survivalinv DROP `" + UUID + "`;";
						try {
							PreparedStatement stmt = connection.prepareStatement(dropCol);
							stmt.executeUpdate(dropCol);
							getLogger().info("Deleting Saved Inventory " + UUID);
							
						} catch (SQLException e) {
							e.printStackTrace();	
							getLogger().info("Failed to Delete Saved Inventory");
							
						}		
					}
						
				}
				else
				{
					player.sendMessage(ChatColor.RED + "You are already in Survival! Change to Creative then try again!");
				}
			}
			return true;
		} 
		if (cmd.getName().equalsIgnoreCase("restartn")) { 
			if (sender.hasPermission("admininventory.restart"))
			{
				Bukkit.broadcastMessage(ChatColor.RED + "[" + ChatColor.GRAY + "Server" + ChatColor.RED + "]" + ChatColor.AQUA + "SERVER RESTARTING IN 10 SECONDS!");
				Bukkit.broadcastMessage(ChatColor.RED + "[" + ChatColor.GRAY + "Server" + ChatColor.RED + "]" + ChatColor.AQUA + "SERVER RESTARTING IN 10 SECONDS!");
				Bukkit.broadcastMessage(ChatColor.RED + "[" + ChatColor.GRAY + "Server" + ChatColor.RED + "]" + ChatColor.AQUA + "SERVER RESTARTING IN 10 SECONDS!");
				boolean success = false;
				try {
					Thread.sleep(10000);
					success = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
					success = false;
				}
				if (success) {
					Bukkit.broadcastMessage(ChatColor.RED + "[" + ChatColor.GRAY + "Server" + ChatColor.RED + "]" + ChatColor.AQUA + "SHUTTING DOWN");
					Bukkit.getServer().shutdown();
					String reason = "§4Server Restarting";
					if (StringUtils.isNotEmpty(reason)) {
					  for (Player player : Bukkit.getOnlinePlayers()) {
					    player.kickPlayer(reason);
					  }
					}
				}
			}
			return true;
		} 
		if (cmd.getName().equalsIgnoreCase("vanishn"))
		{
			FileConfiguration config = this.getConfig();
			Player player = (Player) sender;
			if (player.hasPermission("admininventory.vanish") && config.getBoolean("vanish")) 
			{
				Vanish(sender);
			} 
			else
			{
				if (!(player.hasPermission("admininventory.vanish")))
				{
					player.sendMessage(ChatColor.RED + "You do not have permission to do this.");
				} else if (!(config.getBoolean("vanish")))
				{
					player.sendMessage(ChatColor.RED + "Vanish is not enabled!");
				}

			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("adminreload"))
		{
			this.reloadConfig();
			getLogger().info("Reloaded Config!");
			Player player = (Player) sender;
			if(sender instanceof Player && player.hasPermission("admininventory.reload"))
			{
				player.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + "AdminInventory" + ChatColor.GRAY + "]" + ChatColor.RED + " Reloaded Config!");
				
			}
			return true;
			
		}
		if (cmd.getName().equalsIgnoreCase("admininventory"))
		{
			Player player = (Player) sender;
			if (sender instanceof Player)
			{
				player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "===============[Admin Inventory]===============");
				player.sendMessage(ChatColor.RED + "/adminon - Enables admin mode");
				player.sendMessage(ChatColor.RED + "/adminoff - Disables admin mode");
				player.sendMessage(ChatColor.RED + "/restartn - Stops Server (See Config)");
				player.sendMessage(ChatColor.RED + "/vanishn - Enables/Disables Vanish");
				player.sendMessage(ChatColor.RED + "/adminreload - Reloads the config");
				player.sendMessage(ChatColor.RED + "/admininventory - This Command");
				player.sendMessage(ChatColor.RED + "/adminvanish on/off - Enables or Disables Vanish Option");
				player.sendMessage(ChatColor.RED + "/adminclear - Clears Saved Inventory");
			}
			
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("adminvanish"))
		{
			FileConfiguration config = this.getConfig();
			Player player = (Player) sender;
			if (args.length == 0)
			{
				player.sendMessage(ChatColor.RED + "Incorrect Usage. /adminvanish on/off");
				return true;
			}
			if (sender instanceof Player)
			{
				if (args[0].equalsIgnoreCase("on") && player.hasPermission("admininventory.vanishset"))
				{
					config.set("vanish", true);
					saveConfig();
					this.reloadConfig();
					player.sendMessage(ChatColor.RED + "Turned on Vanish!");
					return true;
				}else if (args[0].equalsIgnoreCase("off") && player.hasPermission("admininventory.vanishset"))
				{
					config.set("vanish", false);
					saveConfig();
					this.reloadConfig();
					player.sendMessage(ChatColor.RED + "Turned off Vanish!");
					return true;
				}
				else
				{
					player.sendMessage("Incorrect Arguments");
					return false;
				}
			}
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("adminclear"))
		{
			Player player = (Player) sender;
			String UUID = player.getUniqueId().toString();
			if (args.length == 0)
			{
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Are you sure you want to delete your current saved inventory? THIS IS NOT REVERSIBLE! Type the command again with yes/no to confirm.");
				return true;
			}
			if (!(args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("no")))
			{
				player.sendMessage(ChatColor.RED + "Wrong Usage");
				return true;
			}
			Boolean cmdd = false;
			try 
			{
				if (args[0].equalsIgnoreCase("yes"))
				{
					cmdd = true;
					getLogger().info(player.getName() + " sent the command: Adminclear YES");
				}
				else if (args[0].equalsIgnoreCase("no"))
				{
					cmdd = false;
					getLogger().info(player.getName() + " sent the command: Adminclear NO");
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				//getLogger().info("Finally, before the if");
				//getLogger().info(UUID);
				//getLogger().info(player.getUniqueId().toString());
				if (cmdd && UUID.equals(player.getUniqueId().toString()))
				{
					String sql = "SELECT * FROM survivalInv WHERE `" + UUID + "` is not NULL;";
					try {
						PreparedStatement stmt = connection.prepareStatement(sql);
						ResultSet results = stmt.executeQuery();
						String uu = UUID.toString();
						results.next();
						String data = results.getString(uu);
					}
					catch (SQLException e)
					{
						player.sendMessage(ChatColor.RED + "SQL Query Failed! Assuming no saved inventory was found!");
					}
					getLogger().info("Clearing Inventory Save!");
					String dropCol = "ALTER TABLE survivalinv DROP `" + UUID + "`;";
					try {
						PreparedStatement stmt = connection.prepareStatement(dropCol);
						stmt.executeUpdate(dropCol);
						getLogger().info("Deleting Saved Inventory " + UUID);
						player.sendMessage(ChatColor.RED + "Deleted Saved Inventory!");
						cmdd = false;
						
					} catch (SQLException e) {
						e.printStackTrace();	
						getLogger().info("Failed to Delete Saved Inventory");
						player.sendMessage(ChatColor.RED + "Failed to Delete Saved Inventory");
						
					}		
				}
				else
				{
					//getLogger().info("Failed the if, UUID?");
					getLogger().info(player.getName() + " player did not say YES to adminclear");
					player.sendMessage(ChatColor.RED + "You chose NOT to clear your saved inventory");
				}
			}
			return true;
		}

		return false; 
	}
	
	public void Vanish(CommandSender sender) 
	{
		Player player = (Player) sender;
		Boolean sucess = player.performCommand("sv");
		if (!(sucess))
		{
			getLogger().info(player.getName() + " tried to vanish. Failed to perform command. Must have SuperVanish installed to work!");
			player.sendMessage(ChatColor.RED + "Failed to Vanish, contact an Administrator");
		}
				
	}

}
