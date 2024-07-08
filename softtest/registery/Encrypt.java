package softtest.registery;

import java.security.MessageDigest;

import softtest.config.java.Config;

public class Encrypt {
	// 对一个字符串使用MD5算法进行加密并转换成128位字符串
	public static String encryptHardInfo(String str) {
		try {
			if (Config.PHASE_REGISTER) {
				int phase = SysInfo.getPhase();				
				return encryptHardInfoPhase(str, phase);
			} else {
				return encrypt(str);
			}
		} catch (Exception e) {
			System.out.print("Exception : " + e.toString());
		}
		
		return null;
	}
	// 对一个字符串使用MD5算法进行加密并转换成128位字符串
	public static String encryptHardInfoPhase(String str, int phase) {
		try {
			if (Config.PHASE_REGISTER) {				
				byte[] plainText = str.substring(0, 12).getBytes();
				// System.out.println("原始数据16进制信息 ＝ "+byteToString(plainText));

				// /////////////////////////////
				// 1 首先使用MD5算法对数据进行散列，得到定长的160bit的信息作为前缀放在字符串前面。
				MessageDigest messageDigest = MessageDigest.getInstance("MD5");
				byte[] digest = messageDigest.digest(plainText);
				String text =  byteToStringHardInfo(digest); // to HexString
				String encrypt = "";
				for (int i = 0; i < 4; i++ ) {
					byte[] temp = text.substring(i * 8, (i+1)*8).getBytes();
					//System.out.println(byteToStringHardInfo(messageDigest.digest(temp)));
					encrypt = encrypt + byteToStringHardInfo(messageDigest.digest(temp));
				}
				
				return encrypt.substring((phase-1) * 12, phase * 12 > encrypt.length() ? encrypt.length() : phase * 12);
			} else {
				return encrypt(str);
			}
		} catch (Exception e) {
			System.out.print("Exception : " + e.toString());
		}
		return null;
	}

	// 对一个字符串使用MD5算法进行加密
	public static String encrypt(String str) {
		try {
			byte[] plainText = str.getBytes();
			// System.out.println("原始数据16进制信息 ＝ "+byteToString(plainText));

			// /////////////////////////////
			// 1 首先使用MD5算法对数据进行散列，得到定长的160bit的信息作为前缀放在字符串前面。
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] digest = messageDigest.digest(plainText);
			// System.out.println("签名信息长度 ＝ "+digest.length);
			// System.out.println("签名信息 ＝ "+byteToString(digest));

			// 2.将加密后的字符串与原字符串进行拼接
			byte[] bText2 = new byte[digest.length + plainText.length];
			for (int i = 0; i < digest.length; i++) {
				bText2[i] = digest[i];
			}
			for (int i = 0; i < plainText.length; i++) {
				bText2[digest.length + i] = plainText[i];
			}
			// System.out.println("加签名信息后文本长度 ＝ "+bText2.length);
			// System.out.println("签名信息 ＝ "+byteToString(bText2));

			// 3.再对字符串进行一次MD5加密
			byte[] digest2 = messageDigest.digest(bText2); // 重点
			return byteToString(digest2); // to HexString

		} catch (Exception e) {
			System.out.print("Exception : " + e.toString());
		}
		return null;
	}

//	public static void main(String[] args) {
//		System.out.println(encryptHardInfo(HardDiskUtils.getHardDiskSN()));
//	}

	/**
	 * 将字节数组装化为32位的16进制字符串
	 * 
	 * @param bts
	 *            传入需要转换的字节数组
	 */
	public static String byteToStringHardInfo(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			// System.out.println(tmp);
			des += tmp;

		}
		return des.toUpperCase();
	}

	/**
	 * 将字节数组装化为16进制字符串
	 * 
	 * @param bts
	 *            传入需要转换的字节数组
	 */
	public static String byteToString(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {

			if (isPrimeNumber(i)) {
				tmp = (Integer.toHexString(bts[i] & 0xFF));
				if (tmp.length() == 1) {
					des += "0";
				}
				// System.out.println(tmp);
				des += tmp;
			}

		}
		return des.toUpperCase();
	}

	/**
	 * 判断一个正整数是否为素数
	 * 
	 * @param number
	 *            传入需要判断的正整数
	 */
	public static boolean isPrimeNumber(int number) {
		boolean flag = true;
		if (number == 0 || number == 1) {
			flag = false;
		}
		if (number < 0)
			throw new IllegalArgumentException("number是不合法的参数！");
		for (int i = 2; i <= Math.sqrt(number); i++) {
			if (number % i == 0)
				flag = false;
			if (flag == false)
				break;
		}
		return flag;
	}

}
