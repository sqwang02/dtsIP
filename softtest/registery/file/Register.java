package softtest.registery.file;

import java.io.File;

import softtest.registery.HardDiskUtils;

public class Register
{
	private RegIdentity license;

	private RegIdentity fBackup;

	private RegIdentity rBackup;

	private static String id = HardDiskUtils.getSysInfo().trim();

	private static Register reg = new Register();

	public static final int CORRECT = 0;

	public static final int ERROR = 1;

	public static final int UNREGISTERED = 2;

	public static int verify()
	{
		return reg.doVerify();
	}

	public static String generateSN()
	{
		return reg.doGenerateSN();
	}

	public static boolean login(File file)
	{
		return reg.doLogin(file);
	}

	private Register()
	{
		try
		{
			license = (RegIdentity) RegUtils.loadFromFile(
					RegConstants.LICENSE_FILE_PATH_0,
					RegConstants.LICENSE_FILE_NAME_0);
		}
		catch (Exception ex)
		{
			license = new RegIdentity(id, 0, RegIdentity.NEVER);
		}
		try
		{
			rBackup = (RegIdentity) RegUtils.loadFromRegistry(
					RegConstants.LICENSE_REG_KEY,
					RegConstants.LICENSE_REG_VALUE);
		}
		catch (Exception ex)
		{
			rBackup = new RegIdentity(id, 0, RegIdentity.NEVER);
		}
		try
		{
			fBackup = (RegIdentity) RegUtils.loadFromFile(
					RegConstants.LICENSE_FILE_PATH_1,
					RegConstants.LICENSE_FILE_NAME_1);
		}
		catch (Exception ex)
		{
			fBackup = new RegIdentity(id, 0, RegIdentity.NEVER);
		}
	}

	private int doVerify()
	{
		int result = CORRECT;

		if (license.getSeq() == RegIdentity.NEVER)
		{
			result = UNREGISTERED;
		}

		if (license.getNum() <= 0)
		{
			result = UNREGISTERED;
		}

		if (!id.equalsIgnoreCase(license.getID()))
		{
			result = ERROR;
		}

		if (!license.equals(fBackup) || !license.equals(rBackup))
		{
			result = ERROR;
		}

		if (result == CORRECT)
		{
			license.updateNum();
			updateRegState();
		}

		return result;
	}

	private String doGenerateSN()
	{
		return (fBackup.equals(rBackup)) ? id + (fBackup.getSeq() + 1) : null;
	}

	private boolean doLogin(File file)
	{
		boolean result = true;

		RegIdentity candidate;
		try
		{
			candidate = (RegIdentity) RegUtils.loadFromFile(file);
		}
		catch (Exception ex)
		{
			candidate = new RegIdentity("", 0, RegIdentity.NEVER);
		}

		if (!fBackup.equals(rBackup))
		{
			result = false;
		}

		if (!id.equalsIgnoreCase(candidate.getID()))
		{
			result = false;
		}

		if (candidate.getSeq() != fBackup.getSeq() + 1)
		{
			result = false;
		}

		if (result)
		{
			license = candidate;
			updateRegState();
		}

		return result;
	}

	private void updateRegState()
	{
		RegUtils.saveToFile(license, RegConstants.LICENSE_FILE_PATH_0,
				RegConstants.LICENSE_FILE_NAME_0);
		RegUtils.saveToFile(license, RegConstants.LICENSE_FILE_PATH_1,
				RegConstants.LICENSE_FILE_NAME_1);
		RegUtils.saveToRegistry(license, RegConstants.LICENSE_REG_KEY,
				RegConstants.LICENSE_REG_VALUE);
	}
}
