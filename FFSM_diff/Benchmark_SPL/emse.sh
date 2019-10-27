#!/bin/bash

PBS -l ncpus=2
PBS -l walltime=96:00:00
PBS -m ae
PBS -M damascenodiego@usp.br

# qsub -N "pyname" -v "pyname=pyname.py" ./emse.sh

module load gcc/4.9.2 R/3.3.3 java-oracle/jdk1.8.0_65

my_dir=/home/damascdn/remote_euler/learningFFSM/FFSM_diff/Benchmark_SPL/
cd $my_dir

if [[ $py -eq 0 ]] ; 
then
	exit(1)
fi 

python $pyname