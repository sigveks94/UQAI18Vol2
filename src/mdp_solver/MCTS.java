package mdp_solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import problem.Action;
import problem.ProblemSpec;

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
			Node currentNode = rootNode;
			//iterations++;
			
			while(childNodes.containsKey(currentNode)) {
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
//		for(Node n : childNodes.get(rootNode)) {
//			if(n instanceof A1Node) {
//				System.out.println(n.getAvgValue());
//				List<Node> outComeNodes = ((A1Node) n).getOutcomeNodes();
//				for(int i = 0; i < ((A1Node) n).getOutcomeNodes().size(); i++) {
//					System.out.println(outComeNodes.get(i).getAvgValue() + " |||| " + ((A1Node) n).getMoveProbs()[i]);
//				}
//			}
//			else {
//				System.out.println(n.getAvgValue());
//			}
//		}
		
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
						nodes.add(resultNode);
					}
				}
				childNodes.put(node, nodes);
				return nodes.get(0);
			}
	}
	
	private void rollout(Node node) {
		
		if(!(node instanceof A1Node)) {
			double timeSpent = node.getTimeUnits();
			double timeAllowed = 100; //ps.getMaxT();
			Node dummyNode = node;
			
			while(!(timeSpent >= timeAllowed || ownSim.isGoalNode(dummyNode))) {
				int number = aRandomInt(0,6); //QUICK FIX HEURISTIC
				Action action = (number == 0) ? actionSpace.get(0) : actionSpace.get(aRandomInt(1,actionSpace.size())); //actionSpace.get(aRandomInt(0, actionSpace.size()));
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
			double timeAllowed = 100; //ps.getMaxT();
			Node dummyNode = node;
			
			while(!(timeSpent >= timeAllowed || ownSim.isGoalNode(dummyNode))) {
				int number = aRandomInt(0,6); //QUICK FIX HEURISTIC
				Action action = (number == 0) ? actionSpace.get(0) : actionSpace.get(aRandomInt(1,actionSpace.size()));  //actionSpace.get(aRandomInt(0, actionSpace.size()));
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
