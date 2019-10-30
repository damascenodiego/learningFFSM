#!/bin/bash

for an_spl in "agm" "vm" "ws" "aerouc5" "bcs2" "cpterminal" "minepump";
do
   qsub -N "p_$an_spl" -v "name=emse_pairs_$an_spl" ./emse.sh
done