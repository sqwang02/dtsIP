package softtest.rules.java;

import softtest.callgraph.java.method.*;
import softtest.fsm.java.FSMMachineInstance;

public abstract class AbstractStateMachine {
	abstract public void registerPrecondition(PreconditionListenerSet listeners);
	public void registerPostcondition(PostconditionListenerSet listeners){
		listeners.addListener(DomainPostconditionListener.getInstance());
	}
	abstract public void registerFeature(FeatureListenerSet listeners);
	
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		// TODO Auto-generated method stub	
	}
}
