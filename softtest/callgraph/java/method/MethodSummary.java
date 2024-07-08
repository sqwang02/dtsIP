package softtest.callgraph.java.method;

/**����ժҪ*/
public class MethodSummary {
	/**ǰ����������*/
	private PreconditionSet preconditons =new PreconditionSet();
	
	/**����������Ϣ����*/
	private FeatureSet featrues =new FeatureSet();
	
	/**������������*/
	private PostconditionSet postconditons =new PostconditionSet();
	
	/**���췽��*/
	public MethodSummary() {

	}

	/**���������Ϣ����*/
	public FeatureSet getFeatrues() {
		return featrues;
	}

	/**����������Ϣ����*/
	public void setFeatrues(FeatureSet featrues) {
		this.featrues = featrues;
	}

	/**��ú�����������*/
	public PostconditionSet getPostconditons() {
		return postconditons;
	}

	/**���ú�����������*/
	public void setPostconditons(PostconditionSet postconditons) {
		this.postconditons = postconditons;
	}

	/**���ǰ����������*/
	public PreconditionSet getPreconditons() {
		return preconditons;
	}

	/**����ǰ����������*/
	public void setPreconditons(PreconditionSet preconditons) {
		this.preconditons = preconditons;
	}

	@Override
	public String toString() {
		StringBuffer buff=new StringBuffer();
		buff.append(preconditons.toString()+"\n");
		buff.append(featrues.toString()+"\n");
		buff.append(postconditons.toString());
		return buff.toString();
	}
	
	
}