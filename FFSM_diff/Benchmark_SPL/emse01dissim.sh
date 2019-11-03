#!/bin/bash

# for an_spl in "agm" "vm" "ws" "bcs2";
# for an_spl in "cpterminal" "minepump";
for an_spl in "agm" "vm" "ws" "bcs2" "cpterminal" "minepump";
# for an_spl in "agm" "vm" "ws" "bcs2" "cpterminal" "minepump" "aerouc5";
do
   qsub -N "d_$an_spl" -v "name=emse_dissim_$an_spl" ./emse.sh
done