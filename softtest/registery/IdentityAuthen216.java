package softtest.registery;

import java.security.MessageDigest;

public class IdentityAuthen216 implements IdentityCheck
{
	public int checkIdentity(Identity id, SentinelUtils sentinel, int base)throws Exception
	{
		int clientNum = sentinel.readData(base);

		int addr = id.getCellAddress();
		int offset = id.getOffset() * 4;

		if(addr < 1)
		{
			return 1;
		}

		if(addr > clientNum)
		{
			return 2;
		}

		int data = sentinel.readData(base + addr);

		byte[] extraMessage = new byte[1];
		data = data >> offset;
		extraMessage[0]= (byte)(data & 0xF);

		String hardDiskSN = HardDiskUtils.getHardDiskSN();
		String str = hardDiskSN.concat(new String(extraMessage));
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		byte[] outcome = digest.digest(str.getBytes());
		String compareStr = new String(outcome);

		String signature = id.getSignature();

		if(compareStr.equalsIgnoreCase(signature))
		{
			return 0;
		}
		else
		{
			return 3;
		}
	}
	
	/*
	public boolean checkIdentity(IdentityObject id, SentinelUtils sentinel, int base)throws Exception
	{
		boolean result = false;
		
		int clientNum = sentinel.readData(base);
		
		int addr = id.getCellAddress();

		
		if(addr > clientNum)
		{
			result = false;
		}
		
		int data = sentinel.readData(base + addr);
//		System.out.println(Integer.toBinaryString(data));
		
		byte[] extraMessage = new byte[2];
		extraMessage[0] = (byte)(data & 0xFF);
		data = data >> 8;
		extraMessage[1] = (byte)(data & 0xFF);
		
		String hardDiskSN = HardDiskUtils.getHardDiskSN();
		String str = hardDiskSN.concat(new String(extraMessage));
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		byte[] outcome = digest.digest(str.getBytes());
		String compareStr = new String(outcome);
//		System.out.println("AuthenObejct: " + compareStr);
		String signature = id.getSignature();
//		System.out.println(signature);
		if(signature!= null && compareStr.equalsIgnoreCase(signature))
		{
			result = true;
		}
		
		return result;
	}
	*/
}
