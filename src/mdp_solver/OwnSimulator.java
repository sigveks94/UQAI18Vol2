package mdp_solver;

import problem.*;
import simulator.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the simulator for the problem.
 * The simulator takes in an action and returns the next state.
 */
public class OwnSimulator {

    /** Problem spec for the current problem **/
    private ProblemSpec ps;
    /** The current state of the environment **/
    /** The number of steps taken **/
    /** Whether to print progress messages or not
     * Feel free to change this if you don't want messages printed **/
    private boolean verbose = false;
    /** A container to store steps for output **/
    private MDPSolver mdp;



    /**
     * Construct a new simulator instance from the given problem spec
     *
     * @param ps the ProblemSpec
     * @param outputFile the path for output file
     */
    public OwnSimulator(ProblemSpec ps, MDPSolver mdp) {
        this.ps = ps;
        this.mdp = mdp;
//        reset();
    }
    

    /**
     * Construct a new simulator instance from the given input file
     *
     * @param inputFile path to input file
     * @param outputFile the path for output file
     * @throws IOException if can't find file or there is a format error
     */
//    public OwnSimulator(String inputFile, MDPSolver mdp) throws IOException {
//        this(new ProblemSpec(inputFile), mdp);
//    }

    /**
     * Reset the simulator and return initial state
     *
     * @return the start state
     */
//    public State reset() {
//        //steps = 0;
//        currentState = State.getStartState(ps.getFirstCarType(),
//                ps.getFirstDriver(), ps.getFirstTireModel());
//        stepRecord = new ArrayList<>();
//        stepRecord.add(new Step(-1, currentState.copyState(), null));
//        if (verbose) {
//            System.out.println("Resetting simulator");
//            System.out.println("Start " + currentState.toString());
//        }
//        return currentState.copyState();
//    }

    /**
     * Perform an action against environment and receive the next state.
     *
     * @param a the action to perform
     * @return the next state or null if max time steps exceeded for problem
     */
    public Node step(Action a, Node node) throws IllegalArgumentException {

        State nextState;

        if (!actionValidForLevel(a)) {
            throw new IllegalArgumentException("ActionType A"
                    + a.getActionType().getActionNo()
                    + " is an invalid action for problem level "
                    + ps.getLevel());
        }

        //if (steps > ps.getMaxT()) {
          //  if (verbose) {
            //    System.out.println("Max time steps exceeded: " + steps + " > "
              //          + ps.getMaxT());
            //}
            //return null;
        //}

//        if (verbose) {
//            System.out.println("Step " + steps +": performing A"
//                    + a.getActionType().getActionNo());
//        }

        switch(a.getActionType().getActionNo()) {
            case 1:
                nextState = performA1(node);
                break;
            case 2:
                nextState = performA2(a, node);
                break;
            case 3:
                nextState = performA3(a, node);
                break;
            case 4:
                nextState = performA4(a, node);
                break;
            case 5:
                nextState = performA5(a, node);
                break;
            case 6:
                nextState = performA6(a, node);
                break;
            case 7:
                nextState = performA7(a, node);
                break;
            default:
                nextState = node.getNodeState();
        }
       
       Node nextNode = new Node(nextState, node, a, mdp, false, null);
  
       return nextNode;
    }

    /**
     * Checks if given action is valid for the current problem level
     *
     * @param a the action being performed
     * @return True if action is value, False otherwise
     */
    private boolean actionValidForLevel(Action a) {
        return ps.getLevel().isValidActionForLevel(a.getActionType());
    }

    /**
     * Perform CONTINUE_MOVING action
     *
     * @return the next state
     */
    private State performA1(Node node) {

        State nextState;

        // check there is enough fuel to make move in current state
        int fuelRequired = getFuelConsumption(node);
        int currentFuel = node.getNodeState().getFuel();
        if (fuelRequired > currentFuel) {
            return node.getNodeState();
        }

        // Sample move distance
        int moveDistance = sampleMoveDistance(node);

        // handle slip and breakdown cases, addition of steps handled in step method
        if (moveDistance == ProblemSpec.SLIP) {
            if (verbose) {
                System.out.println("\tSampled move distance=SLIP");
            }
            nextState = node.getNodeState().changeSlipCondition(true);
        } else if (moveDistance == ProblemSpec.BREAKDOWN) {
            if (verbose) {
                System.out.println("\tSampled move distance=BREAKDOWN");
            }
            nextState = node.getNodeState().changeBreakdownCondition(true);
        } else {
            if (verbose) {
                System.out.println("\tSampled move distance=" + moveDistance);
            }
            nextState = node.getNodeState().changePosition(moveDistance, ps.getN());
        }

        // handle fuel usage for level 2 and above
        if (ps.getLevel().getLevelNumber() > 1) {
            nextState = nextState.consumeFuel(fuelRequired);
        }

        return nextState;
    }

    /**
     * Return the move distance by sampling from conditional probability
     * distribution.
     *
     * N.B. this formula is not at all optimized for performance, so be wary if
     * trying to use it for finding a policy
     *
     * @return the move distance in range [-4, 5] or SLIP or BREAKDOWN
     */
    private int sampleMoveDistance(Node node) {

        double[] moveProbs = getMoveProbs(node);

        double p = Math.random();
        double pSum = 0;
        int move = 0;
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            pSum += moveProbs[k];
            if (p <= pSum) {
                move = ps.convertIndexIntoMove(k);
                break;
            }
        }
        return move;
    }

    /**
     * Calculate the conditional move probabilities for the current state.
     *
     *          P(K | C, D, Ti, Te, Pressure)
     *
     * @return list of move probabilities
     */
    public double[] getMoveProbs(Node node) {

        // get parameters of current state
        Terrain terrain = ps.getEnvironmentMap()[node.getNodeState().getPos() - 1];
        int terrainIndex = ps.getTerrainIndex(terrain);
        String car = node.getNodeState().getCarType();
        String driver = node.getNodeState().getDriver();
        Tire tire = node.getNodeState().getTireModel();

        // calculate priors
        double priorK = 1.0 / ProblemSpec.CAR_MOVE_RANGE;
        double priorCar = 1.0 / ps.getCT();
        double priorDriver = 1.0 / ps.getDT();
        double priorTire = 1.0 / ProblemSpec.NUM_TYRE_MODELS;
        double priorTerrain = 1.0 / ps.getNT();
        double priorPressure = 1.0 / ProblemSpec.TIRE_PRESSURE_LEVELS;

        // get probabilities of k given parameter
        double[] pKGivenCar = ps.getCarMoveProbability().get(car);
        double[] pKGivenDriver = ps.getDriverMoveProbability().get(driver);
        double[] pKGivenTire = ps.getTireModelMoveProbability().get(tire);
        double pSlipGivenTerrain = ps.getSlipProbability()[terrainIndex];
        double[] pKGivenPressureTerrain = convertSlipProbs(pSlipGivenTerrain, node);

        // use bayes rule to get probability of parameter given k
        double[] pCarGivenK = bayesRule(pKGivenCar, priorCar, priorK);
        double[] pDriverGivenK = bayesRule(pKGivenDriver, priorDriver, priorK);
        double[] pTireGivenK = bayesRule(pKGivenTire, priorTire, priorK);
        double[] pPressureTerrainGivenK = bayesRule(pKGivenPressureTerrain,
                (priorTerrain * priorPressure), priorK);

        // use conditional probability formula on assignment sheet to get what
        // we want (but what is it that we want....)
        double[] kProbs = new double[ProblemSpec.CAR_MOVE_RANGE];
        double kProbsSum = 0;
        double kProb;
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            kProb = magicFormula(pCarGivenK[k], pDriverGivenK[k],
                    pTireGivenK[k], pPressureTerrainGivenK[k], priorK);
            kProbsSum += kProb;
            kProbs[k] = kProb;
        }

        // Normalize
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            kProbs[k] /= kProbsSum;
        }

        return kProbs;
    }

    /**
     * Convert the probability of slipping on a given terrain with 50% tire
     * pressure into a probability list, of move distance versus current
     * terrain and tire pressure.
     *
     * @param slipProb probability of slipping on current terrain and 50%
     *                 tire pressure
     * @return list of move probabilities given current terrain and pressure
     */
    private double[] convertSlipProbs(double slipProb, Node node) {

        // Adjust slip probability based on tire pressure
        TirePressure pressure = node.getNodeState().getTirePressure();
        if (pressure == TirePressure.SEVENTY_FIVE_PERCENT) {
            slipProb *= 2;
        } else if (pressure == TirePressure.ONE_HUNDRED_PERCENT) {
            slipProb *= 3;
        }
        // Make sure new probability is not above max
        if (slipProb > ProblemSpec.MAX_SLIP_PROBABILITY) {
            slipProb = ProblemSpec.MAX_SLIP_PROBABILITY;
        }

        // for each terrain, all other action probabilities are uniform over
        // remaining probability
        double[] kProbs = new double[ProblemSpec.CAR_MOVE_RANGE];
        double leftOver = 1 - slipProb;
        double otherProb = leftOver / (ProblemSpec.CAR_MOVE_RANGE - 1);
        for (int i = 0; i < ProblemSpec.CAR_MOVE_RANGE; i++) {
            if (i == ps.getIndexOfMove(ProblemSpec.SLIP)) {
                kProbs[i] = slipProb;
            } else {
                kProbs[i] = otherProb;
            }
        }

        return kProbs;
    }

    /**
     * Apply bayes rule to all values in cond probs list.
     *
     * @param condProb list of P(B|A)
     * @param priorA prior probability of parameter A
     * @param priorB prior probability of parameter B
     * @return list of P(A|B)
     */
    private double[] bayesRule(double[] condProb, double priorA, double priorB) {

        double[] swappedProb = new double[condProb.length];

        for (int i = 0; i < condProb.length; i++) {
            swappedProb[i] = (condProb[i] * priorA) / priorB;
        }
        return swappedProb;
    }

    /**
     * Conditional probability formula from assignment 2 sheet
     *
     * @param pA P(A | E)
     * @param pB P(B | E)
     * @param pC P(C | E)
     * @param pD P(D | E)
     * @param priorE P(E)
     * @return numerator of the P(E | A, B, C, D) formula (still need to divide
     *      by sum over E)
     */
    private double magicFormula(double pA, double pB, double pC, double pD,
                               double priorE) {
        return pA * pB * pC * pD * priorE;
    }

    /**
     * Get the fuel consumption of moving given the current state
     *
     * @return move fuel consumption for current state
     */
    public int getFuelConsumption(Node node) {

        // get parameters of current state
        Terrain terrain = ps.getEnvironmentMap()[node.getNodeState().getPos() - 1];
        String car = node.getNodeState().getCarType();
        TirePressure pressure = node.getNodeState().getTirePressure();

        // get fuel consumption
        int terrainIndex = ps.getTerrainIndex(terrain);
        int carIndex = ps.getCarIndex(car);
        int fuelConsumption = ps.getFuelUsage()[terrainIndex][carIndex];

        if (pressure == TirePressure.FIFTY_PERCENT) {
            fuelConsumption *= 3;
        } else if (pressure == TirePressure.SEVENTY_FIVE_PERCENT) {
            fuelConsumption *= 2;
        }
        return fuelConsumption;
    }

    /**
     * Perform CHANGE_CAR action
     *
     * @param a a CHANGE_CAR action object
     * @return the next state
     */
    private State performA2(Action a, Node node) {

        if (node.getNodeState().getCarType().equals(a.getCarType())) {
            // changing to same car type does not change state but still costs a step
            // no cheap refill here, muhahaha
            return node.getNodeState();
        }

        return node.getNodeState().changeCarType(a.getCarType());
    }

    /**
     * Perform CHANGE_DRIVER action
     *
     * @param a a CHANGE_DRIVER action object
     * @return the next state
     */
    private State performA3(Action a, Node node) { return node.getNodeState().changeDriver(a.getDriverType()); }

    /**
     * Perform the CHANGE_TIRES action
     *
     * @param a a CHANGE_TIRES action object
     * @return the next state
     */
    private State performA4(Action a, Node node) {
        return node.getNodeState().changeTires(a.getTireModel());
    }

    /**
     * Perform the ADD_FUEL action
     *
     * @param a a ADD_FUEL action object
     * @return the next state
     */
    private State performA5(Action a, Node node) {
        // calculate number of steps used for refueling (minus 1 since we add
        // 1 in main function
        //int stepsRequired = (int) Math.ceil(a.getFuel() / (float) 10);
    	// steps += (stepsRequired - 1);
        return node.getNodeState().addFuel(a.getFuel());
    }

    /**
     * Perform the CHANGE_PRESSURE action
     *
     * @param a a CHANGE_PRESSURE action object
     * @return the next state
     */
    private State performA6(Action a, Node node) {
        return node.getNodeState().changeTirePressure(a.getTirePressure());
    }

    /**
     * Perform the CHANGE_CAR_AND_DRIVER action
     *
     * @param a a CHANGE_CAR_AND_DRIVER action object
     * @return the next state
     */
    private State performA7(Action a, Node node) {

        if (node.getNodeState().getCarType().equals(a.getCarType())) {
            // if car the same, only change driver so no sneaky fuel exploit
            return node.getNodeState().changeDriver(a.getDriverType());
        }
        return node.getNodeState().changeCarAndDriver(a.getCarType(),
                a.getDriverType());
    }

    /**
     * Perform the CHANGE_TIRE_FUEL_PRESSURE action
     *
     * @param a a CHANGE_TIRE_FUEL_PRESSURE action object
     * @return the next state
     */
//    private State performA8(Action a) {
//        return currentState.changeTireFuelAndTirePressure(a.getTireModel(),
//                a.getFuel(), a.getTirePressure());
//    }

    /**
     * Check whether a given state is the goal state or not
     *
     * @param s the state to check
     * @return True if s is goal state, False otherwise
     */
    public Boolean isGoalNode(Node n) {
    	
    	if(n instanceof A1Node) {
    		System.out.println("A1 is not a goal node");
    		return false;
    	}
    	
        if (n == null) {
            return false;
        }
        return n.getNodeState().getPos() >= ps.getN();
    }

    /**
     * Get the current number of steps taken in latest simulation
     *
     * @return steps taken in latest simulation
     */
//    public int getSteps() {
//        return steps;
//    }

    
}
