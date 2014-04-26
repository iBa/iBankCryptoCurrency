package com.iBank.CryptoCurrency.Commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.azazar.bitcoin.jsonrpcclient.BitcoinException;
import com.iBank.CryptoCurrency.iBankCryptoCurrency;
import com.iBank.CryptoCurrency.system.Configuration;
import com.iBank.CryptoCurrency.system.DBModel;
import com.iBank.system.Bank;
import com.iBank.system.Command;
import com.iBank.system.CommandInfo;

/**
 * /cryptoc - Shows the address of the executor
 * 
 * @author steffengy Can't be run from console
 */
@CommandInfo(arguments = { "Account" }, permission = "cryptoc.access", root = "cryptoc", sub = "show")
public class RootCommand extends Command {

	/**
	 * Shows if in bank region if enabled Shows a list of all accounts the
	 * player owns and has access to
	 */
	@Override
	public void handle(CommandSender sender, String[] arguments) 
	{
		if(arguments.length != 1)
		{
			send(sender, "&r&"+Configuration.StringEntry.ErrorWrongArguments.getValue());
			return;
		}
		if(!(sender instanceof Player)) 
		{
			send(sender, "&r&"+ Configuration.StringEntry.ErrorNoPlayer.getValue());
			return;
		}

		String bankAcc = arguments[0];
		if(!Bank.hasAccount(bankAcc))
		{
			send(sender, "&r&"+ Configuration.StringEntry.ErrorNoAccount.getValue());
			return;
		}
		String addr = DBModel.getAddress(bankAcc);
		if(addr == null)
		{
			try {
				addr = iBankCryptoCurrency.client.getNewAddress(Configuration.Entry.CoinAccount.getValue());
				DBModel.setAddress(addr, bankAcc);
			} catch (BitcoinException e) {
				send(sender, "&r&"+ Configuration.StringEntry.ErrorInternal.getValue());
				e.printStackTrace();
				return;
			}
		}
		send(sender, Configuration.StringEntry.Address.getValue().replace("{{currency}}",  Configuration.Entry.CoinCurrencyName.getValue()) + addr);
	}

	public String getHelp() {
		return Configuration.StringEntry.RootDescription.getValue();
	}
}
