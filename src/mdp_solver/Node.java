package mdp_solver;
import problem.*;
import simulator.*;

/**
 * 
 * 
 * @author erlendhjellestrandkleiv
 *
 * Class that represent a unique state-action pair. 
 * This unique edge is made up of parentState and currentState
 *
 */

public class Node {
	
	private State currentState;
	private Node parentNode;
	private int totVisits;
	private double value;
	private final static int C = 2; //The discountfactor to be used in UpperConfidenceBound 
	private final static int M = 100000000; //Large number to represent infinity
	private double UCB;
	private Action action; //the predecessor´s action
	
	private int timeUnits; 
	
	public Node(State currentState, Node parentNode, Action action) {
		this.currentState = currentState;
		this.parentNode = parentNode;
		totVisits = 0;
		value = M;
		UCB = M;
		setTimeUnits();
		this.action = action;
		
	}
	
	public Action getAction() {
		return action;
	}
	
	public int getTimeUnits() {
		return timeUnits;
	}
	
	public void setTimeUnits() {
		if(parentNode == null) {
			timeUnits = 0;
		}
		else {
			timeUnits += parentNode.getTimeUnits() + 1;
			if(currentState.isInSlipCondition()) {
				timeUnits += ProblemSpec.getSlipRecoveryTime();
			}
			if(currentState.isInBreakdownCondition()) {
				timeUnits += ProblemSpec.getRepairTime();
			}
		}
	}

	public int getTotVisits() {
		return totVisits;
	}

	public void updateTotVisits() {
		this.totVisits += 1;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value += value;
		updateUCB();
	}

	public State getCurrentState() {
		return currentState;
	}

	public Node getParentNode() {
		return parentNode;
	}
	
	public void updateUCB() {
		UCB = calculateUCB();
	}
	
	public double calculateUCB() {
	 	if(totVisits == 0) {
			return M;
		}
		double avgValue = value/totVisits;
		double explorationValue = C * Math.sqrt((Math.log(parentNode.getTotVisits())/totVisits));
		
		return avgValue + explorationValue;
	}
	
	public void setUCB() {
		calculateUCB();
	}
	
	public double getUCB() {
		return UCB;
	}

}
