#!/bin/bash

for an_spl in "agm" "vm" "ws" "aerouc5" "bcs2" "cpterminal" "minepump";
do
 echo "Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/StatesFFSM:Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/StatesFFSM" > ./pair_merging_$an_spl.log
 grep "^Reference" ./pair_merging_$an_spl.txt >> ./pair_merging_$an_spl.log
 sed -i 's/Reference\/Updated\/TotalStatesRef\/TotalStatesUpdt\/TotalFeaturesRef\/TotalFeaturesUpdt\/CommonFeatures\/RatioFeatures\/RatioStates\/StatesFFSM://g' ./pair_merging_$an_spl.log
 echo -e "Pair dissimilarity:Reference\tUpdated\tConfigDissim" > ./pair_dissimilarity_$an_spl.log
 grep "^Pair dissimilarity:" ./pair_dissimilarity_$an_spl.txt >> ./pair_dissimilarity_$an_spl.log
 sed -i 's/Pair dissimilarity://g' ./pair_dissimilarity_$an_spl.log
done
