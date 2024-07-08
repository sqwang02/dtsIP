package softtest.registery;

import java.io.Serializable;

public class IdentityObject216 implements Serializable,Identity 
{
//	private int cellAddress = 0;
	private int addr = 0;
	private int offset = 0;
	
	private String serverIP = null;
	private String signature = null;
	
	
	
	public IdentityObject216()
	{
		
	}

	/*
	public int getCellAddress()
	{
		return cellAddress;
	}
	*/
	public int getCellAddress()
	{
		return addr;
	}
	
	public int getOffset()
	{
		return offset;
	}

	
	/*
	public void setCellAddress(int cellAddress)
	{
		this.cellAddress = cellAddress;
	}
	*/
	public void setCellAddress(int sn)
	{
		addr = (sn - 1)/4 + 1;
		offset = (sn - 1)%4;
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
		/*
		return "cellAddress！！" + cellAddress + ";  " + "serverIP！！" + serverIP + ";  " + "signature！！" + signature;
		*/
		return "cell！！" + addr + ";  " + "offset！！" + offset + ";  " + "serverIP！！" + serverIP + ";  " + "signature！！" + signature;
	}
}
