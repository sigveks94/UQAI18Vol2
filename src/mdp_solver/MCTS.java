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
	
	
	public MCTS(ProblemSpec ps, List<Action> actionSpace, MDPSolver mdp, Node rootNode) {
		childNodes = new HashMap<>();
		this.ps = ps;
		this.actionSpace = actionSpace;
		this.mdp = mdp;
		ownSim = new OwnSimulator(ps, mdp);
		this.rootNode = rootNode;
	}
	
	//DETTE ER EN ITERASJON
	public Action MCTSIteration() {
		if(ownSim.isGoalNode(rootNode)) {
			return null;
		}
		final long startTime = System.currentTimeMillis();
		
		//DETTE ER EN MINI-ITERASJON
		while(System.currentTimeMillis() - startTime < 14500) {
			Node currentNode = rootNode;
			
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
		return selectBestAction(rootNode);
	}
	

	//RETURNS THE BEST ACTION OUT OF THE OPTIONS THAT HAS THE BEST VALUE-SCORE
	private Action selectBestAction(Node node) {
		
		List<Node> children = childNodes.get(node);
		double bestValue = -1;
		Node bestNode = null;
		for(Node child : children) {
			if(child.getValue() > bestValue) {
				bestNode = child;
				bestValue = child.getValue();
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
			List<A1Node> callStack = new ArrayList<>();
			for (Node subNode : subNodes) {
				callStack.add((A1Node) expand(subNode));
			}
			return callStack.get(0);
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
						nodes.add(new Node(resultNode.getNodeState(), node, action, mdp));
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
			
			while(!(timeSpent >= timeAllowed || ownSim.isGoalNode(node))) {
				
				Action action = actionSpace.get(aRandomInt(0, actionSpace.size()));
				Node resultingNode = ownSim.step(action, node);
				node = resultingNode;
				timeSpent = node.getTimeUnits()+1;
			}
			
			double reward = calculateReward(node);
			//System.out.println(reward);
			
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
			
			while(!(timeSpent >= timeAllowed || ownSim.isGoalNode(node))) {
				Action action = actionSpace.get(aRandomInt(0, actionSpace.size()));
				Node resultingNode = ownSim.step(action, node);
				node = resultingNode;
				timeSpent += node.getTimeUnits();
			}
			node.setValue(calculateReward(node));
		}
		
		backPropagate(a1Node, a1Node.getValue());
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
		while(node.getParentNode() != null) {
			node.updateStat(reward);
			node = node.getParentNode();
		}
		node.updateStat(reward);
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
