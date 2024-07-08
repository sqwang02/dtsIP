package softtest.registery;

import java.io.Serializable;

public class IdentityObject54 implements Serializable,Identity 
{
	private int cellAddress = 0;
	private String serverIP = null;
	private String signature = null;
	
	
	
	public IdentityObject54()
	{
		
	}

	public int getCellAddress()
	{
		return cellAddress;
	}
	
	public int getOffset()
	{
		return 0;
	}

	public void setCellAddress(int cellAddress)
	{
		this.cellAddress = cellAddress;
	}

	public String getServerIP()
	{
		return serverIP;
	}

	public void setServerIP(String serverIP)
	{
		this.serverIP = serverIP;
	}

	public String getSignature()
	{
		return signature;
	}

	public void setSignature(String signature)
	{
		this.signature = signature;
	}
	
	public String toString()
	{
		return "cellAddress！！" + cellAddress + ";  " + "serverIP！！" + serverIP + ";  " + "signature！！" + signature;
	}
}
