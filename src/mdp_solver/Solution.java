package mdp_solver;

import java.io.IOException;

import problem.ProblemSpec;

public class Solution {
	
	
	private MDPSolver mdpSolver;
	private ProblemSpec ps;
	private OwnSimulator sim;
	
	public Solution(String inputFile, String outputFile) throws IOException {
		ps = new ProblemSpec(inputFile);
		sim = new OwnSimulator(ps);
		mdpSolver = new MDPSolver(sim, ps, outputFile);
	}
	
	public void run() {
		mdpSolver.MCTS();
	}
	
	public static void main(String[] args) throws IOException {
		Solution solution = new Solution("input_lvl1.txt", "solution.txt");
		
	}

}
