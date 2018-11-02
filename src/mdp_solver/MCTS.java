package mdp_solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import problem.Action;
import problem.ProblemSpec;
import problem.Tire;

public class MCTS {
	
	
	private HashMap<Node, List<Node>> childNodes;
	private OwnSimulator ownSim;
	private ProblemSpec ps;
	private List<Action> actionSpace;
	private MDPSolver mdp;
	private Node rootNode;
	//private int iterations;
	
	
	public MCTS(ProblemSpec ps, List<Action> actionSpace, MDPSolver mdp, Node rootNode) {
		childNodes = new HashMap<>();
		this.ps = ps;
		this.actionSpace = actionSpace;
		this.mdp = mdp;
		ownSim = new OwnSimulator(ps, mdp);
		this.rootNode = rootNode;
		//iterations = 0;
	}
	
	//DETTE ER EN ITERASJON
	public Action MCTSIteration() {
		if(ownSim.isGoalNode(rootNode)) {
			return null;
		}
		final long startTime = System.currentTimeMillis();
		
		//DETTE ER EN MINI-ITERASJON
		while((System.currentTimeMillis() - startTime < 14500)) { //&& iterations < 100000) {
			Node currentNode = rootNode; //START
			//iterations++;
			
			while(childNodes.containsKey(currentNode) || (currentNode instanceof A1Node && childNodes.containsKey(((A1Node)currentNode).getOutcomeNodes().get(0)))) { 
				currentNode = selectNode(currentNode);
			}
			if(currentNode.getTotVisits() == 0) {
				rollout(currentNode);
			}
			else {
				Node nodeToRollout = expand(currentNode);
				rollout(nodeToRollout);
			}
		}
		for(Node n : childNodes.get(rootNode)) {
			if(n instanceof A1Node) {
				System.out.println(n.getAvgValue());
				List<Node> outComeNodes = ((A1Node) n).getOutcomeNodes();
				for(int i = 0; i < ((A1Node) n).getOutcomeNodes().size(); i++) {
					System.out.println(outComeNodes.get(i).getAvgValue() + " |||| " + ((A1Node) n).getMoveProbs()[i]);
				}
			}
			else {
				System.out.println(n.getAvgValue());
			}
		}
		
		return selectBestAction(rootNode);
		}
	

	//RETURNS THE BEST ACTION OUT OF THE OPTIONS THAT HAS THE BEST VALUE-SCORE
	private Action selectBestAction(Node node) {
		List<Node> children = childNodes.get(node);
		double bestValue = -1;
		Node bestNode = null;
		for(Node child : children) {
			if(child.getAvgValue() > bestValue) {
				bestNode = child;
				bestValue = child.getAvgValue();
			}
		}
		return bestNode.getAction();
	}
	
	
	//THIS WAY OF IMPLEMENTING IT REQUIRES A "CONTAINER"-NODE FOR ALL A1-NODES.
	private Node selectNode(Node node) {
		if(node instanceof A1Node) {
			return selectA1Node((A1Node) node);
		}
		List<Node> children = childNodes.get(node);
		double bestUCB = -1;
		Node bestNode = null;
		for(Node child : children) {
			if(child.getUCB() > bestUCB) {
				bestNode = child;
				bestUCB = child.getUCB();
			}
		}
		return bestNode;
	}
	
	private Node selectA1Node(A1Node node) {
		List<Node> outcomeNodes = node.getOutcomeNodes();
		double bestUCB = -1;
		Node bestNode = null;
		for(Node outcomeNode : outcomeNodes) {
			List<Node> children = childNodes.get(outcomeNode);
			for(Node child : children) {
				if(child.getUCB() > bestUCB) {
					bestNode = child;
					bestUCB = child.getUCB();
				}
			}
		}
		return bestNode;
	}
	
	public Node expand(Node node){
		if(node instanceof A1Node) {
			List<Node> subNodes = ((A1Node) node).getOutcomeNodes();
			Node firstNode = subNodes.get(0);
			for (Node subNode : subNodes) {
				expand(subNode);
			}
			return firstNode;
		}
			else {
				List<Node> nodes = new ArrayList<>();
				for(Action action : actionSpace) {
					if(action.getText().equals("A1")){
						double[] moveProbs = ownSim.getMoveProbs(node);
		 				A1Node a1node = new A1Node(node.getNodeState(), node, action, mdp, moveProbs);
		 				nodes.add(a1node);
					}
					else 
					{
						Node resultNode = ownSim.step(action, node);
						if(!(node.getNodeState().toString().equals(resultNode.getNodeState().toString()))) { //AVOID ADDING NODES THAT "EQUAL" YOURSELF.
							nodes.add(resultNode);
						}
						
					}
				}
				childNodes.put(node, nodes);
				
				return nodes.get(0);
			}
	}
	
	private void rollout(Node node) {
		
		if(!(node instanceof A1Node)) {
			double timeSpent = node.getTimeUnits();
			double timeAllowed = ps.getMaxT();
			Node dummyNode = node;
			
			while(!(timeSpent >= timeAllowed || ownSim.isGoalNode(dummyNode))) {
				//int number = aRandomInt(0,6); //QUICK FIX HEURISTIC
				//Action action = (number == 0) ? actionSpace.get(0) : actionSpace.get(aRandomInt(1,actionSpace.size())); //actionSpace.get(aRandomInt(0, actionSpace.size()));
				Action action = rolloutHeuristic(dummyNode);
				Node resultingNode = ownSim.step(action, dummyNode);
				dummyNode = resultingNode;
				timeSpent = dummyNode.getTimeUnits()+1;
			}
			
			double reward = calculateReward(dummyNode);
			
			
			backPropagate(node, reward);
		}
		else {
			rolloutA1((A1Node) node);
		}
		
		
	}
	
	private void rolloutA1(A1Node a1Node) {
		
		for(Node node : a1Node.getOutcomeNodes()) {
			double timeSpent = node.getTimeUnits();
			double timeAllowed = ps.getMaxT();
			Node dummyNode = node;
			
			while(!(timeSpent >= timeAllowed || ownSim.isGoalNode(dummyNode))) {
				
				 														//				int number = aRandomInt(0,6); //QUICK FIX HEURISTIC
				Action action = rolloutHeuristic(dummyNode);			//				Action action = (number == 0) ? actionSpace.get(0) : actionSpace.get(aRandomInt(1,actionSpace.size()));  //actionSpace.get(aRandomInt(0, actionSpace.size()));
				Node resultingNode = ownSim.step(action, dummyNode);
				dummyNode = resultingNode;
				timeSpent = dummyNode.getTimeUnits()+1;
			}
			node.updateStat(calculateReward(dummyNode));
		}
		
		a1Node.updateA1Node();
		double reward = a1Node.getValue();
		backPropagate(a1Node.getParentNode(), reward);
	}
	
	
	//CALCULATES THE PROBABILITY OF MOVING FORWARD FROM A GIVEN NODE.
	public double calculateProbMove(Node node) {
		
		double[] moveProbs = ownSim.getMoveProbs(node);
		
		double sum = 0;
		for(int i = 5; i < 10; i++) {
			sum += moveProbs[i];
		}
		return sum;
	}
	
	public Action rolloutHeuristic(Node node) {
		double sum = calculateProbMove(node);
		if(sum > 0.5) {
			return actionSpace.get(0);
		}
		Action preferredAction = calculatePreferredAction(node, sum);
		return preferredAction;
	}

	
	
	private Action calculatePreferredAction(Node node, double sum) {
		double oldSumMove = sum;
		Node temporaryNode;
		Action prevAction = node.getAction();
		Action temporaryAction = actionSpace.get(0);
		Action bestAction = temporaryAction;
		double temporaryBestSumMove = oldSumMove;
		
//		//IF YOUR PREVIOUS ACTION WAS NOT TO MOVE FORWARD, THIS MEANS THAT YOU MUST TRY TO MOVE FORWARD NOW.
//		
//		if(!(prevAction == null) && !(prevAction.equals(actionSpace.get(0)))) {
//			return actionSpace.get(0);
//		}
		
		
		//TRY CHANGE CAR (A2)
		LinkedHashMap<String, double[]> carMoveProb = ps.getCarMoveProbability();
		for(int i = 1; i < ps.getCT() + 1; i++) {
			temporaryAction = actionSpace.get(i);
			String car = temporaryAction.getCarType();
			double[] moveProb = carMoveProb.get(car);
			double probMoveForward = 0;
			for(int j = 5; j < 10; j++) {
				probMoveForward += moveProb[j];
			}
			if(probMoveForward > temporaryBestSumMove) {
				if(prevAction == null) {
					temporaryBestSumMove = probMoveForward;
					bestAction = temporaryAction;
					continue;
				}
				if(!(prevAction.equals(temporaryAction))) {
					temporaryBestSumMove = probMoveForward;
					bestAction = temporaryAction; 	
				}
			}
		}
		
		//TRY CHANGE DRIVER (A3)
		
		LinkedHashMap<String, double[]> driverMoveProb = ps.getDriverMoveProbability();
		for(int i = ps.getCT()+1; i < ps.getCT() + ps.getDT()+1; i++) {
			temporaryAction = actionSpace.get(i);
			String driver = temporaryAction.getDriverType();
			double[] moveProb = driverMoveProb.get(driver);
			double probMoveForward = 0;
			for(int j = 5; j < 10; j++) {
				probMoveForward += moveProb[j];
			}
			if(probMoveForward > temporaryBestSumMove) {
				if(prevAction == null) {
					temporaryBestSumMove = probMoveForward;
					bestAction = temporaryAction;
					continue;
				}
				if(!(prevAction.equals(temporaryAction))) {
					temporaryBestSumMove = probMoveForward;
					bestAction = temporaryAction; 	
				}	
			}
		}
		
		
		//TRY CHANGE TIRES (A4)
		LinkedHashMap<Tire, double[]> tireMoveProb = ps.getTireModelMoveProbability();
		for(int i = ps.getCT()+ ps.getDT() + 1; i < ps.getCT() + ps.getDT()+ 4 + 1 ;i++) {
			temporaryAction = actionSpace.get(i);
			Tire tire = temporaryAction.getTireModel();
			double[] moveProb = tireMoveProb.get(tire);
			double probMoveForward = 0;
			for(int j = 5; j < 10; j++) {
				probMoveForward += moveProb[j];
			}
			if(probMoveForward > temporaryBestSumMove) {
				if(prevAction == null) {
					temporaryBestSumMove = probMoveForward;
					bestAction = temporaryAction;
					continue;
				}
				if(!(prevAction.equals(temporaryAction))) {
					temporaryBestSumMove = probMoveForward;
					bestAction = temporaryAction; 	
				}	
			}
		}
		
		
		//TRY ADD FUEL?? :) (A5) Only level 2 and up
		
		if(ps.getLevel().getLevelNumber() > 1) {
			temporaryAction = actionSpace.get(ps.getCT() + ps.getDT()+ 4 + 1);
			temporaryNode = ownSim.step(temporaryAction, node);
			double newMoveSum = calculateProbMove(temporaryNode);
			if(newMoveSum > temporaryBestSumMove) {
				if(prevAction == null) {
					temporaryBestSumMove = newMoveSum;
					bestAction = temporaryAction;
				}
				else{
					if((!(prevAction.equals(temporaryAction)))) {
						temporaryBestSumMove = newMoveSum;
						bestAction = temporaryAction; 	
					}	
				}
			}
		}
		
		//TRY CHANGE PRESSURE (A6) Only level 2 and up
		
		if(ps.getLevel().getLevelNumber() > 1) {
			for(int i = ps.getCT() + ps.getDT()+ 4 + 2; i < ps.getCT() + ps.getDT()+ 4 + 2 + 3; i++) {
				temporaryAction = actionSpace.get(i);
				temporaryNode = ownSim.step(temporaryAction, node);
				double newMoveSum = calculateProbMove(temporaryNode);
				if(newMoveSum > temporaryBestSumMove) {
					if(prevAction == null) {
						temporaryBestSumMove = newMoveSum;
						bestAction = temporaryAction;
					}
					else{
						if((!(prevAction.equals(temporaryAction)))) {
							temporaryBestSumMove = newMoveSum;
							bestAction = temporaryAction; 	
						}	
					}
				}
			}
		}
		//TRY CHANGE CAR AND DRIVER ??? FOR LEVEL 4!! FOR EACH CAR LOOP THROUGH EACH DRIVER AND MULTIPLY THEIR PROBABILITIES FOR MOVING FORWARD TOGETHER INTO A carDriverMoveProb-list.
		if(ps.getLevel().getLevelNumber() > 3) {
			for(int i = ps.getCT() + ps.getDT()+ 4 + 2 + 3; i < ps.getCT() + ps.getDT()+ 4 + 2 + 3 + ps.getCT() * ps.getDT(); i++) {
				temporaryAction = actionSpace.get(i);
				temporaryNode = ownSim.step(temporaryAction, node);
				double newMoveSum = calculateProbMove(temporaryNode);
				if(newMoveSum > temporaryBestSumMove) {
					if(prevAction == null) {
						temporaryBestSumMove = newMoveSum;
						bestAction = temporaryAction;
					}
					else{
						if((!(prevAction.equals(temporaryAction)))) {
							temporaryBestSumMove = newMoveSum;
							bestAction = temporaryAction; 	
						}	
					}
				}
			}
		}
		
		return bestAction;
	}

	public double calculateReward(Node node) {
		if(ownSim.isGoalNode(node)) {
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
		
		if(node.getParentNode() == null) {
			node.updateStat(reward);
			return;
		}
		else if(node.isSubNode()) {
			node.updateStat(reward);
			A1Node superNode = node.getA1Node();
			superNode.updateA1Node();
			backPropagate(superNode.getParentNode(), reward);
			
		}
		else {
			node.updateStat(reward);
			backPropagate(node.getParentNode(), reward);
		}
		
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
	

}
