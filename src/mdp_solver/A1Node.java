package mdp_solver;

import java.util.ArrayList;
import java.util.List;

import problem.Action;
import simulator.State;

public class A1Node extends Node {
	
	List<Node> outcomeNodes = new ArrayList<>();
	
	double[] moveProbs;

	public A1Node(State nodeState, Node parentNode, Action action, MDPSolver mdp, double[] moveProbs) {
		super(nodeState, parentNode, action, mdp);
		generateOutcomeNodes();
		this.moveProbs = moveProbs;
	}
	
	public double getValue() {
		double value = 0;
		for(int i = 0; i < outcomeNodes.size(); i++) {
			value += outcomeNodes.get(i).getValue() * moveProbs[i];
		}
		return value;
	}
	
	public List<Node> getOutcomeNodes(){
		return outcomeNodes;
	}
	
	
	@Override
	public State getNodeState() {
		System.out.println("Calling A1-state. Doesn´t make sense.");
		return null;
	}
	
	private void generateOutcomeNodes() {
		for(int i = -4; i < 6; i++) {
			State dummyState = nodeState;
			State subNodeState = dummyState.changePosition(i, mdp.getProblemSpec().getN());
			outcomeNodes.add(new Node(subNodeState, parentNode, action, mdp));
		}
		State slipState = nodeState;
		State breakState = nodeState;
		
		slipState = slipState.changeSlipCondition(true);
		breakState = breakState.changeBreakdownCondition(true);
		
		outcomeNodes.add(new Node(slipState, parentNode, action, mdp));
		outcomeNodes.add(new Node(breakState, parentNode, action, mdp));
		
	}
	
	
	
	
	
	
	
	
	
	

}
