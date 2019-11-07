#!/bin/bash

# for an_spl in "agm" "vm" "ws" "bcs2";
# for an_spl in "cpterminal" "minepump";
# for an_spl in "agm" "vm" "ws" "bcs2" "cpterminal" "minepump";
# for an_spl in "agm" "vm" "ws" "bcs2" "cpterminal" "minepump" "aerouc5";
for an_spl in "agm" "vm" "ws" "cpterminal" "minepump" "aerouc5";
# for an_spl in "aerouc5";
do
   qsub -N "p_$an_spl" -v "name=emse_pairs_$an_spl" ./emse.sh
done