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
	
	protected State nodeState;
	protected Node parentNode;
	protected int totVisits;
	protected double value;
	protected final static double C = Math.sqrt(5); //The discountfactor to be used in UpperConfidenceBound 
	protected final static int M = 100000000; //Large number to represent infinity
	protected double UCB;
	protected Action action; //the predecessor´s action
	protected int timeUnits;
	protected MDPSolver mdp;
	protected boolean isSubNode;
	protected A1Node A1node;
	
	public Node(State nodeState, Node parentNode, Action action, MDPSolver mdp, boolean isSubNode, A1Node a1node) {
		this.nodeState = nodeState;
		this.parentNode = parentNode;
		totVisits = 0;
		value = 0;
		UCB = M;
		this.action = action;
		this.mdp = mdp;
		this.isSubNode = isSubNode;
		this.A1node = a1node;
		setTimeUnits();
	}
	
	public A1Node getA1Node() {
		return A1node;
	}
	
	public Action getAction() {
		return action;
	}
	
	public int getTimeUnits() {
		return timeUnits;
	}
	
	public boolean isSubNode() {
		return isSubNode;
	}
	
	public double getAvgValue() {
		return this.value/this.totVisits;
	}
	
	public void setTimeUnits(int units) {
		this.timeUnits = units;
	}
	
	public void setTimeUnits() {
		if(parentNode == null) {
			timeUnits = 0;
		}
		else {
			timeUnits += parentNode.getTimeUnits() + 1;
			if(nodeState.isInSlipCondition()) {
				timeUnits += mdp.getSlipRecoveryTime();
				nodeState = nodeState.changeSlipCondition(false);
			}
			if(nodeState.isInBreakdownCondition()) {
				timeUnits += mdp.getRepairTime();
				nodeState = nodeState.changeBreakdownCondition(false);
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
	
	public void updateStat(double value) {
		setValue(value);
		updateTotVisits();
		updateUCB();
	}

	public void setValue(double value) {
		this.value += value;
	}

	public State getNodeState() {
		return nodeState;
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
		double explorationValue = 0;
		if(parentNode!=null) {
			explorationValue = C * Math.sqrt((Math.log(parentNode.getTotVisits())/totVisits));
		}
		
		return avgValue + explorationValue;
	}
	
	public void setUCB() {
		UCB= calculateUCB();
	}
	
	public double getUCB() {
		return UCB;
	}
	
	/**
     * Return the step instance as a string in the format specified by assignment 2
     * spec.
     *
     *      step;(pos,slip,breakdown,car,driver,tire,tirePressure);(actionNumber:actionValues)
     *
     * @return string representation of step as per assignment 2 spec
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (timeUnits == -1) {
            sb.append("start");
        } else {
            sb.append(timeUnits);
        }
        // add state tuple
        sb.append(";(");
        sb.append(nodeState.getPos()).append(",");
        sb.append(booleanToInt(nodeState.isInSlipCondition())).append(",");
        sb.append(booleanToInt(nodeState.isInBreakdownCondition())).append(",");
        sb.append(nodeState.getCarType()).append(",");
        sb.append(nodeState.getDriver()).append(",");
        sb.append(nodeState.getTireModel().asString()).append(",");
        sb.append(nodeState.getFuel()).append(",");
        sb.append(nodeState.getTirePressure().asString()).append(",)");
        // add action
        sb.append(";(");
        if (action == null) {
            sb.append("n.a.");
        } else {
            sb.append(action.getText());
        }
        sb.append(")\n");

        return sb.toString();
    }
    
    private static int booleanToInt(boolean value) {
        // Convert true to 1 and false to 0.
        return value ? 1 : 0;
    }
    

}
