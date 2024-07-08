package softtest.registery;

import java.security.MessageDigest;
import java.util.Random;

public class IdentityReg54 implements IdentityCheck
{
	public int checkIdentity(Identity id, SentinelUtils sentinel, int base) throws Exception
	{
		int data = sentinel.readData(base + id.getCellAddress());
			
		if(data != 0)
		{
			return 4;

		}
		
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
				
		data = ((int)extraMessage[1]) & 0xFF;
		data = data << 8;
		data = data ^ (((int)extraMessage[0]) & 0xFF);
		sentinel.writeData(data, id.getCellAddress() + base);

		return 0;
	}
}
