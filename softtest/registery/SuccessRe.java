package softtest.registery;

import softtest.config.java.Config;
/**
 * 
 * @author chenhonghe
 * 2011-07-05
 *
 */

public class SuccessRe {
    /*SUCCESSRREGISTER��ʼ��ΪConfig.REGISTER�ķ�������Config.REGISTERd������£���ע��ɹ�ʱͨ��setR������Ϊtrue*/
	private static boolean SUCCESSRREGISTER=!Config.REGISTER;
/*SUCCESSLOCK��ʼ��ΪConfig.LOCK�ķ�������Config.LOCKʱ����ע��ɹ�ʱͨ��setL������Ϊtrue*/
	private static boolean SUCCESSLOCK=!Config.LOCK;
	/*SUCCESSFFILE_LICENSE��ʼ��ΪConfig.FILE_LICENSE�ķ�������Config.FILE_LICENSEʱ����ע��ɹ�ʱͨ��setFL������Ϊtrue*/
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
    /*���÷�����ӵ������㣬����ע�᲻�ɹ���Ϣ�����³����˳�*/
	public static void check() {
		if(!SUCCESSRREGISTER||!SUCCESSLOCK||!SUCCESSFFILE_LICENSE){
			System.exit(0);
		}
	}
}

