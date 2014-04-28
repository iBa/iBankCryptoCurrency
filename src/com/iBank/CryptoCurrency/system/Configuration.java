package com.iBank.CryptoCurrency.system;

import java.math.BigDecimal;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * The configuration class for iBank
 * @author steffengy
 * 
 */
public class Configuration {
	public static enum Entry
	{
		Enabled("System.Enabled", true),
		Debug("System.Debug", false),
		DatabaseType("System.Database.Type", "sqlite"),
		DatabaseUrl("System.Database.Url", "cc.db"),
		DatabaseName("System.Database.Database", "cc"),
		DatabaseUser("System.Database.User", "user"),
		DatabasePW("System.Database.Password", "pw"), 
		DatabaseMappingTable("System.Database.Tables.Mapping", "mapping"),
		CoinCurrencyName("System.Coin.CurrencyName", "emc"),
		CoinMinConfirms("System.Coin.MinConfirms", 6),
		CoinExchangeRate("System.Coin.ExchangeRate", 100),
		CoinServer("System.Coin.Server", "127.0.0.1"),
		CoinPort("System.Coin.Port", 8235),
		CoinLoginUser("System.Coin.LoginUser", "iBank"),
		CoinLoginPassword("System.Coin.LoginPassword", "Password"),
		CoinAccount("System.Coin.Account", "iBank");
		
		String key;
		Object value;
	
		/**
		 * Constructor of a Value
		 * @param Name The name of the entry
		 * @param value The value of the entry
		 */
		private Entry(String Name, Object value) 
		{
			this.key = Name;
			this.value = value;
		}
		/**
		 * @return The key of the Entry
		 */
		public String getKey() 
		{
			return key;
		}
		/**
		 * Gets the value as String
		 * @return String The value of the Entry
		 */
		public String getValue() 
		{
			return (String) value;
		}
		
		/**
		 * Gets the value as Object
		 * @return Object
		 */
		public Object getObject()
		{
		    return value;
		}
		
		/**
		 * @return The value of the Entry as boolean
		 */
		public Boolean getBoolean() 
		{
            return (Boolean) value;
        }
		
		/*
		 * @return The value of the Entry as Integer (or Double)
		 */
        public Integer getInteger() 
        {
            if(value instanceof Double) return ((Double) value).intValue();
            return (Integer) value;
        }
        
        /**
         * @return The value of the Entry as Double
         */
        public Double getDouble() 
        {
            if(value instanceof Integer) return (double) ((Integer) value).intValue();

            return (Double) value;
        }
        
        /**
         * @return The value of the Entry as BigDecimal
         */
        public BigDecimal getBigDecimal() 
        {
            return new BigDecimal(String.valueOf(value));
        }
        
        /**
         * @return The value of the Entry as Long (or Integer)
         */
        public Long getLong() 
        {
            if(value instanceof Integer) return ((Integer) value).longValue();

            return (Long) value;
        }
        
        /*
         * @return The value of the Entry as List
         */
        @SuppressWarnings("unchecked")
		public List<String> getStringList() 
		{
        		return (List<String>) value;
        }
        
        /*
         * Sets the value of this Entry
         * @param value the value as object
         */
        public void setValue(Object value) 
        {
        	this.value = value;
        }
        
        @Deprecated
        public String toString() {
        	return getValue();
        }
	}
	
	public static enum StringEntry
	{
		CCTag("Tags.CC", "&g&[&w&CryptoC&g&]"),
		RootDescription("Commands.Root", "Shows the users CryptoCoin Address"),
		HelpDescription("Commands.Help", "Displays the help"),
		Address("Data.Address", "Your {{currency}}-address: http://iba.github.io/iBankCryptoCurrency/?"),
		ErrorNoAccount("Error.no_account", "Sorry, that account doesn't exist!"),
		ErrorNoPlayer("Error.no_player", "Sorry, you need to be a player to execute this!"),
		ErrorWrongArguments("Error.wrong_arguments", "Wrong arguments given!"), 
		ErrorInternal("Error.internal", "An internal error occured!");
		
		String key;
		String value;
	
		/**
		 * Constructor of a Value
		 * @param Name The name of the entry
		 * @param value The value of the entry
		 */
		private StringEntry(String Name, String value) 
		{
			this.key = Name;
			this.value = value;
		}
		
		/**
		 * @return The key of the Entry
		 */
		public String getKey() 
		{
			return key;
		}
		
		/**
		 * @return The value of the Entry
		 */
		public String getValue() 
		{
			return value;
		}
		
        /*
         * Sets the value of this Entry
         * @param value the value
         */
        public void setValue(String value) 
        {
        	this.value = value;
        }
        
        @Override
        @Deprecated
        public String toString()
        {
            return this.value;
        }
	}
    /**
     * Sets the config system up
     * @param config The configuration got from the YamlFile 
     */
    public static void init(YamlConfiguration config)
    {
    	for(Entry s : Entry.values())
    		if(!s.getKey().isEmpty() && config.get(s.getKey()) != null)
    			s.setValue(config.get(s.getKey()));
    }
    
    /**
     * Sets the lang system up
     * @param config The configuration got from the YamlFile 
     */
    public static void stringinit(YamlConfiguration config)
    {
    	for(StringEntry s : StringEntry.values())
    		if(!s.getKey().isEmpty() && config.get(s.getKey()) != null)
				s.setValue(config.getString(s.getKey()));
    }
}
