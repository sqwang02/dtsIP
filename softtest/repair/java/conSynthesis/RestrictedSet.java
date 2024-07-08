package softtest.repair.java.conSynthesis;

import java.util.ArrayList;

public class RestrictedSet {
	public static String restrictrule(String op,String e1,String e2){
		String cond=null;
		int i,j,l,g,m,n,o,p;
	
		String divide[]=new String[]{"/","%","div","ldiv","fmod","atan2"};
		String logop[]=new String[]{"log","log10","sqrt","_logb","_y0","y1"};
		String as[]=new String[]{"asin","acos"};
		String jyn[]=new String[]{"_jn","_yn"};
		for(i=0;i<divide.length;i++){
			if(op.equals(divide[i]))
				cond= e2+"!=0";
		}
		for(j=0;j<logop.length;j++){
			if(op.equals(logop[j]))
				cond= e1+">0";
			}
		for(l=0;l<as.length;l++){
			if(op.equals(as[l]))
				cond= e1+">=0"+"&&"+e1+"<=1";
			
		}
		if(op.equals("pow"))
			cond= e1+"!=0"+"&&"+e2+">=0";
		for(g=0;g<jyn.length;g++){
			if(op.equals(jyn[g]))
				cond= e1+">=0"+"&&"+e2+">=0";
		}
		if(op.equals("pow"))
			cond= e1+"!=0"+"&&"+e2+">=0";
		return cond;
	}

}
