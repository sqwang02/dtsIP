package LONGMAI;


public class NoxTimerKey
{
  public native int NoxGetLastError();

  public native int NoxFind(int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2);

  public native int NoxOpen(int paramInt, String paramString);

  public native int NoxReadStorage(int paramInt, byte[] paramArrayOfByte);

  public native int NoxReadMem(int paramInt, byte[] paramArrayOfByte);

  public native int NoxWriteMem(int paramInt, byte[] paramArrayOfByte);

  public native int NoxClose(int paramInt);

  public native int NoxGetUID(int paramInt, char[] paramArrayOfChar);

  public native int NoxGetExpiryDateTime(int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2);

  public native int NoxGetRemnantCount(int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3);

  public native int NoxGenRequestFile(int paramInt, String paramString1, String paramString2);

  public native int NoxUnlock(int paramInt, String paramString);

  static
  {
    System.loadLibrary("NoxTAppJ");
  }
}
