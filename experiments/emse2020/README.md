# Learning by Sampling: Learning Behavioral Family Models from Software Product Lines

This folder contains the laboratory package from the journal paper entitled *Learning by Sampling: Learning Behavioral Family Models from Software Product Lines* that has been submitted to the Empirical Software Engineering Journal: special issue on “Configurable Systems”. 

This repository is organized as follows:

In the root folder **[FFSM_diff](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff)**, we have a Java project that can be opened using the [Eclipse IDE](https://www.eclipse.org/ide) and [JDK version 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

In folder **[FFSM_diff/lib](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/lib)**, we find some of the Java libraries used in our Eclipse project. These include [FeatureIDE](https://featureide.github.io) and [Apache Commons Math](http://commons.apache.org/proper/commons-math/). Other libraries, such as [LearnLib](https://learnlib.de) are included using [Apache Maven](https://maven.apache.org/).

In folder **[FFSM_diff/src](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/src/)**, we find the source-code of the FFSM_Diff project. Essentially, there is one main package: [uk.le.ac](https://github.com/damascenodiego/learningFFSM/tree/master/FFSM_diff/src/main/java/uk/le/ac), that includes code artifacts we developed in our study inspired by the ideas of [the work of Fragal et al. on Featured Finite State Machines](http://doi.org/10.1007/978-3-319-57666-4_13).

In **[experiments/emse2020/data](https://github.com/damascenodiego/learningFFSM/tree/master/experiments/emse2020/data)**, you find the data used in the experiment of this paper. This includes the FM, FSM and FFSM models. PNG files of the state machines and feature models are available as well.

In **[experiments/emse2020/data/exp_emse/learningFFSMs.Rproj](https://github.com/damascenodiego/learningFFSM/tree/master/experiments/emse2020/data/exp_emse/learningFFSMs.Rproj)**, there is a RStudio project for the experiment data.

In **[experiments/emse2020/data/wise2learn.sh](https://github.com/damascenodiego/learningFFSM/tree/master/experiments/emse2020/data/wise2learn.sh)**, there are the bash commands we organized to automate our experiments. 

For more details about the scripts used in this study, please check the folder [experiments/emse2020/analysis_plan](https://github.com/damascenodiego/learningFFSM/tree/master/experiments/emse2020/analysis_plan/). 

If you have any questions, feel free to contact me via damascenodiego@alumni.usp.br