package com.iBank.CryptoCurrency.Database;

import java.io.File;
import java.sql.ResultSet;

import com.iBank.CryptoCurrency.iBankCryptoCurrency;
import com.iBank.CryptoCurrency.system.Configuration;
import com.iBank.Database.Condition;
import com.iBank.Database.Database;
import com.iBank.Database.Mysql;
import com.iBank.Database.QueryResult;
import com.iBank.Database.SQLBuilder;
import com.iBank.Database.SQLite;
import com.iBank.utils.StreamUtils;

/**
 * Providing simple access to all datasources
 * @author steffengy
 *
 */
public class DataSource 
{
	private static Drivers type = null;
	private static Database db = null;
	
	
	public static enum Drivers 
	{
		MYSQL("mysql.jar", "com.mysql.jdbc.Driver"),
		SQLite("sqlite.jar", "org.sqlite.JDBC");
		
		String filename;
		String classname;
		
		private Drivers(String filename, String classname) 
		{
			this.filename = filename;
			this.classname = classname;
		}
		
		public String getFilename() 
		{
			return filename;
		}
		
		public boolean useable()
		{
			try{
				Class.forName(classname);
				return true;
			}
			catch(Exception e) 
			{
				return false;
			}
		}
	}

	public static boolean setup(Drivers driver, String url, iBankCryptoCurrency main) 
	{
		if(driver == Drivers.SQLite || driver == Drivers.MYSQL)
		{
			if(Drivers.SQLite.useable() || Drivers.MYSQL.useable()) 
			{
				type = driver;
				if(type == Drivers.SQLite)
					db = new SQLite(new File(main.getDataFolder(), url));
				else if(type == Drivers.MYSQL) 
					db = new Mysql(url, Configuration.Entry.DatabaseUser.getValue(), Configuration.Entry.DatabasePW.getValue(), Configuration.Entry.DatabaseName.getValue());
				
				if(!db.success()) return false;
				
				if(!db.existsTable(Configuration.Entry.DatabaseMappingTable.getValue())) 
				{
					if(type == Drivers.SQLite) System.out.println("[iBank-CC] Creating SQLite tables...");
					else if(type == Drivers.MYSQL) System.out.println("[iBank-CC] Creating Mysql tables...");
					String sql = "";
					if(type == Drivers.SQLite)
						sql = StreamUtils.inputStreamToString(main.getResource("sql/sqlite.sql"));
					else if(type == Drivers.MYSQL)
						sql = StreamUtils.inputStreamToString(main.getResource("sql/mysql.sql"));
					
					for(String line : sql.split(";")) 
					{
						if(line.length()>1) 
							db.execute(
									line.replace("{$mapping$}", 
									Configuration.Entry.DatabaseMappingTable.getValue())
							);
					}
				}
				DataSource.updateStructure();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Stub here
	 */
	private static void updateStructure() 
	{

	}

	/**
	 * Shuts the data sources down
	 */
	public static void shutdown() 
	{
		if(type == Drivers.SQLite || type == Drivers.MYSQL) 
		{
			db.close();
			db = null;
		}
	}
	
	/**
	 * 
	 * @param fields
	 * @param table
	 * @param condition
	 * @return QueryResult
	 */
	public static QueryResult query(String[] fields, String table, Condition... condition) 
	{
		//if(Configuration.Entry.Debug.getBoolean())  System.out.println("QUERY_IN");
		if(type == Drivers.MYSQL || type == Drivers.SQLite) 
		{
			String query = SQLBuilder.select(fields,table, condition);
			ResultSet result = null;
			try
			{
				if(type==Drivers.SQLite || type == Drivers.MYSQL) result = db.query(query);
			}
			catch(Exception e) 
			{
				if(Configuration.Entry.Debug.getBoolean()) 
					System.out.println("[iBank-CC] Query (maybe?) failed ("+type.toString()+")"+e);
			}
			QueryResult retval = new QueryResult();
			if(result == null) return retval;
			boolean first = true;
			try 
			{
				while(result.next()) 
				{
					if(!first) 
					{
						retval.newEntry();
						retval.nextEntry();
					}
					first = false;
					for(String field : fields) 
					{
							Object tmp = result.getObject(field);
							if(tmp != null && !result.wasNull()) 
							{
								retval.add(field, tmp);
								retval.found = true;
							}
					}
				}
				retval.resetPointer();
			}
			catch (Exception e) 
			{ 
				System.out.println("[iBank-CC] Error while parsing DB-Query result!"); 
			}
			//if(Configuration.Entry.Debug.getBoolean()) System.out.println("QUERY_OUT");
			return retval;
		}
		System.out.println("[iBank-CC] Uncaught Error!");
		return null;
	}
	
	/**
	 * Inserts into a table
	 * @param table The table
	 * @param fields The fields
	 * @param values The values
	 */
	public static int insertEntry(String table, String[] fields, Object[] values) 
	{
		return insertEntry(table,fields,values, false);
	}
	
	public static int insertEntry(String table, String[] fields,  Object[] values, boolean returnId) 
	{
		if(type == Drivers.MYSQL || type == Drivers.SQLite) {
			String query = SQLBuilder.insert(fields,table, values);
			int ret = 0;
				if(returnId) 
					ret = db.insert(query);
				else
				{
					db.execute(query);
					ret = 1;
				}
			if(type==Drivers.SQLite) ((SQLite)db).commit();
			return ret;
		}
		return -1;
	}

	/**
	 * Deletes an entry from a table
	 * @param table The table
	 * @param Condition The condition (set)
	 */
	public static void deleteEntry(String table, Condition... condition) 
	{
		if(type == Drivers.MYSQL || type == Drivers.SQLite) 
		{
			String query = SQLBuilder.delete(table, condition);
			db.execute(query);
			if(type==Drivers.SQLite) ((SQLite)db).commit();
		}
	}
	/**
	 * Updates an entry in the table
	 * @param table The table
	 * @param fields The fields
 	 * @param values The values
	 * @param condition The conditions
	 */
	public static void update(String table, String[] fields, Object[] values, Condition... condition) 
	{
		if(type == Drivers.MYSQL || type == Drivers.SQLite) 
		{
			String query = SQLBuilder.update(fields,table, values, condition);
			db.execute(query);
			if(type==Drivers.SQLite) ((SQLite)db).commit();
		}
	}

}
