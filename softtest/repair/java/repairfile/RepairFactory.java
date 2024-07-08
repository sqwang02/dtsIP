package softtest.repair.java.repairfile;

import softtest.repair.java.conSynthesis.IConditionSynthesis;

public class RepairFactory {
	public IDefectRepair getRepair(String category){
		String repairName = "softtest.repair.java.repairfile"+category+"Repair";
		IDefectRepair ics = null;
		try{
			ics = (IDefectRepair)(Class.forName(repairName).newInstance());
		}catch(Exception e){
			e.printStackTrace();
		}
		return ics;
	}
	

}
