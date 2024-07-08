package softtest.registery;

import java.security.MessageDigest;

public class IdentityAuthen54 implements IdentityCheck
{
	public int checkIdentity(Identity id, SentinelUtils sentinel, int base)throws Exception
	{
		int clientNum = sentinel.readData(base);
			
		int addr = id.getCellAddress();

		if(addr < 1)
		{
			return 1;
		}
		if(addr > clientNum)
		{
			return 2;
		}
			
		int data = sentinel.readData(base + addr);
			
		byte[] extraMessage = new byte[2];
		extraMessage[0] = (byte)(data & 0xFF);
		data = data >> 8;
		extraMessage[1] = (byte)(data & 0xFF);
			
		String hardDiskSN = HardDiskUtils.getHardDiskSN();
		String str = hardDiskSN.concat(new String(extraMessage));
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		byte[] outcome = digest.digest(str.getBytes());
		String compareStr = new String(outcome);

		String signature = id.getSignature();

		if(signature!= null && compareStr.equalsIgnoreCase(signature))
		{
			return 0;
		}
		else
		{
			return 3;
		}
	}
}
