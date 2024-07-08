package softtest.registery.file;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import ca.beq.util.win32.registry.RegistryKey;
import ca.beq.util.win32.registry.RegistryValue;
import ca.beq.util.win32.registry.RootKey;
import ca.beq.util.win32.registry.ValueType;

class RegUtils
{

	public static Object loadFromFile(String path, String name)
			throws Exception
	{
		FileInputStream fin = null;
		CipherInputStream cin = null;
		ObjectInputStream oin = null;
		try
		{
			File file = new File(path, name);
			fin = new FileInputStream(file);
			Cipher cipher = RegEncoder.deCipher();
			cin = new CipherInputStream(fin, cipher);
			oin = new ObjectInputStream(cin);
			// oin = new ObjectInputStream(fin);
			return oin.readObject();
		}
		finally
		{
			try
			{
				if (oin != null)
					oin.close();
				else
					if (cin != null)
						cin.close();
					else
						if (fin != null)
							fin.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	public static Object loadFromFile(File file) throws Exception
	{
		FileInputStream fin = null;
		CipherInputStream cin = null;
		ObjectInputStream oin = null;
		try
		{
			fin = new FileInputStream(file);
			Cipher cipher = RegEncoder.deCipher();
			cin = new CipherInputStream(fin, cipher);
			oin = new ObjectInputStream(cin);
			// oin = new ObjectInputStream(fin);
			return oin.readObject();
		}
		finally
		{
			try
			{
				if (oin != null)
					oin.close();
				else
					if (cin != null)
						cin.close();
					else
						if (fin != null)
							fin.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	public static void saveToFile(Object data, String path, String name)
	{
		File file = new File(path);
		if (!file.exists())
			file.mkdirs();

		FileOutputStream fout = null;
		CipherOutputStream cout = null;
		ObjectOutputStream out = null;
		try
		{
			fout = new FileOutputStream(new File(file, name));
			Cipher cipher = RegEncoder.enCipher();
			cout = new CipherOutputStream(fout, cipher);
			out = new ObjectOutputStream(cout);
			// out = new ObjectOutputStream(fout);
			out.writeObject(data);
		}
		catch (Exception ex)
		{
		}
		finally
		{
			try
			{
				if (out != null)
					out.close();
				else
					if (cout != null)
						cout.close();
					else
						if (fout != null)
							fout.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	public static Object loadFromRegistry(String key, String value)
			throws Exception
	{
		RegistryKey dtsKey = new RegistryKey(RootKey.HKEY_CURRENT_USER, key);

		if (!dtsKey.exists() || !dtsKey.hasValue(value))
			throw new Exception();
		// return null;

		RegistryValue dtsValue = dtsKey.getValue(value);
		byte[] data = (byte[]) dtsValue.getData();

		ByteArrayInputStream ain = null;
		CipherInputStream cin = null;
		ObjectInputStream oin = null;
		try
		{
			ain = new ByteArrayInputStream(data);
			Cipher cipher = RegEncoder.deCipher();
			cin = new CipherInputStream(ain, cipher);
			oin = new ObjectInputStream(cin);
			// oin = new ObjectInputStream(ain);
			return oin.readObject();
		}
		finally
		{
			try
			{
				if (oin != null)
					oin.close();
				else
					if (cin != null)
						cin.close();
					else
						if (ain != null)
							ain.close();
			}
			catch (Exception ex)
			{
			}
		}

	}

	public static void saveToRegistry(Object data, String key, String value)
	{
		ByteArrayOutputStream aout = null;
		CipherOutputStream cout = null;
		ObjectOutputStream out = null;
		boolean flag = false;
		try
		{
			aout = new ByteArrayOutputStream();
			Cipher cipher = RegEncoder.enCipher();
			cout = new CipherOutputStream(aout, cipher);
			out = new ObjectOutputStream(cout);
			// out = new ObjectOutputStream(aout);
			out.writeObject(data);

			flag = true;
		}
		catch (Exception ex)
		{
		}
		finally
		{
			try
			{
				if (out != null)
					out.close();
				else
					if (cout != null)
						cout.close();
					else
						if (aout != null)
							aout.close();
			}
			catch (Exception ex)
			{
				// flag = false;
			}
		}

		if (flag)
		{
			byte[] b = aout.toByteArray();
			RegistryKey dtsKey = new RegistryKey(RootKey.HKEY_CURRENT_USER, key);
			if (!dtsKey.exists())
			{
				dtsKey.create();
			}
			RegistryValue dtsValue = new RegistryValue(value,
					ValueType.REG_BINARY, b);
			dtsKey.setValue(dtsValue);
		}

	}

	private static final String WIN_EXEC_COMMOND = "cmd /c set SystemRoot";

	public static String getWinSystemRoot()
	{
		String result = null;

		BufferedReader br = null;
		try
		{
			Process p = Runtime.getRuntime().exec(WIN_EXEC_COMMOND);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = br.readLine();
			if (line != null)
			{
				int index = line.indexOf("=");
				result = line.substring(index + 1);
			}

		}
		catch (Exception ex)
		{
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();
			}
			catch (Exception ex)
			{
			}
		}

		return result;

	}

	public static String getLinuxSystemRoot()
	{
		return null;
	}

}
