package softtest.registery.file;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import softtest.registery.HardDiskUtils;
import sun.misc.BASE64Decoder;

class RegEncoder
{

	private static Cipher enCipher = null;

	private static Cipher deCipher = null;

	private static Key key = null;

	private static final String ALGORITHM = "DES";

	static
	{
		try
		{
			String rawKey = HardDiskUtils.getSysInfo();
			byte[] keyData = decryptBASE64(rawKey);
			// byte[] keyData = rawKey.getBytes();
			// System.out.println(new String(keyData));
			key = toKey(keyData);
		}
		catch (Exception ex)
		{
			key = null;
		}

	}

	public static Cipher enCipher() throws Exception
	{
		//if (enCipher == null)
		//{
		enCipher = Cipher.getInstance(ALGORITHM);
		enCipher.init(Cipher.ENCRYPT_MODE, key);
		//}
		return enCipher;
	}

	public static Cipher deCipher() throws Exception
	{
		//if (deCipher == null)
		//{
		deCipher = Cipher.getInstance(ALGORITHM);
		deCipher.init(Cipher.DECRYPT_MODE, key);
		//}
		return deCipher;
	}

	private static Key toKey(byte[] keyData) throws Exception
	{
		DESKeySpec dks = new DESKeySpec(keyData);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
		SecretKey secretKey = keyFactory.generateSecret(dks);

		return secretKey;
	}

	private static byte[] decryptBASE64(String key) throws Exception
	{
		return (new BASE64Decoder()).decodeBuffer(key);
	}

}
