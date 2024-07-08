package softtest.registery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import softtest.config.java.Config;

import ca.beq.util.win32.registry.RegistryKey;
import ca.beq.util.win32.registry.RegistryValue;
import ca.beq.util.win32.registry.RootKey;

public class Registery {

	private static Map<String, String> reg = new HashMap<String, String>();
	static File file = new File("..\\..\\DTSReg.txt");

	// FileOutputStream fos=new FileOutputStream(file);
	// PrintWriter pw=new PrintWriter(fos);
	// pw.write(/*sBuffer.toString().toCharArray()*/);
	// pw.flush();
	// pw.close();

	// 把相应的值储存到变量中去
	public static void writeValue(String version, String hardWareSN,
			String softWareSN) {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
		
		FileOutputStream fos;
		PrintWriter pw = null;
		try {
			fos = new FileOutputStream(file);
			pw = new PrintWriter(fos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		reg.clear();
		reg.put("version", version.toLowerCase());
		reg.put("hardwaresn", hardWareSN.toLowerCase());
		reg.put("softwaresn", softWareSN.toLowerCase());

		// 在注册表的HKEY_LOCAL_MACHINE\Software\JavaSoft\prefs下写入注册表值.
		RegistryKey r = RegisterAssitant.createRegistryKey("Software\\dts");

		RegistryValue v = new RegistryValue();
		// Preferences pre = Preferences.systemRoot().node("/dts");
		// for (String str : reg.keySet()) {
		// pre.put(str, reg.get(str));
		// }
		for (String str : reg.keySet()) {
			RegisterAssitant.addToRegistryValue(v, str, reg.get(str));
			RegisterAssitant.writeRegister(r, v);

			if (null != pw) {
				pw.write(str + ":" + reg.get(str)
						+ System.getProperty("line.separator"));
			}

		}

		pw.flush();
		pw.close();
	}

	// 检查是否已经注册
	public static boolean checkRegistery() {
		boolean ret = false;
		try {
			String hardWareSNSysStr = SysInfo.getHardSN();
			String softWareSNSysStr = Encrypt.encryptHardInfo(hardWareSNSysStr);
			if (Registery.checkValue("dtsjava-2.3.1", hardWareSNSysStr,
					softWareSNSysStr)) {
				ret = true;
				if (Config.LOCK) {
					//检查权限
					if (!SysInfo.checkPermission()) {
						System.exit(0);
					}
				}
			} else {
				ret = false;
			}
		} catch (RuntimeException e) {
			//e.printStackTrace();
		} finally {
			return ret;
		}
	}

	// 查注册表是否与计算出的key相同
	public static boolean checkValue(String version, String hardWareSN,
			String softWareSN) {

		if (checkRegValue(version, hardWareSN, softWareSN)) {
			return true;
		} else {

			reg.clear();
			reg.put("version", version.toLowerCase());
			reg.put("hardwaresn", hardWareSN.toLowerCase());
			reg.put("softwaresn", softWareSN.toLowerCase());

			String temp = "";
			String tempSub = "";

			BufferedReader bReader = null;
			try {
				bReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(file)));
				while ((temp = bReader.readLine()) != null) {
					tempSub = temp.substring(0, temp.indexOf(':'));
					if (!temp.substring(temp.indexOf(':') + 1, temp.length())
							.equals(reg.get(tempSub))
							&& !temp.startsWith("version")) {
						return false;
					}
				}
				return true;
			} catch (Exception e) {
				//e.printStackTrace();
			} finally {
				try {
					if (bReader != null) {
						bReader.close();
					}
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
			return false;
		}

	}

	public static boolean checkRegValue(String version, String hardWareSN,
			String softWareSN) {
		reg.clear();
		reg.put("version", version.toLowerCase());
		reg.put("hardwaresn", hardWareSN.toLowerCase());
		reg.put("softwaresn", softWareSN.toLowerCase());

		RegistryKey r = new RegistryKey(RootKey.HKEY_CURRENT_USER,
				"Software\\dts");
		// Preferences pre = Preferences.systemRoot().node("/dts");
		// for (String str : reg.keySet()) {
		// String regval = pre.get(str, "NULL");
		// if (!regval.equals(reg.get(str)) && !str.equals("version")) {
		// return false;
		// }
		// }
		try {
			for (String str : reg.keySet()) {
				String regval = r.getValue(str).getStringValue();
				if (!regval.equals(reg.get(str)) && !str.equals("version")) {
					return false;
				}
			}
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
		return true;
	}

	// public static void main(String[] args){
	// Registery reg = new Registery();
	// //reg.writeValue();
	// }
}