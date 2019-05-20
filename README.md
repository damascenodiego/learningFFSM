

# Learning from difference

This folder contains the laboratory package from the paper entitled *Learning from difference: An automated approach for learning family models from software product lines* that has been accepted as full paper for the research track of the [23rd International Systems and Software Product Line Conference (SPLC 2019)](https://splc2019.net).

This repository is organized as follows:

In folder **[FFSM_diff](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff)**, we have a Java project that can be opened using the [Eclipse IDE](https://www.eclipse.org/ide) and [JDK version 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

In folder **[FFSM_diff/lib](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/lib)**, we find some of the Java libraries used in our Eclipse project. These include [FeatureIDE](https://featureide.github.io) and [Apache Commons Math](http://commons.apache.org/proper/commons-math/). Other libraries, such as [LearnLib](https://learnlib.de) are included using [Apache Maven](https://maven.apache.org/).

In folder **[FFSM_diff/src](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/src/)**, we find the source-code of the FFSM_Diff project. Essentially, there are two packages: [uk.le.ac](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/src/main/java/uk/le/ac), that includes code artifacts we developed in our study; and [br.usp.icmc](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/src/main/java/br/usp/icmc), that was reused from [the work of Fragal et al. on Featured Finite State Machines](http://doi.org/10.1007/978-3-319-57666-4_13).

In **[FFSM_diff/Benchmark_SPL](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/Benchmark_SPL)**, we included the subject systems (i.e., [agm](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/Benchmark_SPL/agm), [vm](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/Benchmark_SPL/vm), [ws](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/Benchmark_SPL/ws))  and their respective FSMs and FFSMs as KISS files and feature models as XML files. PNG images of the state machines and feature models are available as well.

In **[FFSM_diff/Benchmark_SPL/learningFFSMs](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/Benchmark_SPL/learningFFSMs)**, there is a RStudio project for the experiment data. The learnFFSM.jar file is a compiled version of FFSM_Diff algorithm. 

The **FFSM_diff/Benchmark_SPL/run_*.py** files are the test scripts that we designed run our experiments for recovering the complete FFSM of each SPL.


The **FFSM_diff/Benchmark_SPL/run_*_pairs.py** files are the test scripts that we designed run our experiments for learning FFSMs from all pairs of product FSMs.

To run the [learnFFSM.jar](https://github.com/damascenodiego/learningFFSM/blob/master/FFSM_diff/Benchmark_SPL/learnFFSM.jar) program, use the following parameters:

     usage: LearnFFSM
      -clean <arg>   Simplify FFSM labels
      -fm <arg>      Feature model
      -fref <arg>    FFSM reference
      -h             Help menu
      -mref <arg>    Mealy reference
      -out <arg>     Output file
      -updt <arg>    Mealy update
