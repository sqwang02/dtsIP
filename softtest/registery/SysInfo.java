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
		// 查找加密锁
		if (0 != aNox.NoxFind(nAppID, keyHandles, nKeyNum)) {
			if (softtest.config.java.Config.LANGUAGE == 0) {
				JOptionPane.showMessageDialog(null, "请插入加密锁！");
			} else {
				JOptionPane.showMessageDialog(null,
						"Please insert the encryption lock！");
			}
			return false;
		}
		// System.out.println("找到"+nKeyNum[0]+"只锁");
		// System.out.println(UID);
		// 打开第一个加密锁3e50fbc633fbfe24
		if (0 != aNox.NoxOpen(keyHandles[0], "3e50fbc633fbfe24")) {
			if (softtest.config.java.Config.LANGUAGE == 0) {
				JOptionPane.showMessageDialog(null, "加密锁错误或者已经过期！");
			} else {
				JOptionPane.showMessageDialog(null,
						"Encryption lock errors or has expired!");
			}
			System.out.println(aNox.NoxGetLastError());
			return false;
		}
		aNox.NoxClose(keyHandles[0]);
		// System.out.println("加密锁已关闭");
		return true;
	}

	public static int getPhase() {
		NoxTimerKey aNox = new NoxTimerKey();
		int[] keyHandles = new int[8];
		int[] nKeyNum = new int[1];
		int nAppID = 0xFFFFFFFF;
		int nRtn = 0;
		// 获得次数限制信息
		int[] nRemain = new int[1];
		int[] nMax = new int[1];
		int[] Mode2 = new int[1];

		// 查找加密锁
		if (0 != aNox.NoxFind(nAppID, keyHandles, nKeyNum)) {
			if (softtest.config.java.Config.LANGUAGE == 0) {
				JOptionPane.showMessageDialog(null, "找不到加密锁！");
			} else {
				JOptionPane.showMessageDialog(null,
						"Can not find encryption locks!");
			}
			return -1;
		}
		// System.out.println("找到"+nKeyNum[0]+"只锁");
		nRtn = aNox.NoxGetRemnantCount(keyHandles[0], nRemain, nMax, Mode2);
		if (nRtn != 0) {
			System.out.println("获得次数限制信息失败");
			System.out.println(aNox.NoxGetLastError());
			return -1;
		}
		int all = nMax[0];
		int left = nRemain[0];
		int num = all / Config.PHASE_NUMUBER;
		int total = (all - left) / num + 1;
		if (total > Config.PHASE_NUMUBER) {
			if (softtest.config.java.Config.LANGUAGE == 0) {
				JOptionPane.showMessageDialog(null, "加密锁已过期！");
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
		String hardDiskSN = HardDiskUtils.getHardDiskSN().trim(); // 获取硬盘序列号
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
	// 把硬盘序列号和网卡序列号进行拼接
	public static String getSysInfo() {
		String sysInfo = "";
		String hardDiskSN = HardDiskUtils.getHardDiskSN(); // 获取硬盘序列号
		String networkCardMac = NetworkUtils.getMacAddress(); // 获取网卡mac地址
		// 取硬盘序列号最后六位
		if (null != hardDiskSN && !hardDiskSN.equals("")) {

			sysInfo += hardDiskSN.substring(hardDiskSN.length() - 6, hardDiskSN
					.length());
		} else {
			sysInfo += "AFWG9D";
		}
		// 取网卡序列号的最后六位
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
				// 将偶数位顺序的硬盘序列号和逆序的网卡序列号进行互换
				tmp = bSysInfo[i];
				bSysInfo[i] = bSysInfo[bSysInfo.length - i - 1];
				bSysInfo[bSysInfo.length - i - 1] = tmp;
			}
		}
		return new String(bSysInfo);
	}

}
