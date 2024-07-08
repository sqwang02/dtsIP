package softtest.dts.java;

import softtest.config.java.Config;
import softtest.fsmanalysis.java.*;
import softtest.registery.Authentication;
import softtest.registery.Registery;
import softtest.registery.RegisteryClient;
import softtest.registery.SuccessRe;
import softtest.registery.SysInfo;
import softtest.registery.file.Register;
import softtest.registery.file.RegViewer;
import softtest.registery.file.Reset;

import java.io.*;

import javax.swing.JOptionPane;

import LONGMAI.NoxTimerKey;

public class DTSJava {

	public static void main(String args[]) {
		
		if (Config.FILE_LICENSE) {
			int result = Register.verify();

			if (result == Register.UNREGISTERED) {
				RegViewer.launch();
				return;
			}

			if (result == Register.ERROR) {
				// JOptionPane.showMessageDialog(null, "用户操作不当引起错误", "ERROR!",
				// JOptionPane.ERROR_MESSAGE);
				Reset.launch();
				return;
			}
			SuccessRe.setFL(result);// 2011-07-05

		}

		// wavericq 20100325
		if (Config.NETWORK_LOCK) {
			Authentication au = new Authentication();
			au.initialize();
			boolean result = au.checkIdentity();
//			SuccessRe.setFL(result?1:0);
			SuccessRe.setFL(result?0:1);
			if (!result){
				System.exit(0);
			}
		}

		if (Config.LOCK) {
			NoxTimerKey aNox = new NoxTimerKey();
			int[] keyHandles = new int[8];
			int[] nKeyNum = new int[1];
			int nAppID = 0xFFFFFFFF;
			// 查找加密锁
			/**
			 * 2011-07-05
			 */
			int rightLock=aNox.NoxFind(nAppID, keyHandles, nKeyNum);
            /*此处不受跳转指令影响，保证SUCCESSLOCK开关的值和是否通过加密锁注册的真实情况一致*/
			SuccessRe.setL(rightLock==0?true:false);
			//查找加密锁
	        if( 0 != rightLock) {
				if (softtest.config.java.Config.LANGUAGE == 0) {
					JOptionPane.showMessageDialog(null, "请插入加密锁！");
				} else {
					JOptionPane.showMessageDialog(null,
							"Please insert the encryption lock!");
				}
				return;
			}
		}
		if (Config.REGISTER) {
			/**
			 * 2011-07-05
			 */
			boolean rightRe=Registery.checkRegistery();
        	if (!rightRe) {
				RegisteryClient rc = new RegisteryClient();
				rc.launchFrame();
				rc.setVisible(true);
				return;
			}
        	SuccessRe.setR(rightRe);
		} else {
			if (Config.LOCK) {
				// 检查权限
				/**
				 * 2011-07-05
				 */
				boolean rightLock=SysInfo.checkPermission();
				SuccessRe.setL(rightLock);
				if (!rightLock) {
					return;
				}
			}
		}

		if (args.length != 4 && args.length != 5) {
			return;
		}

		String classpath = "temp";
		String sourcepath = "";
		String defect = null;
		String category = null;
		
		if (args.length == 4) 
		//1 被测工程路径 2 数据库 3 -R 4 工程名
		{
			sourcepath = args[0];			
		} else if (args.length == 5)
		//1 被测工程路径 2 数据库 3 -R 4 库文件 5 工程名
		{
			sourcepath = args[0];
			classpath = classpath + File.pathSeparator + args[3];
		}
		

	/*	if (args.length == 4) {
			classpath = classpath + File.pathSeparator + args[2];
			sourcepath = args[0];
		} else if (args.length == 3) {
			// 如果是eclipse工程，那么利用.classpath寻找lib
			ClasspathParser p = new ClasspathParser(args[0]);
			if (p.hasClasspathFile()) {
				classpath = classpath + File.pathSeparator
						+ p.getEclipseLibPath();
				sourcepath = p.getEclipseSrcPath();
			} else {
				sourcepath = args[0];
				classpath = classpath + File.pathSeparator + args[0];
			}
		} else if (args.length == 5) {
			Config.TESTING = true;
			sourcepath = args[0];
			defect = args[2];
			category = args[3];
		}*/
		
		//dongyk 20120918 将日志名字设置为工程名字
		Log4jInit log=new Log4jInit();
		//String logName=args[args.length-1];
		//if(logName.contains("."))
		//{
		//	logName=logName.substring(0, logName.indexOf("."));
		//}
		//log.init(logName);
		log.init("java");

		if (defect != null && category != null) {
			ProjectAnalysis.loadFSM(defect, category);
		} else {
			ProjectAnalysis.loadFSM();
		}

		// Config.TESTING = true;

		FSMControlFlowData loopdata = new FSMControlFlowData();
		loopdata.getDB().openDataBase(args[1]);
		
		ProjectAnalysis.projectAnalysisByFile(sourcepath, classpath, loopdata);
		
		loopdata.getDB().closeDataBase();
		
		/*
		 * try{ if (!Config.TESTING) { Thread.sleep(15000); } }catch(Exception
		 * e){ // do nothing } System.exit(0);
		 */
	}
	
		
	
}
