package mdp_solver;


import java.util.ArrayList;

import java.util.List;



import problem.*;
import simulator.*;

public class MDPSolver {
	
	private ProblemSpec ps;
	private Level level;
	private List<Action> actionSpace;
	private Simulator sim;
	private int actionCounter;
	
	public MDPSolver(ProblemSpec ps, String outPutFileName) {
		this.ps = ps;
		level = ps.getLevel();
		actionSpace = generateActionSpace();
		sim = new Simulator(ps, outPutFileName);
		actionCounter = 0;
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
		
		if(ps.getLevel().getLevelNumber() > 3) {
			actionSpace.addAll(generateCarDriverActions(cars, drivers, availableActions.get(6)));
		}
		
		return actionSpace;
	}
	
	private List<Action> generateCarDriverActions(List<String> cars, List<String> drivers, ActionType aT){
		List<Action> actions = new ArrayList<>();
		for(String car : cars) {
			for(String driver : drivers) {
				actions.add(new Action(aT, car, driver));
			}
		}
		return actions;
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
	
	public void solve(){
		sim.reset();
		Node currentRootNode = new Node(State.getStartState(ps.getFirstCarType(), ps.getFirstDriver(), ps.getFirstTireModel()), null, null, this, false, null);
		int max = ps.getMaxT();
		while(actionCounter < max) {
			MCTS iteration = new MCTS(ps, actionSpace,this, currentRootNode);
			Action a = iteration.MCTSIteration();
			if(a==null) {
				break;
			}
			State resultState = sim.step(a);
			if(resultState == null) {
				break;
			}
			System.out.println(resultState);
			System.out.println(a.getText() + "\n");
			currentRootNode = new Node(resultState, currentRootNode, a, this, false, null);
			currentRootNode.setTimeUnits(sim.getSteps());
		}		
	}
	
	public ProblemSpec getProblemSpec() {
		return ps;
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
	 * MDPSolver should receive the action suggested from the particular rootNode calculated from a MCTS-class. The MDPSolver should feed this information to
	 * the simulator given in the source code.
	 * 
	 * The MCTS class is the class responsible for finding out what action to take from the current rootNode (the rootNode changes every 15 seconds)
	 * 
	 * 
	 * 
	 * 		MCTS class
	 * 
	 * 
	 * 			From root node it should evaluate all possible actions possible to take. Particularly for A1 this means that it has to have 12 node outcomes.
	 * 			Have to be careful that when backpropagating a value in a Node belonging to A1, the value that is backpropagated should be an expected value based on all UCB values of the A1-nodes multiplied by the possibility of the particular outcomes.
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
