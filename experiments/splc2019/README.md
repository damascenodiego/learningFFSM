# Learning from Difference: An Automated Approach for Learning Family Models from Software Product Lines

This folder contains the laboratory package from the paper entitled *Learning from difference: An automated approach for learning family models from software product lines* that has been published as full paper in the research track of the [23rd International Systems and Software Product Line Conference (SPLC 2019)](https://splc2019.net).

This repository is organized as follows:

In the root folder **[FFSM_diff](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff)**, we have a Java project that can be opened using the [Eclipse IDE](https://www.eclipse.org/ide) and [JDK version 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

In folder **[FFSM_diff/lib](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/lib)**, we find some of the Java libraries used in our Eclipse project. These include [FeatureIDE](https://featureide.github.io) and [Apache Commons Math](http://commons.apache.org/proper/commons-math/). Other libraries, such as [LearnLib](https://learnlib.de) are included using [Apache Maven](https://maven.apache.org/).

In folder **[FFSM_diff/src](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/src/)**, we find the source-code of the FFSM_Diff project. Essentially, there is one main package: [uk.le.ac](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/src/main/java/uk/le/ac), that includes code artifacts we developed in our study inspired by the ideas of [the work of Fragal et al. on Featured Finite State Machines](http://doi.org/10.1007/978-3-319-57666-4_13).

In **[experiments/splc2019/data](https://github.com/damascenodiego/learningFFSM/tree/master/experiments/splc2019/data)**, you find the data used in the experiment of this paper. This includes the FM, FSM and FFSM models. PNG files of the state machines and feature models are available as well.

In **[experiments/splc2019/data/learningFFSMs.Rproj](https://github.com/damascenodiego/learningFFSM/tree/master/experiments/splc2019/data/learningFFSMs.Rproj)**, there is a RStudio project for the experiment data.

The **experiments/splc2019/data/scripts/run_<spl>.py** files are the test scripts that we designed run our experiments for recovering the complete FFSM of each SPL.

The **experiments/splc2019/data/scripts/run_<spl>_pairs.py** files are the test scripts that we designed run our experiments for learning FFSMs from all pairs of product FSMs.

For more details about the scripts used in this study, please check the folder [experiments/splc2019/analysis_plan](https://github.com/damascenodiego/learningFFSM/tree/master/experiments/splc2019/analysis_plan/). 


To run the [learnFFSM.jar](https://github.com/damascenodiego/learningFFSM/blob/master/experiments/splc2019/data/learnFFSM.jar) program, use the following parameters:

     usage: LearnFFSM
      -clean <arg>   Simplify FFSM labels
      -fm <arg>      Feature model
      -fref <arg>    FFSM reference
      -h             Help menu
      -mref <arg>    Mealy reference
      -out <arg>     Output file
      -updt <arg>    Mealy update


If you have any questions, feel free to contact me via damascenodiego@alumni.usp.br