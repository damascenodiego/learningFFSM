#!/bin/bash

# ./emse01pairwise.sh


for an_spl in "agm" "vm" "ws" "cpterminal" "minepump" "aerouc5"; do
   cd $an_spl/
   for i in ./products_*/*.config; do 
   	java -cp ../learnFFSM.jar uk.le.ac.fts.FsmFromFTS -fts fts/*.fts -conf $i;
   done
   for a_prtz in ./products_*.prtz;do
      python ../emse_prtz.py $a_prtz
   done
   cd -
done