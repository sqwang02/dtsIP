package softtest.registery;

import softtest.config.java.Config;
/**
 * 
 * @author chenhonghe
 * 2011-07-05
 *
 */

public class SuccessRe {
    /*SUCCESSRREGISTER初始化为Config.REGISTER的反，开启Config.REGISTERd的情况下，在注册成功时通过setR方法设为true*/
	private static boolean SUCCESSRREGISTER=!Config.REGISTER;
/*SUCCESSLOCK初始化为Config.LOCK的反，开启Config.LOCK时，在注册成功时通过setL方法设为true*/
	private static boolean SUCCESSLOCK=!Config.LOCK;
	/*SUCCESSFFILE_LICENSE初始化为Config.FILE_LICENSE的反，开启Config.FILE_LICENSE时，在注册成功时通过setFL方法设为true*/
    private static boolean SUCCESSFFILE_LICENSE=!Config.FILE_LICENSE;
	public static void  setL(boolean able){
		SUCCESSLOCK=able;
	}
	public static void  setR(boolean able){
		SUCCESSRREGISTER=able;
	}
	public static void  setFL(int able){
		SUCCESSFFILE_LICENSE=(able==0?true:false);
	}
    /*将该方法添加到各检查点，任意注册不成功信息将导致程序退出*/
	public static void check() {
		if(!SUCCESSRREGISTER||!SUCCESSLOCK||!SUCCESSFFILE_LICENSE){
			System.exit(0);
		}
	}
}

