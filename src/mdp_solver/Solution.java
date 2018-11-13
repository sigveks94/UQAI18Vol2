package mdp_solver;

import java.io.File;
import java.io.IOException;

import problem.ProblemSpec;

public class Solution {
	
	private MDPSolver mdpSolver;
	private ProblemSpec ps;
	
	public Solution(String inputFile, String outputFile) throws IOException {
		ps = new ProblemSpec(inputFile);
		mdpSolver = new MDPSolver(ps, outputFile);
	}
	
	public void run() {
		mdpSolver.solve();
	}
	
	public static void main(String[] args) throws IOException {
		String filePath = new File("").getAbsolutePath() + "/";
		String addOn = "examples/level_1/";
		Solution solution = new Solution(filePath + "input_lvl4_2.txt", filePath + "solutionInput_lvl4_2.txt");
		solution.run();
	}
}
