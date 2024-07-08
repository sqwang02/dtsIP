package softtest.repair.java.conSynthesis;

public class ConSysFactory {
	public IConditionSynthesis getRepairConSys(String category){
		String repairConSysName = "softtest.repair.java.conSynthesis."+category+"RepairConSys";
		IConditionSynthesis ics = null;
		try{
			ics = (IConditionSynthesis)(Class.forName(repairConSysName).newInstance());
		}catch(Exception e){
			e.printStackTrace();
		}
		return ics;
	}
}
