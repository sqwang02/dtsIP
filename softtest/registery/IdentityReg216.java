package softtest.registery;

import java.security.MessageDigest;
import java.util.Random;

public class IdentityReg216 implements IdentityCheck
{
	public int checkIdentity(Identity id, SentinelUtils sentinel, int base) throws Exception
	{
		int addr = id.getCellAddress();
		int offset = id.getOffset()*4;
		
		int data = sentinel.readData(base + addr);  
		
		int d = data >> offset;
		d = data & 0xF;
		
		if(d != 0)
		{
			return 4;
		}
		
		byte[] extraMessage = new byte[1];
		Random random = new Random();
		do
		{
			random.nextBytes(extraMessage);
			extraMessage[0] = (byte)(extraMessage[0] & 0xF);
		}
		while(extraMessage[0] == 0);
		
		String hardDiskSN = HardDiskUtils.getHardDiskSN();
		String str = hardDiskSN.concat(new String(extraMessage));
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		byte[] outcome = digest.digest(str.getBytes());
		String signature = new String(outcome);
		id.setSignature(signature);
		
		int tmp = extraMessage[0] << offset;
		data = data ^ tmp;
		sentinel.writeData(data, addr + base);
		
		return 0;
	}
	
	/*
	public boolean checkIdentity(IdentityObject id, SentinelUtils sentinel, int base) throws Exception
	{
		boolean result;
		
		int data = sentinel.readData(base + id.getCellAddress());
		
		if(data != 0)
		
		{
			result = false;

		}
		else
		{
			byte[] extraMessage = new byte[2];
			Random random = new Random();
			do
			{
				random.nextBytes(extraMessage);
			}
			while(extraMessage[0]== 0 && extraMessage[1] == 0);
			
			String hardDiskSN = HardDiskUtils.getHardDiskSN();
			String str = hardDiskSN.concat(new String(extraMessage));
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] outcome = digest.digest(str.getBytes());
			String signature = new String(outcome);
			id.setSignature(signature);
//			System.out.println("RegObject: " + signature);
			
			
			data = ((int)extraMessage[1]) & 0xFF;
			data = data << 8;
			data = data ^ (((int)extraMessage[0]) & 0xFF);
//			System.out.println(Integer.toBinaryString(data));
			sentinel.writeData(data, id.getCellAddress() + base);

			
			result = true;

		}

		return result;
	}
	*/
	
}
