package mdp_solver;

import java.io.File;
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
		String filePath = new File("").getAbsolutePath() + "/";
		Solution solution = new Solution(filePath + "input_lvl1.txt", filePath + "solution.txt");
		solution.run();
	}

}
