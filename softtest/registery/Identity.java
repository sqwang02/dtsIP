package softtest.registery;

public interface Identity
{
	public void setSignature(String signature);
	
	public String getSignature();
	
	public String getServerIP();
	
	public void setServerIP(String serverIP);
	
	public int getCellAddress();
	
	public void setCellAddress(int cellAddress);
	
	public int getOffset();
	
	
}
