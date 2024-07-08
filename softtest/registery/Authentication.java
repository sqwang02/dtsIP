 package softtest.registery;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

import ca.beq.util.win32.registry.RegistryKey;
import ca.beq.util.win32.registry.RegistryValue;
import ca.beq.util.win32.registry.RootKey;
import ca.beq.util.win32.registry.ValueType;

import softtest.config.java.Config;
import softtest.config.java.Nlock_size;

public class Authentication implements ActionListener
{
	private Identity id = null;
	private SentinelUtils sentinel = null;
	private IdentityCheck check = null;
	private AuthenticationFrame aFrame = null;
	private boolean isRegisted;
	private Object lock;
	private String path;
	
	private final static int basicClientCell = 0x09;
//	private final static String pathSuffix = "\\DTSSoftware\\regInfo.dat";
	
	public Authentication()
	{
		creatFilePath();
		loadIDFromFile();
		if(id == null)
		{
			loadIDFromRegistry();
		}
	}
	
	public void initialize()
	{
		/*if(id == null)
		{
			id = new IdentityObject();
			check = new IdentityReg();
			aFrame = new AuthenticationFrame();
			aFrame.setActionListener(this);
			lock = this;
			synchronized(lock)
			{
				try
				{
					lock.wait();
				}
				catch (InterruptedException e){}
			}
		}*/
	}
	
	public boolean checkIdentity()
	{	
		try
		{
			String message = "";
			
			if(id != null)
			{
				if(check.checkIdentity(id, sentinel, basicClientCell) == 0)
				{
					saveIDToFile();
					saveIDToRegistry();
					return true;
				}
				else
				{
					if(Config.NLOCK_SIZE == Nlock_size.S216)
					{
						check = new IdentityAuthen216();
					}
					if(Config.NLOCK_SIZE == Nlock_size.S54)
					{
						check = new IdentityAuthen54();
					}
					
					isRegisted = true;
					
					if (Config.LANGUAGE == 0)
					{
						message = "您的注册信息有误,请先输入网络锁的IP地址,然后点选序列号,来完成身份验证";
					}
					else
					{
						if (Config.LANGUAGE == 1)
						{
							message = "Welcome to use, enter the IP address of the network lock, and then select a serial number to complete the registration";
						}
					}
				}
			}
			else
			{
				if(Config.NLOCK_SIZE == Nlock_size.S216)
				{
					check = new IdentityReg216();
					id = new IdentityObject216();
				}
				if(Config.NLOCK_SIZE == Nlock_size.S54)
				{
					check = new IdentityReg54();
					id = new IdentityObject54();
				}
				isRegisted = false;
				
				if (Config.LANGUAGE == 0)
				{
					message = "欢迎您的使用,请先输入网络锁的IP地址,然后点选序列号,来完成注册";
				}
				else
				{
					if (Config.LANGUAGE == 1)
					{
						message = "Welcome to use, enter the IP address of the network lock, and then select a serial number to complete the registration";
					}
				}
			}
			
			aFrame = new AuthenticationFrame();
			aFrame.setWarningMessage(message);
			aFrame.setActionListener(this);
			lock = this;
			synchronized(lock) 
			{
				try 
				{
					lock.wait();
				} 
				catch (InterruptedException e) 
				{
				}
			}
			
			int r = check.checkIdentity(id, sentinel,basicClientCell);
			if(r == 0)
			{
				saveIDToFile();
				saveIDToRegistry();
				return true;
			}
			else
			{
				deleteFile();
				deleteRegistryItem();
				
				if(r == 1)
				{
					if (Config.LANGUAGE == 0)
					{
						message = "用户注册信息已清除";
					}
					else
					{
						if (Config.LANGUAGE == 1)
						{
							message = "User registration information has been removed";
						}
					}
				}
				else
				{
					if (Config.LANGUAGE == 0)
					{
						message = "操作失败: ID--" + id.getCellAddress() + "/" + id.getOffset() + "@" +  id.getServerIP() + " [ 错误代码: " + r + "]";
					}
					else
					{
						if (Config.LANGUAGE == 1)
						{
							message = "operation failed: ID--" + id.getCellAddress() + "/" + id.getOffset() + "@" +  id.getServerIP() + " [ errorNum: " + r + "]";
						}
					}
				}
				
				JOptionPane.showMessageDialog(aFrame, message, "ERROR!", JOptionPane.ERROR_MESSAGE);
				
				return false;
			}
				
		}
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(aFrame, ex.getMessage(), "ERROR!", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		finally
		{
			if(aFrame != null)
			{
				aFrame.dispose();
			}
			
			if(sentinel != null)
			{
				try
				{
					sentinel.close();
				}
				catch(Exception ex){}
			}
		}
		
		
	}
	
//	public boolean checkIdentity()
//	{
//		boolean result = false;
//		
//		try
//		{
//			if(id != null && check.checkIdentity(id, sentinel, basicClientCell))
//			{
//				result = true;
//				saveIDToFile();
//				saveIDToRegistry();
//			}
//			
//			else
//			{
//				if (Config.DTS_LANGUAGE == Config.LANG_CHINESE) {
//					String message;
//					if (id == null) {
//						id = new IdentityObject();
//						check = new IdentityReg();
//						isRegisted = false;
//						message = "欢迎您的使用,请先输入网络锁的IP地址,然后点选序列号,来完成注册";
//					} else {
//						check = new IdentityAuthen();
//						isRegisted = true;
//						message = "您的注册信息有误,请先输入网络锁的IP地址,然后点选序列号,来完成身份验证";
//					}
//
//					aFrame = new AuthenticationFrame();
//					aFrame.setWarningMessage(message);
//					aFrame.setActionListener(this);
//					lock = this;
//					synchronized (lock) {
//						try {
//							lock.wait();
//						} catch (InterruptedException e) {
//						}
//					}
//					
//					/*
//					if (id.getCellAddress() > 0
//							&& check.checkIdentity(id, sentinel,
//									basicClientCell)) 
//					*/		
//					if (id.getCell() > 0
//							&& check.checkIdentity(id, sentinel,
//									basicClientCell)) 
//					{
//						result = true;
//						saveIDToFile();
//						saveIDToRegistry();
//					} 
//					else 
//					{
//						result = false;
//						deleteFile();
//						deleteRegistryItem();
//
//						/*
//						if (id.getCellAddress() > 0) 
//						{
//							message = "操作失败：ID--" + id.getCellAddress() + "/"
//									+ id.getServerIP();
//						} 
//						 */
//						if (id.getCell() > 0) 
//						{
//							message = "操作失败：ID--" + id.getCell() + "/"
//									+ id.getServerIP();
//						} 
//						else 
//						{
//							message = "用户注册信息已清除";
//						}
//
//						JOptionPane.showMessageDialog(aFrame, message,
//								"ERROR!", JOptionPane.ERROR_MESSAGE);
//					}
//
//				}
//				if (Config.DTS_LANGUAGE == Config.LANG_ENGLISH) {
//					String message;
//					if (id == null) {
//						id = new IdentityObject();
//						check = new IdentityReg();
//						isRegisted = false;
//						message = "Welcome to use, enter the IP address of the network lock, and then select a serial number to complete the registration";
//					} else {
//						check = new IdentityAuthen();
//						isRegisted = true;
//						message = "Your registration information is incorrect, enter the IP address of the network lock, and then select a serial number to complete the authentication";
//					}
//
//					aFrame = new AuthenticationFrame();
//					aFrame.setWarningMessage(message);
//					aFrame.setActionListener(this);
//					lock = this;
//					synchronized (lock) {
//						try {
//							lock.wait();
//						} catch (InterruptedException e) {
//						}
//					}
//
//					/*
//					if (id.getCellAddress() > 0
//							&& check.checkIdentity(id, sentinel,
//									basicClientCell)) 
//					 */
//					if (id.getCell() > 0
//							&& check.checkIdentity(id, sentinel,
//									basicClientCell)) 
//					{
//						result = true;
//						saveIDToFile();
//						saveIDToRegistry();
//					} else {
//						result = false;
//						deleteFile();
//						deleteRegistryItem();
//
//						/*
//						if (id.getCellAddress() > 0) 
//						{
//							message = "operation failed:ID--" + id.getCellAddress()
//									+ "/" + id.getServerIP();
//						}
//						 */
//						if (id.getCell() > 0) 
//						{
//							message = "operation failed:ID--" + id.getCell()
//									+ "/" + id.getServerIP();
//						} 
//						else 
//						{
//							message = "User registration information has been removed";
//						}
//
//						JOptionPane.showMessageDialog(aFrame, message,
//								"ERROR!", JOptionPane.ERROR_MESSAGE);
//					}
//
//				}
//			}
//		}
//		catch(Exception ex)
//		{
//			JOptionPane.showMessageDialog(aFrame, ex.getMessage(), "ERROR!", JOptionPane.ERROR_MESSAGE);
//		}
//		
//		if(aFrame != null)
//		{
//			aFrame.dispose();
//		}
//		
//		if(sentinel != null)
//		{
//			try
//			{
//				sentinel.close();
//			}
//			catch (Exception e)
//			{
//				
//			}
//		}
//		
//		return result;
//	}
	
	
	/*public boolean checkIdentity()
	{
		boolean result = false;
		
		try
		{
			if(check.checkIdentity(id, sentinel, basicClientCell))
			{
				result = true;
				saveIDToFile();
				saveIDToRegistry();
			}
			else
			{
				deleteFile();
				deleteRegistryItem();
//				JOptionPane.showMessageDialog(aFrame, "操作失败：ID--" + id.getCellAddress() + "/" + id.getServerIP(), "ERROR!", JOptionPane.ERROR_MESSAGE);
				
			}
		}
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(aFrame, ex.getMessage(), "ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		
		if(aFrame != null)
		{
			aFrame.dispose();
		}
		
		if(sentinel != null)
		{
			try
			{
				sentinel.close();
			}
			catch (Exception e)
			{
				
			}
		}
		
		return result;
	}*/
	
	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand().toUpperCase();
		if("IP".equalsIgnoreCase(command))
		{
			enterIPAction();
		}
		if("ADDR".equalsIgnoreCase(command))
		{
			enterAddrAction();
		}
	}
	
	private void enterIPAction()
	{
		String ip = aFrame.getIPField();
		if(checkIP(ip))
		{
			try
			{
				sentinel = SentinelUtils.findSentinel(ip);		//!!!
				
				createAddressChoice();
//				check = new IdentityReg();   //???
				id.setServerIP(ip);
				aFrame.stateAfterIPInput();
			}
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(aFrame, ex.getMessage(), "ERROR!", JOptionPane.ERROR_MESSAGE);
				if(sentinel != null)
				{
					try
					{
						sentinel.close();
					}
					catch (Exception e)
					{
						
					}
				}
			}
		}
		else
		{
			if (Config.LANGUAGE == 0)
				aFrame.setWarningMessage("IP地址格式错误");
			if (Config.LANGUAGE == 1)
				aFrame.setWarningMessage("IP address format error");
		}
	}
	
	private void enterAddrAction()
	{
		try
		{
			String cellStr = aFrame.getAddrChoice();
			int cellAddr = Integer.parseInt(cellStr);
			
			/*
			id.setCellAddress(cellAddr);
			*/
			id.setCellAddress(cellAddr);
		}
		catch(Exception ex)
		{
			//aFrame.setWarningMessage("所用的用户序列号都已被占用");
			
			//id.setCellAddress(-1);
			id.setCellAddress(-4);		//???
			
			
			if (Config.LANGUAGE == 0)
				aFrame.setWarningMessage("清除用户注册信息");
			if (Config.LANGUAGE == 1)
				aFrame.setWarningMessage("Remove user registration information");
		}
		synchronized(lock)
		{
			lock.notify();
		}
	}
	
	private void creatFilePath()
	{
		/*path = "C:\\Program Files\\Common Files" + pathSuffix;
		
		RegistryKey regKey = new RegistryKey(RootKey.HKEY_LOCAL_MACHINE, 
				"SOFTWARE\\Microsoft\\Windows\\CurrentVersion");
		Iterator i = regKey.values();
		while(i.hasNext())
		{
			RegistryValue regValue = (RegistryValue)i.next();
			if(regValue.getName().equals("CommonFilesDir"))
			{
				path = regValue.getStringValue() + pathSuffix;
				break;
			}
		}
		
		int index = path.lastIndexOf("\\");
		String p = path.substring(0, index);
		File f = new File(p);
		
		if(!f.exists())
		{
			f.mkdirs();
			
		}*/
		
		path = "..\\..\\regInfo.dat";
		File reg = new File(path);
		
		try
		{
			path = reg.getCanonicalPath();
		}
		catch (IOException e)
		{
			//path = System.getProperty("user.dir");
		}
		
	}
	
	private void loadIDFromFile()
	{
		FileInputStream fileInput = null;
		ObjectInputStream objInput = null;
		try
		{
			fileInput = new FileInputStream(path);
			objInput = new ObjectInputStream(fileInput);
			
			if(Config.NLOCK_SIZE == Nlock_size.S216)
			{
				id = (IdentityObject216)objInput.readObject();
				check = new IdentityAuthen216();
			}
			if(Config.NLOCK_SIZE == Nlock_size.S54)
			{
				id = (IdentityObject54)objInput.readObject();
				check = new IdentityAuthen54();
			}
			
			
			//sentinel = new SentinelUtils(id.getServerIP());
			sentinel = SentinelUtils.findSentinel(id.getServerIP());
		}
		catch (Exception e)
		{
			id = null;
			//sentinel = null;    //???
		}
		finally
		{
			try
			{
				if(objInput != null)
				{
					objInput.close();
					fileInput = null;
				}
				if(fileInput != null)
				{
					fileInput.close();
				}
			}
			catch(IOException ex){}
		}
		
		if(id == null)			//???
		{
			deleteFile();
		}
	}
	
	private void loadIDFromRegistry()
	{
		RegistryKey dtsKey = new RegistryKey(RootKey.HKEY_CURRENT_USER, "Software\\DTSTest");
		if(dtsKey.exists()&& dtsKey.hasValue("regInfo"))
		{
			RegistryValue dtsValue = dtsKey.getValue("regInfo");
			byte[] data = (byte[])dtsValue.getData();
			
			ByteArrayInputStream arrayInput = null;
			ObjectInputStream objInput = null;
			try
			{
				arrayInput = new ByteArrayInputStream(data);
				objInput = new ObjectInputStream(arrayInput);
				
				if(Config.NLOCK_SIZE == Nlock_size.S216)
				{
					id = (IdentityObject216)objInput.readObject();
					check = new IdentityAuthen216();
				}
				if(Config.NLOCK_SIZE == Nlock_size.S54)
				{
					id = (IdentityObject54)objInput.readObject();
					check = new IdentityAuthen54();
				}
				
				//sentinel = new SentinelUtils(id.getServerIP());
				sentinel = SentinelUtils.findSentinel(id.getServerIP());
				
			}
			catch (Exception e)
			{
				id = null;
				//sentinel = null;			//???
			}
			finally
			{
				try
				{
					if(objInput != null)
					{
						objInput.close();
					}
				}
				catch (IOException e){}
			}
		}
		
	}
	
	private void deleteFile()
	{
		File file = new File(path);
		if(file.exists() && file.isFile())
		{
			file.delete();
		}
	}
	
	private void deleteRegistryItem()
	{
		RegistryKey dtsKey = new RegistryKey(RootKey.HKEY_CURRENT_USER, "Software\\DTSTest");
		if(dtsKey.exists())
		{
			dtsKey.delete();
		}
	}
	
	private void saveIDToFile()
	{
		FileOutputStream fileOutput = null;
		ObjectOutputStream objOutput = null;
		FileOutputStream txtOutput = null;
		Properties prop = null;
		try
		{
			fileOutput = new FileOutputStream(path);
			objOutput = new ObjectOutputStream(fileOutput);
			objOutput.writeObject(id);
			
			int index = path.lastIndexOf("\\");
			String txtPath = path.substring(0, index) + "\\info.txt";
			txtOutput = new FileOutputStream(txtPath);
			prop = new Properties();
			prop.setProperty("server_IP", id.getServerIP());
			
			
			/*
			prop.setProperty("serial_number",Integer.toString(id.getCellAddress()));
			*/
			prop.setProperty("serial_number",Integer.toString(id.getCellAddress()));
			prop.setProperty("offset",Integer.toString(id.getOffset()));
			
			
			prop.store(txtOutput, null);
		}
		catch (IOException e){}
		finally
		{
			try
			{
				if(objOutput != null)
				{
					objOutput.close();
					fileOutput = null;
				}
				if(fileOutput != null)
				{
					fileOutput.close();
				}
				if(txtOutput!= null)
				{
					txtOutput.close();
				}
			}
			catch(IOException ex){}
		}
	}
	
	private void saveIDToRegistry()
	{
		ByteArrayOutputStream arrayOutput = null;
		ObjectOutputStream objOutput = null;
		try
		{
			arrayOutput = new ByteArrayOutputStream();
			objOutput = new ObjectOutputStream(arrayOutput);
			objOutput.writeObject(id);
			byte[] data = arrayOutput.toByteArray();
			
			RegistryKey dtsKey = new RegistryKey(RootKey.HKEY_CURRENT_USER, "Software\\DTSTest");
			if(!dtsKey.exists())
			{
				dtsKey.create();
			}
			RegistryValue dtsValue = new RegistryValue("regInfo",ValueType.REG_BINARY,data);
			dtsKey.setValue(dtsValue);
		}
		catch(Exception ex){}
		finally
		{
			try
			{
				if(objOutput != null)
				{
					objOutput.close();
				}
			}
			catch(IOException ex){}
		}
	}
	
	private boolean checkIP(String ip)
	{
		boolean result;
		try
		{
			String regex = "(0|([1-9]\\d?)|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))" + "\\."
						+ "(0|([1-9]\\d?)|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))" + "\\."
						+ "(0|([1-9]\\d?)|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))" + "\\."
						+ "(0|([1-9]\\d?)|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))";
			result = ip.matches(regex);
		}
		catch(Exception ex)
		{
			result = false;
		}
		return result;
	}
	
	
	//???
	private void createAddressChoice() throws Exception
	{	
		int numClient = sentinel.readData(basicClientCell);
		int data = 0;
		for(int i=1; i<=numClient; i++)
		{
			
			if(Config.NLOCK_SIZE == Nlock_size.S54)
			{
				data = sentinel.readData(i + basicClientCell);
			}
			if(Config.NLOCK_SIZE == Nlock_size.S216)
			{
				int addr = (i-1)/4 + 1;
				int offset = (i-1)%4*4;
				data = sentinel.readData(addr + basicClientCell);
				data = data >> offset;
				data = data & 0xF;
			}
			
			
			if(data == 0 && !isRegisted)
			{
				aFrame.addAddressItem(i);
			}
			
			if(data != 0 && isRegisted)
			{
				aFrame.addAddressItem(i);
			}
			
		}
		
		if(isRegisted)
		{
			if(Config.LANGUAGE == 0)
			{
				aFrame.addAddressItem("清除用户信息");
			}
			if(Config.LANGUAGE == 1)
			{
				aFrame.addAddressItem("clear_reg_info");
			}
		}
	}
}
