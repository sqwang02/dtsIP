package softtest.repair.java;

import java.sql.*;
import java.util.*;
import softtest.repair.java.DefectRepair;

public class DefectRepairTest {
	public static void main(String[] args){
		//@SuppressWarnings("static-access")
		readFileACCESS rmf = new readFileACCESS();
		rmf.readDefectInformation(args[0]);
		
	}
	//@SuppressWarnings({ "unchecked", "rawtypes" })
	
	
	
}

