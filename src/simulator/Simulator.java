package simulator;

import problem.Action;
import problem.ActionType;
import problem.ProblemSpec;

import java.io.IOException;

/**
 * This class is the simulator for the problem.
 * The simulator takes in an action and returns the next state.
 */
public class Simulator {

    /** Problem spec for the current problem **/
    private ProblemSpec ps;
    /** The current state of the environment **/
    private State currentState;
    /** The number of steps taken **/
    private int steps;

    /**
     * Construct a new simulator instance from the given problem spec
     *
     * @param ps the ProblemSpec
     */
    public Simulator(ProblemSpec ps) {
        this.ps = ps;
        currentState = State.getStartState(ps.getFirstCarType(),
                ps.getFirstDriver());
        steps = 0;
    }

    /**
     * Construct a new simulator instance from the given input file
     *
     * @param fileName path to input file
     * @throws IOException if can't find file or there is a format error
     */
    public Simulator(String fileName) throws IOException {
        this(new ProblemSpec(fileName));
    }

    /**
     * Perform an action against environment and recieve the next state
     *
     * @param a the action to perform
     * @return the next state
     */
    public State step(Action a) {

        State nextState;

        switch(a.getActionType().getActionNo()) {
            case 1:
                nextState = performA1(a);
                break;
            case 2:
                nextState = performA2(a);
                break;
            case 3:
                nextState = performA3(a);
                break;
            case 4:
                nextState = performA4(a);
                break;
            case 5:
                nextState = performA5(a);
                break;
            case 6:
                nextState = performA6(a);
                break;
            case 7:
                nextState = performA7(a);
                break;
            default:
                nextState = performA8(a);
        }

        steps += 1;
        currentState = nextState.copyState();
        return nextState;
    }

    /**
     * Perform CONTINUE_MOVING action
     *
     * @param a a CONTINUE_MOVING action object
     * @return the next state
     */
    private State performA1(Action a) {

        if (currentState.isInSlipCondition()) {
            return currentState.reduceSlipTimeLeft();
        }
        if (currentState.isInBreakdownCondition()) {
            return currentState.reduceBreakdownTimeLeft();
        }

        // Sample move distance
        int moveDistance = sampleMoveDistance();

        // handle slip and breakdown cases
        if (moveDistance == ProblemSpec.SLIP) {
            return currentState.changeSlipCondition(true,
                    ps.getSlipRecoveryTime());
        }
        if (moveDistance == ProblemSpec.BREAKDOWN) {
            return currentState.changeBreakdownCondition(true,
                    ps.getRepairTime());
        }
        return currentState.changePosition(moveDistance, ps.getN());
    }

    /**
     * Return the move distance by sampling from conditional probability
     * distribution.
     *
     * @return the move distance in range [-4, 5] or SLIP or BREAKDOWN
     */
    private int sampleMoveDistance() {
        return -1;
    }

    /**
     * Perform CHANGE_CAR action
     *
     * @param a a CHANGE_CAR action object
     * @return the next state
     */
    private State performA2(Action a) {
        return currentState.changeCarType(a.getCarType());
    }

    /**
     * Perform CHANGE_DRIVER action
     *
     * @param a a CHANGE_DRIVER action object
     * @return the next state
     */
    private State performA3(Action a) {
        return currentState.changeDriver(a.getDriverType());
    }

    /**
     * Perform the CHANGE_TYRES action
     *
     * @param a a CHANGE_TYRES action object
     * @return the next state
     */
    private State performA4(Action a) {
        return currentState.changeTires(a.getTireModel());
    }

    /**
     * Perform the ADD_FUEL action
     *
     * @param a a ADD_FUEL action object
     * @return the next state
     */
    private State performA5(Action a) {
        return currentState.addFuel(a.getFuel());
    }

    /**
     * Perform the CHANGE_PRESSURE action
     *
     * @param a a CHANGE_PRESSURE action object
     * @return the next state
     */
    private State performA6(Action a) {
        return currentState.changeTirePressure(a.getTirePressure());
    }

    /**
     * Perform the CHANGE_CAR_AND_DRIVER action
     *
     * @param a a CHANGE_CAR_AND_DRIVER action object
     * @return the next state
     */
    private State performA7(Action a) {
        return currentState.changeCarAndDriver(a.getCarType(),
                a.getDriverType());
    }

    /**
     * Perform the CHANGE_TYRE_FUEL_PRESSURE action
     *
     * @param a a CHANGE_TYRE_FUEL_PRESSURE action object
     * @return the next state
     */
    private State performA8(Action a) {
        return currentState.changeTireFuelAndTirePressure(a.getTireModel(),
                a.getFuel(), a.getTirePressure());
    }

    /**
     * Check whether a given state is the goal state or not
     *
     * @param s the state to check
     * @return True if s is goal state, False otherwise
     */
    public Boolean isGoalState(State s) {
        return s.getPos() >= ps.getN();
    }

}
