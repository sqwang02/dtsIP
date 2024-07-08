package softtest.registery;

public interface IdentityCheck
{
	public int checkIdentity(Identity id, SentinelUtils sentinel, int base) throws Exception;
	//public boolean checkIdentity(IdentityObject id, SentinelUtils sentinel, int base) throws Exception;
}
