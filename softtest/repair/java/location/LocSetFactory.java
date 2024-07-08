package softtest.repair.java.location;


import softtest.repair.java.conSynthesis.IConditionSynthesis;

public class LocSetFactory{
	public ILocation getRepairLocation(String category){
	String repairLocation ="softtest.repair.java.location."+category+"RepairLocation";
	ILocation lo = null;
	try{
		lo = (ILocation)(Class.forName(repairLocation).newInstance());
	}catch(Exception e){
		e.printStackTrace();
	}
	return lo;
	}

}
