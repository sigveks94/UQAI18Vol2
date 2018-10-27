package mdp_solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


import problem.*;
import simulator.*;

public class MDPSolver {
	
	private HashMap<Node, List<Node>> childNodes;
	private ProblemSpec ps;
	private Level level;
	private Simulator sim;
	private List<Action> actionSpace;
	
	public MDPSolver(Simulator sim) {
		childNodes = new HashMap<>();
		
		this.sim = sim;
		
		ps = sim.getProblemSpec();
		
		level = ps.getLevel();
		
		actionSpace = generateActionSpace();
		
		
	}
	
	/**
	 * Method that creates the entire actionSpace based on all possible actions.
	 * @return actionSpace based on level
	 */
	private List<Action> generateActionSpace(){
		List<ActionType> availableActions = level.getAvailableActions(); //ORDER: MOVE, CAR, DRIVER, TIRE, FUEL, PRESSURE
		List<Action> actionSpace = new ArrayList<>();
		actionSpace.add(new Action(availableActions.get(0)));
		List<String> drivers = ps.getDriverOrder();
		List<String> cars = ps.getCarOrder();
		List<Tire> tires = ps.getTireOrder();
		
		actionSpace.addAll(generateCarActions(cars, availableActions.get(1)));
		actionSpace.addAll(generateDriverActions(drivers, availableActions.get(2)));
		actionSpace.addAll(generateTireActions(tires, availableActions.get(3)));
		if(ps.getLevel().getLevelNumber() > 1) {
			actionSpace.add(new Action(availableActions.get(4), 10)); //ONLY ONE FUEL ACTION AND THAT IS TO FILL 10 LITERS
			actionSpace.addAll(generateTirePressureActions(TirePressure.FIFTY_PERCENT,TirePressure.SEVENTY_FIVE_PERCENT, TirePressure.ONE_HUNDRED_PERCENT, availableActions.get(5)));
		}
		
		return actionSpace;
	}
	
	private List<Action> generateCarActions(List<String> cars,ActionType aT){
		List<Action> actions = new ArrayList<>();
		for(String car : cars) {
			actions.add(new Action(aT, car));
		}
		return actions;
	}
	
	private List<Action> generateDriverActions(List<String> drivers, ActionType aT){
		List<Action> actions = new ArrayList<>();
		for(String driver : drivers) {
			actions.add(new Action(aT, driver));
		}
		return actions;
	}
	
	private List<Action> generateTireActions(List<Tire> tires, ActionType aT){
		List<Action> actions = new ArrayList<>();
		for(Tire tire : tires) {
			actions.add(new Action(aT , tire));
		}
		return actions;
		
	}
	
	private List<Action> generateTirePressureActions(TirePressure fifty, TirePressure seventyFive, TirePressure hundred, ActionType aT){
		List<Action> actions = new ArrayList<>();
		actions.add(new Action(aT, fifty));
		actions.add(new Action(aT, seventyFive));
		actions.add(new Action(aT, hundred));
		return actions;
		
	}
	
	
	
	
	/**
	 * Method that takes a state as input and add a list of all states possible to reach from that state as a child of that state.
	 * @param parentState
	 */
	
	public void MCTS(Node firstNode){
		List<Node> chosenNodes= new ArrayList<>();
		chosenNodes.add(firstNode);
		Node currentRootNode=firstNode;
		while(!sim.isGoalState(currentRootNode.getCurrentState())) {
			currentRootNode = MCTSIteration(currentRootNode);
			chosenNodes.add(currentRootNode);
		}
		
		//OUTPUT-FORMATTING AND TWEAKING - ONLY NEED TO ITERATE THE CHOSENNODES-LIST AND EXTRACT THE INFORMATION FROM THE NODES
		
	}
	
	public Node selectNextRootNode(Node node) {
		//RETURNS THE NEXT SELECTED NODE OUT OF THE OPTIONS THAT HAS THE BEST UCB-SCORE
		return null;
	}
	
	private Node MCTSIteration(Node rootNode) {
		
		// WHILE LOOP WITH EXIT CONDITION TIME = 13 
		// INSIDE IS MCTSITERATION-ALGORITHM WHICH SELECTS, EXPANDS AND ROLLOUTS CONTINOUSLY
		// THIS RESULTS IN ALL AFFECTED NODES� VALUES AND VISITED-FREQUENCIES ARE UPDATED
		// NEED TO CREATE A FUNCTION THAT SELECTS THE NEXT LEAFNODE WICH IS THE SUBJECT OF THE NEXT WHILE-LOOP (aka selectNextLeafNode(rootNode))
		// SHOULD END WITH SETTING THE PARENT NODE OF STATE WE END UP IN AS NULL
		
		return selectNextRootNode(rootNode);
	}
	
	
	
	//COMPLETE THIS
		public void expand(Node node){
			List<Node> nodes = new ArrayList<>();
			for(Action action : actionSpace) {
				State state = sim.step(action);
				nodes.add(new Node(state, node, action, this));
			}
			childNodes.put(node, nodes);
		}
	
	
	private void rollOut(Node node) {
		
		double timeSpent = node.getTimeUnits();
		double timeAllowed = ps.getMaxT();
		
		while(!(timeSpent >= timeAllowed || sim.isGoalState(node.getCurrentState()))) {

			Action action = actionSpace.get(aRandomInt(0, actionSpace.size()));
			
			State resultingState = sim.step(action);
			
			Node resultingNode = new Node(resultingState, node, action, this);
			
			node = resultingNode;
			
			timeSpent += node.getTimeUnits();
		}
		
		double reward = calculateReward(node);
		
		backPropagate(node, reward);
	}
	
	
	public double calculateReward(Node node) {
		
		if(sim.isGoalState(node.getCurrentState())) {
			double timeSpent = node.getTimeUnits();
			if(timeSpent > ps.getMaxT()) {
				return 0;
			}
			else {
				return 100/timeSpent;
			}
		}
		else {
			return 0;
		}
	}
	
	public void backPropagate(Node node, double reward) {
		while(node.getParentNode() != null) {
			node.setValue(reward);
			node.updateTotVisits();
			node = node.getParentNode();
		}
		node.setValue(reward);
		node.updateTotVisits();
	}
	
	 /** I'm a helper method */
    private static int aRandomInt(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min)) + min;
    }
    
    public int getSlipRecoveryTime() {
    	return ps.getSlipRecoveryTime();
    }
    
    public int getRepairTime() {
    	return ps.getRepairTime();
    }
	
	/*
	 * THOUGTHS OF WHAT TO DO
	 * 
	 * 
	 *	Generate neigbour-states to current state
	 *	Calculate reward to a given state (both deterministic and non-deterministic)
	 *	Return string of resulting optimal actions
	 *	ant-jar build file
	 * 
	 * 
	 * 	PSEUDOCODE
	 * 
	 * 		MCTS:
	 * 
	 * 			-Set current node to startState (initial)
	 * 			-Check whether current node is leaf node
	 * 				-if(leafNode)
	 * 					-Check whether node has been visited
	 * 						-if(visited)
	 * 							for each available action from current node, add a new node to the tree.
	 * 							current node = first new child node (or some other smart heuristic)
	 * 							rollout
	 * 						-else
	 * 							rollout
	 * 				-else
	 * 					current = child node of current that maximises UCB.
	 * 			-Check whether current is a leaf node
	 * 
	 * 			
	 * 
	 * 	
	 * 							
	 * 
	 * 		
	 * 
	 * 
	 * 
	 * 
	 */


}
