#!/bin/bash

#PBS -l ncpus=1
#PBS -l walltime=48:00:00
#PBS -m ae
#PBS -M damascenodiego@usp.br

# qsub -N "pyname" -v "name=pyname" ./emse.sh

module load gcc/4.9.2 R/3.3.3 java-oracle/jdk1.8.0_65

my_dir=~/git/learningFFSM/FFSM_diff/Benchmark_SPL/
cd $my_dir

python $name".py"