package softtest.config.java;

import java.io.File;


public class Config
{
	public static String version = "DTSJava7";

	public static String statistics = "stat\\statistics.mdb";

	public static boolean SOURCE14 = false;

	//public static String DEBUGPATH = "D:\\Code\\MyEclipse_workspace\\Result\\JAVATEMP" + File.separatorChar;
	public static String DEBUGPATH = "D:\\Code\\MyEclipse_workspace\\Result\\JAVATEMP1216" + File.separatorChar;

	/** �Ƿ���������Ŀ�����ͼ��״̬��ͼ */
    public static boolean TRACE = false;
   
    public static boolean CFGTRACE = true;
    
//	public static boolean TRACE = false;
	/** �Ƿ����±������Ķ����ļ� */
	public static boolean COMPILEFSM = false;

	/** �Ƿ񽫿���Ϊ�յı������ݽ��Ӻ����ڲ����п��� */
	public static boolean NESTPARAMMAYNULL = false;

	/** ���Ը��� */
	public static boolean DEBUG = false;

	/** ������ʱ���ƣ���λΪ���� */
	//public static long TIMEOUT = 7200000;
	public static long TIMEOUT = 3600000;
	//public static long TIMEOUT = 14000;
	
	/** �Ƿ������Դ��Ϊ����������Ӱ�� */
	public static boolean RL_USEASPARAM = false;

	/** �Ƿ��������±����С��0����� */
	public static boolean OOB_NEG = false;

	/** �Ƿ���ϵ��Ϊ+1��-1�����Բ���ʽ */
	public static boolean LINEAR = false;

	public static boolean TESTING = false;

	/** 0:·�������� 1����ͬ״̬�ϲ�2��������ϲ� */
	public static int PATH_SENSITIVE = 1;

	public static int PATH_LIMIT = 10000000;

	/** true:ʹ�ú���ժҪ false:��ʹ�ú���ժҪ */
	public static boolean USE_SUMMARY = true;

	/** �Ƿ�ʹ�ÿ⺯��ժҪ */
	public static boolean USE_LIBSUMMARY = true;

	/** 0:���ģ�1��Ӣ�� */
	public static int LANGUAGE = 0;

	/** true:������֤���ܴ� false:������֤���ܹر� */
	public final static boolean LOCK = false;

	public final static boolean REGISTER = false;

	public final static boolean PHASE_REGISTER = false; // �Ƿ�Ϊ�ֽ׶�ע��,����Ƿֶ�ע�ᣬ�����ע��REGISTER��Ϊtrue

	public final static int PHASE_NUMUBER = 10; // �׶���

	/**ʹ��������ע��*/
	public final static boolean NETWORK_LOCK = false;
	public final static Nlock_size NLOCK_SIZE = Nlock_size.S54;  //Ĭ��ΪS54

	/** �Ƿ�ʹ����Ȩ�ļ���ע�� wavericq20100510 */

	public final static boolean FILE_LICENSE = false;

	/** �Ƿ�Ϊ���԰� */
	public final static boolean ISTRIAL = false;
	
	/**�Ƿ�Ϊ��������*/
	public final static boolean ISSERVER = false;

	/** ���԰�ÿ��IP������� */
	public static double PERCENT = 20;

	/** ���԰�ÿ��IP��������Ŀ */
	public static int MAXIP = 10;
	
	//stat���ݿ�����
	public static String DB_STAT_PASSWORD = "741852963";
	
	/** ��־�ļ� */
	public static String LOG_FILE="java.log";
	
	public static boolean AUTOCLOSE = false;
	
	/** ���ɴ�·������ sqwang*/
	public static int INFEASIBLE_PATH_ = 0;
	
	
	
}
