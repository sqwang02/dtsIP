package softtest.config.java;

import java.io.File;


public class Config
{
	public static String version = "DTSJava7";

	public static String statistics = "stat\\statistics.mdb";

	public static boolean SOURCE14 = false;

	//public static String DEBUGPATH = "D:\\Code\\MyEclipse_workspace\\Result\\JAVATEMP" + File.separatorChar;
	public static String DEBUGPATH = "D:\\Code\\MyEclipse_workspace\\Result\\JAVATEMP1216" + File.separatorChar;

	/** 是否输出函数的控制流图，状态机图 */
    public static boolean TRACE = false;
   
    public static boolean CFGTRACE = true;
    
//	public static boolean TRACE = false;
	/** 是否重新编译规则的动作文件 */
	public static boolean COMPILEFSM = false;

	/** 是否将可能为空的变量传递进子函数内部进行考虑 */
	public static boolean NESTPARAMMAYNULL = false;

	/** 调试跟踪 */
	public static boolean DEBUG = false;

	/** 分析超时限制，单位为毫秒 */
	//public static long TIMEOUT = 7200000;
	public static long TIMEOUT = 3600000;
	//public static long TIMEOUT = 14000;
	
	/** 是否忽略资源作为函数参数的影响 */
	public static boolean RL_USEASPARAM = false;

	/** 是否考虑数组下标可能小于0的情况 */
	public static boolean OOB_NEG = false;

	/** 是否处理系数为+1或-1的线性不等式 */
	public static boolean LINEAR = false;

	public static boolean TESTING = false;

	/** 0:路径不敏感 1：相同状态合并2：不允许合并 */
	public static int PATH_SENSITIVE = 1;

	public static int PATH_LIMIT = 10000000;

	/** true:使用函数摘要 false:不使用函数摘要 */
	public static boolean USE_SUMMARY = true;

	/** 是否使用库函数摘要 */
	public static boolean USE_LIBSUMMARY = true;

	/** 0:中文；1：英文 */
	public static int LANGUAGE = 0;

	/** true:加密验证功能打开 false:加密验证功能关闭 */
	public final static boolean LOCK = false;

	public final static boolean REGISTER = false;

	public final static boolean PHASE_REGISTER = false; // 是否为分阶段注册,如果是分段注册，必须把注册REGISTER设为true

	public final static int PHASE_NUMUBER = 10; // 阶段数

	/**使用网络锁注册*/
	public final static boolean NETWORK_LOCK = false;
	public final static Nlock_size NLOCK_SIZE = Nlock_size.S54;  //默认为S54

	/** 是否使用授权文件来注册 wavericq20100510 */

	public final static boolean FILE_LICENSE = false;

	/** 是否为测试版 */
	public final static boolean ISTRIAL = false;
	
	/**是否为服务器版*/
	public final static boolean ISSERVER = false;

	/** 测试版每类IP输出比例 */
	public static double PERCENT = 20;

	/** 测试版每类IP最大输出数目 */
	public static int MAXIP = 10;
	
	//stat数据库密码
	public static String DB_STAT_PASSWORD = "741852963";
	
	/** 日志文件 */
	public static String LOG_FILE="java.log";
	
	public static boolean AUTOCLOSE = false;
	
	/** 不可达路径数量 sqwang*/
	public static int INFEASIBLE_PATH_ = 0;
	
	
	
}
