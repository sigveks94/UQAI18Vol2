package mdp_solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import problem.*;
import simulator.*;

public class MDPSolver {
	
	private HashMap<State, List<State>> childStates;
	private ProblemSpec ps;
	private Level level;
	
	public MDPSolver(ProblemSpec ps) {
		childStates = new HashMap<>();
		
		this.ps = ps;
		
		level = ps.getLevel();
		
		
	}
	
	//COMPLETE THIS
	private List<Action> generateActionSpace(){
		List<ActionType> availableActions = level.getAvailableActions();
		List<String> drivers = ps.getDriverOrder();
		List<String> cars = ps.getCarOrder();
		List<Tire> tires = ps.getTireOrder();
		return null;
	}
	
	
	/**
	 * Method that takes a state as input and add a list of all states possible to reach from that state as a child of that state.
	 * @param parentState
	 */
	
	//COMPLETE THIS
	public void generateChildStates(State state){
		
		List<State> states = new ArrayList<>();
		
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
