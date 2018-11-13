package mdp_solver;

import java.io.File;
import java.io.IOException;

import problem.ProblemSpec;

public class Solution {
	
	private MDPSolver mdpSolver;
	private ProblemSpec ps;
	
	public Solution(String inputFile, String outputFile) throws IOException {
		System.out.println("ja");
		ps = new ProblemSpec(inputFile);
		System.out.println("her");
		mdpSolver = new MDPSolver(ps, outputFile);
	}
	
	public void run() {
		mdpSolver.solve();
	}
	
	public static void main(String[] args) throws IOException {
		String filePath = new File("").getAbsolutePath();
		System.out.println(filePath);
		String addOn = "examples/level_1/";
		String input = filePath + "\\" + "input_lvl2_2.txt";
		System.out.println(input);
		String output = filePath + "\\" + "solutionInput_lvl2_2_2.txt";
		System.out.println(output);
		Solution solution = new Solution(input, output);
		System.out.println(2);
		solution.run();
	}
}
