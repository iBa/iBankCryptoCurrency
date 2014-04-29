package com.iBank.CryptoCurrency.system;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.iBank.CryptoCurrency.Database.DataSource;
import com.iBank.Database.AndCondition;
import com.iBank.Database.Condition;
import com.iBank.Database.QueryResult;

public class DBModel 
{
	public static class PaymentInformation
	{
		public String address;
		public BigDecimal amount;
		public String bankAcc;
		
		public PaymentInformation(String address, String account, BigDecimal am) {
			this.address = address;
			bankAcc = account;
			amount = am;
		}	
	}
	
	public static void setPaymentInformation(PaymentInformation info) 
	{
		DataSource.update(Configuration.Entry.DatabaseMappingTable.getValue(), 
				new String[] {"amount"},
				new Object[] {info.amount},
				new AndCondition("account", info.bankAcc, Condition.Operators.IDENTICAL)
		);
	}
	
	public static PaymentInformation[] getPaymentArray()
	{
		QueryResult data = DataSource.query(new String[]{"address", "amount", "account"}, Configuration.Entry.DatabaseMappingTable.getValue());
		if(!data.found) return new PaymentInformation[0];
		List<PaymentInformation> ret = new ArrayList<PaymentInformation>();
		do
		{
			ret.add(new PaymentInformation(data.getString("address"), data.getString("account"), data.getBigInteger("amount")));
		} while(data.nextEntry());
		return ret.toArray(new PaymentInformation[0]);
	}
	
	/* payment info by address */
	public static PaymentInformation getPaymentInformation(String address)
	{
		QueryResult data = DataSource.query(new String[]{"amount", "account"}, Configuration.Entry.DatabaseMappingTable.getValue(), new AndCondition("address", address, Condition.Operators.IDENTICAL));
		if(!data.found) return null;
		return new PaymentInformation(address, data.getString("account"), data.getBigInteger("amount"));
	}
	
	/* address by bank account */
	public static String getAddress(String bankAcc)
	{
		QueryResult data = DataSource.query(new String[]{"address"}, Configuration.Entry.DatabaseMappingTable.getValue(), new AndCondition("account", bankAcc, Condition.Operators.IDENTICAL));
		if(!data.found) return null;
		return data.getString("address");
	}
	
	public static void setAddress(String addr, String bankAcc)
	{
		DataSource.insertEntry(Configuration.Entry.DatabaseMappingTable.getValue(),
			new String[]{"address","amount", "account"}, 
			new Object[]{addr, 0, bankAcc}
		);
	}
}
