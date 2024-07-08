package softtest.registery;

public class SentinelSuperpro
{
	/*constants used in various SSP APIs. For further details refer to the pdf file "SuperProJavaInterface.pdf"*/
	   //Protocol constants
	   final public static int NSPRO_TCP_PROTOCOL     = 1;
	   final public static int NSPRO_IPX_PROTOCOL     = 2;
	   final public static int NSPRO_NETBEUI_PROTOCOL = 4;
	   final public static int NSPRO_SAP_PROTOCOL     = 8;
	   
	   final public  static int SP_TERM_SERV_CHECK_ON = 1;
	   final public  static int SP_TERM_SERV_CHECK_OFF= 0;

	   //EnumServer constants
	   final public static int NSPRO_RET_ON_FIRST           = 1;
	   final public static int NSPRO_GET_ALL_SERVERS        = 2;
	   final public static int NSPRO_RET_ON_FIRST_AVAILABLE = 4;

	   //heartbeat constants
	   final public static long MAX_HEARTBEAT           = 2592000;
	   final public static long MIN_HEARTBEAT           = 60;
	   final public static int INFINITE_HEARTBEAT      = 0xFFFFFFFF; //==-1
	   final public static String RNBO_STANDALONE       = "RNBO_STANDALONE";
	   final public static String RNBO_SPN_LOCAL        = "RNBO_SPN_LOCAL";
	   final public static String RNBO_SPN_DRIVER       = "RNBO_SPN_DRIVER";
	   final public static String RNBO_SPN_BROADCAST    = "RNBO_SPN_BROADCAST";
	   final public static String RNBO_SPN_ALL_MODES    = "RNBO_SPN_ALL_MODES";
	   final public static String RNBO_SPN_SERVER_MODES = "RNBO_SPN_SERVER_MODES"; 

	   final public static int RNBO_SUCCESS = 0;

	   final public static int MAX_ADDR_LEN  = 32;
	   final public static int MAX_NAME_LEN  = 64;
	   final public static int API_PACKET_SZ = 1028;//APIPACKET size since it is in Byte
	   
	   

	   /* SSP APIs*/
	   public static native int RNBOsproFormatPacket(byte[] apiPacket,/*IN*/
	                                          int apiPacketSize);/*IN*/

	   public static native int RNBOsproInitialize(byte[] apiPacket);/*IN*/

	   public static native int RNBOsproFindFirstUnit(byte[] apiPacket,/*IN*/
	                                           int developerID);/*IN*/

	   public static native int RNBOsproFindNextUnit(byte[] apiPacket);/*IN*/

	   public static native int RNBOsproRead(byte[] apiPacket,/*IN*/
	                                  int address,/*IN*/
	                                  int[] data);/*OUT*/

	   public static native int RNBOsproExtendedRead(byte[] apiPacket,  /*IN*/
	                                          int address,/*IN*/
	                                          int[] data,/*OUT*/
	                                          byte[] accessCode);/*OUT*/

	   public static native int RNBOsproWrite(byte[] apiPacket, /*IN*/
	                                   int writePassword,/*IN*/
	                                   int address,/*IN*/
	                                   int data,/*IN*/
	                                   byte accessCode);/*IN*/

	   public static native int RNBOsproOverwrite(byte[] apiPacket,/*IN*/
	                                       int writePassword,/*IN*/
	                                       int overwritePassword1,/*IN*/
	                                       int overwritePassword2,/*IN*/
	                                       int address,/*IN*/
	                                       int data,/*IN*/
	                                       byte accessCode);/*IN*/

	   public static native int RNBOsproDecrement(byte[] apiPacket,/*IN*/
	                                       int writePassword,/*IN*/
	                                       int address);/*IN*/

	   public static native int RNBOsproActivate(byte[] apiPacket,/*IN*/
	                                      int writePassword,/*IN*/
	                                      int activatePassword1,/*IN*/
	                                      int activatePassword2,/*IN*/
	                                      int address);/*IN*/

	   public static native int RNBOsproQuery(byte[] apiPacket,/*IN*/
	                                   int address,/*IN*/
	                                   char[] queryData,/*IN*/
	                                   char[] response,/*OUT*/
	                                   long[] response32,/*OUT*/
	                                   int length);/*IN*/

	   public static native int RNBOsproGetFullStatus(byte[] apiPacket);/*IN*/

	   public static native int RNBOsproGetVersion(byte[] apiPacket,/*IN*/
	                                        byte[] majVer,/*OUT*/
	                                        byte[] minVer,/*OUT*/
	                                        byte[] rev,/*OUT*/
	                                        byte[] osDrvrType)/*OUT*/;

	   public static native int RNBOsproSetContactServer(byte[] apiPacket,/*IN*/
	                                              String serverName);/*IN*/

	   public static native int RNBOsproSetSharedLicense(byte[] apiPacket/*IN*/,
	                                              int    shareMainLic,/*IN*/
	                                              int    shareSubLic); /*IN*/

	   public static native int RNBOsproGetContactServer(byte[] apiPacket,/*IN*/
	                                              StringBuffer serverName,/*OUT*/
	                                              int srvrNameLen);/*IN*/

	   public static native int RNBOsproGetSubLicense(byte[] apiPacket,/*IN*/
	                                           int address);/*IN*/

	   public static native int RNBOsproReleaseLicense(byte[] apiPacket,/*IN*/
	                                            int address,/*IN*/
	                                            int[] NumSubLicense);/*INOUT*/

	   public static native int RNBOsproGetHardLimit(byte[] apiPacket,/*IN*/
	                                          int[] hardLmt);/*OUT*/

	   public static native int RNBOsproSetHeartBeat(byte[] apiPacket,/*IN*/
	                                          int heartBeat);/*IN*/

	   public static native int RNBOsproSetProtocol(byte[] apiPacket,/*IN*/
	                                         int protocol);/*IN*/

	   public static native int RNBOsproEnumServer(int EnumFlag,/*IN*/
	                                        int DevId,/*IN*/
	                                        JSrvrInfo[] srvrInfo,/*OUT*/
	                                        int[] numSrvr);/*INOUT*/

	   public static native int RNBOsproGetKeyInfo(byte[] apiPacket,/*IN*/
	                                        int devId,/*IN*/
	                                        int keyIndex,/*IN*/
	                                        softtest.registery.JKeyInfo keyInfo);/*OUT*/

	   public static native void RNBOsproCleanup();

	   public static native int RNBOsproGetKeyType(byte[] apiPacket /*IN*/,
	                                        int[]  keyFamily /*OUT*/,
	                                        int[]  keyFormFactor/*OUT*/,
	                                        int[]  keyMemorySize/*OUT*/);

	   public static native int RNBOsproCheckTerminalService(byte[] apiPacket,/*IN*/
	                                         int termserv);/*IN*/
	                                         
	   static
	   {
	      System.loadLibrary("sxjdk"); //load the JNI library
	   }
}
