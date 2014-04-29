package com.iBank.CryptoCurrency;

import java.math.BigDecimal;
import java.util.TimerTask;

import com.azazar.bitcoin.jsonrpcclient.BitcoinException;
import com.iBank.CryptoCurrency.system.Configuration;
import com.iBank.CryptoCurrency.system.DBModel;
import com.iBank.CryptoCurrency.system.DBModel.PaymentInformation;
import com.iBank.system.Bank;
import com.iBank.system.BankAccount;

public class Syncer extends TimerTask {

	@Override
	public void run() {
		if(Configuration.Entry.Debug.getBoolean()) System.out.println("[iBank-CC] Checking Payment stuff...");
		for(PaymentInformation pm : DBModel.getPaymentArray())
		{
			double amount = 0;
			try {
				amount = iBankCryptoCurrency.client.getReceivedByAddress(pm.address, Configuration.Entry.CoinMinConfirms.getInteger());
			} catch (BitcoinException e) {
				e.printStackTrace();
			}
			if(Configuration.Entry.Debug.getBoolean()) System.out.println("[iBank-CC] DBG: " + 
					pm.amount + "!=" + amount + " / " + pm.bankAcc);
			if(amount == pm.amount) return;
			BankAccount bankAcc = Bank.getAccount(pm.bankAcc);
			if(bankAcc == null) { System.out.println("[iBank-CC] This is not good! Invalid bank account: " + pm.bankAcc + " " + pm.address); continue; }
			bankAcc.addBalance(new BigDecimal(amount).subtract(new BigDecimal(pm.amount)).multiply(new BigDecimal(Configuration.Entry.CoinExchangeRate.getDouble())));
			pm.amount = amount;
			DBModel.setPaymentInformation(pm);
		}
	}

}
