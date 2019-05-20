# Learning from difference

This folder contains the laboratory package from the paper entitled 
*Learning from difference: An automated approach for learning family models from software product lines* that
has been accepted as full paper for the research track of the 23rd International Systems and Software Product Line Conference (SPLC 2019).
This project is organized as follows:

In folder **FFSM_diff**, we have a Java project that can be opened using the Eclipse IDE and JDK version 1.8.


In **Benchmark_SPL**, 
we included the subject systems (i.e., agm, vm, ws) and their respective 
FSMs and FFSMs as KISS files and feature models as XML files.
PNG images of the state machines and feature models are available as well.

In **learningFFSMs**, there is a RStudio project for the experiment data.
The learnFFSM.jar file is a compiled version of FFSM_Diff algorithm. 
To display the help menu, run **java -jar learnFFSM.jar -h**.

The **run_*.py** are the test scripts that run the 
learnFFSM.jar file for each SPL. 

In folder **lib**, we find some of the Java libraries used in our Eclipse project. 
These include FeatureIDE and Apache Commons Math.
Other libraries, such as LearnLib are included using Apache Maven.

In folder **src**, we find the source-code of the FFSM_Diff project.
Essentially, there are two packages: 
uk.le.ac, that relates to our algorithm; and 
br.usp.icmc, that relates to Fragal et al. paper.

