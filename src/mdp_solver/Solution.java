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
		Solution solution = new Solution(filePath + "input_lvl2.txt", filePath + "solution.txt");
		solution.run();
	}

}
