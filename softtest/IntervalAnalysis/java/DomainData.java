package softtest.IntervalAnalysis.java;

import softtest.cfg.java.VexNode;

/** ���ڱ��ʽ��������ݴ��ݣ��μ�ExpressionDomainVisitor */
public class DomainData {
	private VexNode currentVex = null;
	
	/** ������ */
    public ClassType type=null;
    
    /** �� */
    public Object domain=null;
    
    /** �����Ƿ���������õı�־ */
    public boolean sideeffect=true;
    
    /** �Ƿ��һ�εĳ����������� */
    public boolean firstcal=false;

	/**
	 * @return the currentVex
	 */
	public VexNode getCurrentVex() {
		return currentVex;
	}

	/**
	 * @param currentVex the currentVex to set
	 */
	public void setCurrentVex(VexNode currentVex) {
		this.currentVex = currentVex;
	}
	
	/** �չ��췽�� */
	public DomainData() {
		// do nothing
	}

	/** ָ����ǰ�������ڵ� */
	public DomainData(VexNode currentVex) {
		this.currentVex = currentVex;
	}
}
