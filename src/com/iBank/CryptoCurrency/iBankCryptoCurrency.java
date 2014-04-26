package com.iBank.CryptoCurrency;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Timer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.azazar.bitcoin.jsonrpcclient.BitcoinJSONRPCClient;
import com.iBank.iBank;
import com.iBank.CryptoCurrency.Commands.CommandHelp;
import com.iBank.CryptoCurrency.Commands.RootCommand;
import com.iBank.CryptoCurrency.Database.DataSource;
import com.iBank.CryptoCurrency.Database.DataSource.Drivers;
import com.iBank.CryptoCurrency.system.Configuration;
import com.iBank.system.CommandHandler;
import com.iBank.utils.StreamUtils;

/**
 * CryptoCurrency Plugin for iBank
 * @author steffengy
 * @copyright Copyright steffengy (C) 2014
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class iBankCryptoCurrency extends JavaPlugin 
{
	public static iBankCryptoCurrency mainInstance = null;
    private static YamlConfiguration StringConfig = null;
    private static YamlConfiguration Config = null;
    private static File ConfigFile = null;
    private static File StringFile = null;
    public static PluginDescriptionFile description = null;
    public static DataSource data = new DataSource();
    public static BitcoinJSONRPCClient client = null;
    private Timer Syncer = null;
    
	@Override
	public void onEnable() {
		//dirty hack :(
		mainInstance = this;
		if(!(getDataFolder().exists())) getDataFolder().mkdir();
		// Load configuration + strings
		reloadConfig();
		loadStrings();
		Configuration.init(Config);
		Configuration.stringinit(StringConfig);

		if(!Configuration.Entry.Enabled.getBoolean()) 
		{
			System.out.println("[iBank-CryptoCurrency] Disabled as configured in configuration");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
	    //register commands
		CommandHandler.register(new RootCommand());
		CommandHandler.register(new CommandHelp("cryptoc"));
	    //register loan help
		description = this.getDescription();  
		  
		//DB
		if(Configuration.Entry.DatabaseType.getValue().equalsIgnoreCase("sqlite") || Configuration.Entry.DatabaseType.getValue().equalsIgnoreCase("mysql")) 
		{
			if(Configuration.Entry.DatabaseUrl.getValue() != null) 
			{
				// connect
				Drivers driver = DataSource.Drivers.SQLite;
				if(Configuration.Entry.DatabaseType.getValue().equalsIgnoreCase("mysql")) {
					driver = DataSource.Drivers.MYSQL;
				}
				
				if(!DataSource.setup(driver, Configuration.Entry.DatabaseUrl.getValue(), this)) {
					System.out.println("[iBank] Database connection failed! Shuting down iBank...");
					getServer().getPluginManager().disablePlugin(this);
					return;
				}
			}
			else
			{
				System.out.println("[iBank] Database connection failed! No File specified!");
			}
		}
		else
		{
			if(Configuration.Entry.DatabaseUrl.getValue().toString() != null) {
			// connect
			if(!DataSource.setup(DataSource.Drivers.SQLite, Configuration.Entry.DatabaseUrl.getValue(), this)) 
			{
				System.out.println("[iBank] Database connection failed! Shuting down iBank...");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			}
			else
			{
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
		}
	    
		//Connect to specificed coin server
		try {
			client = new BitcoinJSONRPCClient("http://" + 
					Configuration.Entry.CoinLoginUser.getValue() + 
					":" + 
					Configuration.Entry.CoinLoginPassword.getValue() +
					"@" +
					Configuration.Entry.CoinServer.getValue() + ":" +
					Configuration.Entry.CoinPort.getInteger() + "");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				long time = 60L * 1000L;
				Syncer = new Timer();
				Syncer.scheduleAtFixedRate(new Syncer(), time, time);
			}
		}).start();
        
		System.out.println("[iBank-CC] Version " + description.getVersion() + " loaded successfully!");
	}
	
	@Override
	public void onDisable() 
	{
		DataSource.shutdown();
		//Kill timers
		if(Syncer != null) 
		{
			Syncer.cancel();
			Syncer.purge();
			Syncer = null;
		}
		System.out.println("[iBank-CC] unloaded");
	}

	/**
	 * Reloads the config
	 */
	public void reloadConfig() 
	{
	    if(ConfigFile == null) ConfigFile = new File(getDataFolder(), "config.yml");
	    if(ConfigFile.exists()) Config = YamlConfiguration.loadConfiguration(ConfigFile);
	    else
	    {
	    	if(StreamUtils.copy(getResource("config.yml"), ConfigFile)) {
	    		Config = YamlConfiguration.loadConfiguration(ConfigFile);
	    	}
	    	else
	    	{
	    		System.out.println("[iBank-CC] OOPS! Failed loading config!");
	    	}
	    }
	}
	/**
	 * Loads the strings file
	 */
	private void loadStrings() 
	{
	    if(StringFile == null) StringFile = new File(getDataFolder(), "strings.yml");
	    
	    StringConfig = YamlConfiguration.loadConfiguration(StringFile);
	    //Get default config
	    InputStream defConfigStream = getResource("strings.yml");
	    if (defConfigStream != null) 
	    {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        StringConfig.setDefaults(defConfig);
	    }
	}

    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) { 	
    	return CommandHandler.handle(sender, cmd.getName(), args);
    }
    
	/**
	 * HERE: Wrapper to iBank
	 * Checks if an user has a permission
	 * @param user The Player
	 * @param permission The permission
	 * @return boolean
	 */
	public static boolean hasPermission(Player user, String permission) 
	{
		return iBank.hasPermission(user, permission);
	}
	
	/**
	 * Checks if a command sender has a permission
	 * @param user CommandSender 
	 * @param permission Permission
	 * @return boolean
	 */
	public static boolean hasPermission(CommandSender user, String permission) 
	{
		if(!(user instanceof Player)) return true;
		return hasPermission((Player)user, permission);
	}
}
