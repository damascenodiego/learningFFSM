#!/bin/bash

for an_spl in "agm" "vm" "ws" "bcs2" "cpterminal" "minepump";
# for an_spl in "agm" "vm" "ws" "aerouc5" "bcs2" "cpterminal" "minepump";
do
   qsub -N "d_$an_spl" -v "name=emse_dissim_$an_spl" ./emse.sh
done