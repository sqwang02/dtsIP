package softtest.callgraph.java.method;

/**函数摘要*/
public class MethodSummary {
	/**前置条件集合*/
	private PreconditionSet preconditons =new PreconditionSet();
	
	/**函数特征信息集合*/
	private FeatureSet featrues =new FeatureSet();
	
	/**后置条件集合*/
	private PostconditionSet postconditons =new PostconditionSet();
	
	/**构造方法*/
	public MethodSummary() {

	}

	/**获得特征信息集合*/
	public FeatureSet getFeatrues() {
		return featrues;
	}

	/**设置特征信息集合*/
	public void setFeatrues(FeatureSet featrues) {
		this.featrues = featrues;
	}

	/**获得后置条件集合*/
	public PostconditionSet getPostconditons() {
		return postconditons;
	}

	/**设置后置条件集合*/
	public void setPostconditons(PostconditionSet postconditons) {
		this.postconditons = postconditons;
	}

	/**获得前置条件集合*/
	public PreconditionSet getPreconditons() {
		return preconditons;
	}

	/**设置前置条件集合*/
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