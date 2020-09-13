This file describes the analysis plan for the experiments of the conference paper "Learning from Difference: An Automated Approach for Learning Family Models from Software Product Lines" published in the 23rd International Systems and Software Product Line Conference - Volume A 2019.

Steps: 

**(1)** In data/scripts/ folder, execute the **run_<spl>_pair.py** files to learn an FFSM model from each pair of FSM model of each SPL spl.

**(2)** In data/scripts/ folder, execute the **run_<spl>.py** files to recover an FFSM model from each SPL spl by using exhaustive learning.

**(3)** The data/script.r file performs the statistical analysis we made using the dataset.tab and recovering_ffsm.tab files. 
    These tab files were handcrafted using the log files from each run in the two previous steps.

**(4)** The data/script.r script also generates multiple plots depiciting:

	- the correlation between feature sharing and model size in number of states 
	  See file -> correlation.pdf

	- the size of the recovered FFSM models in number of states
	  See file -> recovering_ffsm.pdf 

	- the size of the FFSM models learned from all pairs of FSMs
	  See files -> tot_size_prod.pdf and tot_size_prod_2.pdf

**(5)** The data/script.r script also prints a few other statistics in the standard output


If you have any questions, feel free to contact me via damascenodiego@alumni.usp.br