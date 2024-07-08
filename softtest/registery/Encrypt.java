package softtest.registery;

import java.security.MessageDigest;

import softtest.config.java.Config;

public class Encrypt {
	// ��һ���ַ���ʹ��MD5�㷨���м��ܲ�ת����128λ�ַ���
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
	// ��һ���ַ���ʹ��MD5�㷨���м��ܲ�ת����128λ�ַ���
	public static String encryptHardInfoPhase(String str, int phase) {
		try {
			if (Config.PHASE_REGISTER) {				
				byte[] plainText = str.substring(0, 12).getBytes();
				// System.out.println("ԭʼ����16������Ϣ �� "+byteToString(plainText));

				// /////////////////////////////
				// 1 ����ʹ��MD5�㷨�����ݽ���ɢ�У��õ�������160bit����Ϣ��Ϊǰ׺�����ַ���ǰ�档
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

	// ��һ���ַ���ʹ��MD5�㷨���м���
	public static String encrypt(String str) {
		try {
			byte[] plainText = str.getBytes();
			// System.out.println("ԭʼ����16������Ϣ �� "+byteToString(plainText));

			// /////////////////////////////
			// 1 ����ʹ��MD5�㷨�����ݽ���ɢ�У��õ�������160bit����Ϣ��Ϊǰ׺�����ַ���ǰ�档
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] digest = messageDigest.digest(plainText);
			// System.out.println("ǩ����Ϣ���� �� "+digest.length);
			// System.out.println("ǩ����Ϣ �� "+byteToString(digest));

			// 2.�����ܺ���ַ�����ԭ�ַ�������ƴ��
			byte[] bText2 = new byte[digest.length + plainText.length];
			for (int i = 0; i < digest.length; i++) {
				bText2[i] = digest[i];
			}
			for (int i = 0; i < plainText.length; i++) {
				bText2[digest.length + i] = plainText[i];
			}
			// System.out.println("��ǩ����Ϣ���ı����� �� "+bText2.length);
			// System.out.println("ǩ����Ϣ �� "+byteToString(bText2));

			// 3.�ٶ��ַ�������һ��MD5����
			byte[] digest2 = messageDigest.digest(bText2); // �ص�
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
	 * ���ֽ�����װ��Ϊ32λ��16�����ַ���
	 * 
	 * @param bts
	 *            ������Ҫת�����ֽ�����
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
	 * ���ֽ�����װ��Ϊ16�����ַ���
	 * 
	 * @param bts
	 *            ������Ҫת�����ֽ�����
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
	 * �ж�һ���������Ƿ�Ϊ����
	 * 
	 * @param number
	 *            ������Ҫ�жϵ�������
	 */
	public static boolean isPrimeNumber(int number) {
		boolean flag = true;
		if (number == 0 || number == 1) {
			flag = false;
		}
		if (number < 0)
			throw new IllegalArgumentException("number�ǲ��Ϸ��Ĳ�����");
		for (int i = 2; i <= Math.sqrt(number); i++) {
			if (number % i == 0)
				flag = false;
			if (flag == false)
				break;
		}
		return flag;
	}

}
