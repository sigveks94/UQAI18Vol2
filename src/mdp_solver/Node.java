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
	private int value;
	private final static int C = 2; //The discountfactor to be used in UpperConfidenceBound 
	private final static int M = 100000000; //Large number to represent infinity
	private double UCB;
	
	//ADD FIELD FOR TIME UNITS. GETTERS AND SETTERS. 
	
	public Node(State currentState, Node parentNode) {
		this.currentState = currentState;
		this.parentNode = parentNode;
		totVisits = 0;
		value = 0;
		UCB = 0;
		
	}

	public int getTotVisits() {
		return totVisits;
	}

	public void setTotVisits(int totVisits) {
		this.totVisits = totVisits;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public State getCurrentState() {
		return currentState;
	}

	public Node getParentNode() {
		return parentNode;
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
