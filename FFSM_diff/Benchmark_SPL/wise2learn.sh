#!/bin/bash

# ./emse01pairwise.sh


for an_spl in "agm" "vm" "ws" "cpterminal" "minepump" "aerouc5"; do
	qsub -N "p_$an_spl" -v "name=emse_pairs_$an_spl" ./emse.sh
done

for an_spl in "minepump" "aerouc5" "cpterminal"; do
	cd $an_spl/
	for i in ./products_*/*.config; do 
		java -cp ../learnFFSM.jar uk.le.ac.fts.FsmFromFTS -fts fts/*.fts -conf $i;
	done
	for a_prtz in ./products_*.prtz;do
		echo $an_spl" - "$a_prtz
		python ../emse_prtz.py $a_prtz &
	done
	cd -
done


for an_spl in "agm" "vm" "ws"; do
	cd $an_spl/
	for a_prtz in ./products_*.prtz;do
		echo $an_spl" - "$a_prtz
		python ../emse_prtz.py $a_prtz &
	done
	cd -
done

wait
