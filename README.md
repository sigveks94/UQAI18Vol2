# COMP3702/7702 Assignment 2 Supporting code

Here is some code to help you get across the line

### What's included

**Problem Package**

The main problem class and parser.

To use it simply create a new ProblemSpec object and pass in the path to the input file of your choosing as an argument.

```$xslt
ProblemSpec ps = new ProblemSpec("path/to/inputFile.txt");
```

Enjoy the problem instance object

**Simulator package**

The simulator for you to test your policies. 

Initialize the simulator by passing your problem spec to its constructor.

```$xslt
ProblemSpec ps = new ProblemSpec("path/to/inputFile.txt");
Simulator sim = new Simulator(ps);
```

To utilize the simulator, simply call the step function which accepts an
Action class instance and returns the next state.

```$xslt
Action a = new Action(ActionType.MOVE)    // or some other action
State nextState = sim.step(a);
```

Check out the SimulatorTest for an example of using random actions.