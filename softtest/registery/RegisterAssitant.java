package softtest.registery;
import java.util.Iterator;

import ca.beq.util.win32.registry.RegistryKey;
import ca.beq.util.win32.registry.RegistryValue;
import ca.beq.util.win32.registry.RootKey;
import ca.beq.util.win32.registry.ValueType;

public abstract class RegisterAssitant {
	// 生RegistryKey,例如：str="Software\\Test"
	public static RegistryKey createRegistryKey(String str) {

		RegistryKey r = new RegistryKey(RootKey.HKEY_CURRENT_USER, str);
		if (!r.exists()) {
			r.create();
		}
		return r;
	}

	public static RegistryKey createRegistryKey(RootKey rootKey, String str) {
		RegistryKey r = new RegistryKey(rootKey, str);
		if (!r.exists()) {
			r.create();
		}
		return r;
	}

	// 生成RegistryValue
	public static RegistryValue createRegistryValue(String key, String value) {
		return new RegistryValue(key, value);
	}

	public static RegistryValue createRegistryValue(String key, ValueType type,
			String value) {
		return new RegistryValue(key, type, value);
	}

	// 写注册表
	public static void writeRegister(RegistryKey rk, RegistryValue rv) {
		rk.setValue(rv);
	}

	// 向rv中追加注册信息
	public static RegistryValue addToRegistryValue(RegistryValue rv,
			String key, ValueType type, String value) {
		rv.setName(key);
		rv.setType(type);
		rv.setData(value);
		return rv;
	}

	public static RegistryValue addToRegistryValue(RegistryValue rv,
			String key, String value) {
		rv.setName(key);
		rv.setType(ValueType.REG_SZ);
		rv.setData(value);
		return rv;
	}

	public static void test() {
//		RegistryKey r = RegisterAssitant.createRegistryKey("Software\\Test");
//
//		RegistryValue v = RegisterAssitant.createRegistryValue("dts_inf", "discinfo");
//
//		RegisterAssitant.writeRegister(r, v);
//
//		RegisterAssitant.addToRegistryValue(v, "Mac_inf", "10101025");
//
//		RegisterAssitant.writeRegister(r, v);
//
//		Iterator i = r.values();
//		while (i.hasNext()) {
//			v = (RegistryValue) i.next();
//			System.out.println(v.toString());
//		}
		//r.delete();

	}

}
