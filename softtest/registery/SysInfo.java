package softtest.registery;

import javax.swing.JOptionPane;

import LONGMAI.NoxTimerKey;
import softtest.config.java.Config;

public class SysInfo {
	public static boolean checkPermission() {
		NoxTimerKey aNox = new NoxTimerKey();
		int[] keyHandles = new int[8];
		int[] nKeyNum = new int[1];
		int nAppID = 0xFFFFFFFF;
		int nRtn = 0;
		// ���Ҽ�����
		if (0 != aNox.NoxFind(nAppID, keyHandles, nKeyNum)) {
			if (softtest.config.java.Config.LANGUAGE == 0) {
				JOptionPane.showMessageDialog(null, "������������");
			} else {
				JOptionPane.showMessageDialog(null,
						"Please insert the encryption lock��");
			}
			return false;
		}
		// System.out.println("�ҵ�"+nKeyNum[0]+"ֻ��");
		// System.out.println(UID);
		// �򿪵�һ��������3e50fbc633fbfe24
		if (0 != aNox.NoxOpen(keyHandles[0], "3e50fbc633fbfe24")) {
			if (softtest.config.java.Config.LANGUAGE == 0) {
				JOptionPane.showMessageDialog(null, "��������������Ѿ����ڣ�");
			} else {
				JOptionPane.showMessageDialog(null,
						"Encryption lock errors or has expired!");
			}
			System.out.println(aNox.NoxGetLastError());
			return false;
		}
		aNox.NoxClose(keyHandles[0]);
		// System.out.println("�������ѹر�");
		return true;
	}

	public static int getPhase() {
		NoxTimerKey aNox = new NoxTimerKey();
		int[] keyHandles = new int[8];
		int[] nKeyNum = new int[1];
		int nAppID = 0xFFFFFFFF;
		int nRtn = 0;
		// ��ô���������Ϣ
		int[] nRemain = new int[1];
		int[] nMax = new int[1];
		int[] Mode2 = new int[1];

		// ���Ҽ�����
		if (0 != aNox.NoxFind(nAppID, keyHandles, nKeyNum)) {
			if (softtest.config.java.Config.LANGUAGE == 0) {
				JOptionPane.showMessageDialog(null, "�Ҳ�����������");
			} else {
				JOptionPane.showMessageDialog(null,
						"Can not find encryption locks!");
			}
			return -1;
		}
		// System.out.println("�ҵ�"+nKeyNum[0]+"ֻ��");
		nRtn = aNox.NoxGetRemnantCount(keyHandles[0], nRemain, nMax, Mode2);
		if (nRtn != 0) {
			System.out.println("��ô���������Ϣʧ��");
			System.out.println(aNox.NoxGetLastError());
			return -1;
		}
		int all = nMax[0];
		int left = nRemain[0];
		int num = all / Config.PHASE_NUMUBER;
		int total = (all - left) / num + 1;
		if (total > Config.PHASE_NUMUBER) {
			if (softtest.config.java.Config.LANGUAGE == 0) {
				JOptionPane.showMessageDialog(null, "�������ѹ��ڣ�");
			} else {
				JOptionPane.showMessageDialog(null,
						"Encryption lock has expired!");
			}
			System.exit(0);
		}
		return total;
	}

	public static String getHardSN() {
		if (Config.PHASE_REGISTER) {
			int phase = getPhase();
			return getHardDiskSN() + phase;
		}
		return getHardDiskSN();
	}

	public static String getHardDiskSN() {
		String ret = "AFWG9DKDY5W7";
		String hardDiskSN = HardDiskUtils.getHardDiskSN().trim(); // ��ȡӲ�����к�
		if (null != hardDiskSN && !hardDiskSN.equals("")) {
			if (hardDiskSN.length() >= ret.length()) {
				ret = hardDiskSN.substring(0, 12);
			} else {
				ret = hardDiskSN + ret.substring(hardDiskSN.length());
			}
		}
		return ret;
	}

	// public static void main(String[] args) {
	// System.out.println(getHardSN());
	// }
	// ��Ӳ�����кź��������кŽ���ƴ��
	public static String getSysInfo() {
		String sysInfo = "";
		String hardDiskSN = HardDiskUtils.getHardDiskSN(); // ��ȡӲ�����к�
		String networkCardMac = NetworkUtils.getMacAddress(); // ��ȡ����mac��ַ
		// ȡӲ�����к������λ
		if (null != hardDiskSN && !hardDiskSN.equals("")) {

			sysInfo += hardDiskSN.substring(hardDiskSN.length() - 6, hardDiskSN
					.length());
		} else {
			sysInfo += "AFWG9D";
		}
		// ȡ�������кŵ������λ
		if (null != networkCardMac && !networkCardMac.equals("")) {
			String[] tmp = networkCardMac.split("-");
			String tmpstr = "";
			for (int i = 0; i < tmp.length; i++) {
				tmpstr += tmp[i];
			}
			sysInfo += tmpstr.substring(tmpstr.length() - 6, tmpstr.length());
		} else {
			sysInfo += "KDY5W7";
		}

		byte[] bSysInfo = sysInfo.getBytes();
		byte tmp;
		for (int i = 0; i < bSysInfo.length / 2; i++) {
			if (i % 2 == 1) {
				// ��ż��λ˳���Ӳ�����кź�������������кŽ��л���
				tmp = bSysInfo[i];
				bSysInfo[i] = bSysInfo[bSysInfo.length - i - 1];
				bSysInfo[bSysInfo.length - i - 1] = tmp;
			}
		}
		return new String(bSysInfo);
	}

}
