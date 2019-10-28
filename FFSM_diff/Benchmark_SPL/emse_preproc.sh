# # for an_spl in "aerouc5" "agm" "cpterminal" "minepump" "vm" "ws";
# for an_spl in "bcs2";
# do
# 	qsub -N "xd_$an_spl" -v "name=emse_dissim_$an_spl" ./emse.sh
# 	qsub -N "xp_$an_spl" -v "name=emse_pairs_$an_spl" ./emse.sh
# done


for an_spl in "aerouc5" "agm" "cpterminal" "minepump" "vm" "ws" "bcs2";
do
 echo "Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/StatesFFSM:Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/StatesFFSM" > ./pair_merging_$an_spl.log
 grep "^Reference" ./pair_merging_$an_spl.txt >> ./pair_merging_$an_spl.log
 sed -i 's/Reference\/Updated\/TotalStatesRef\/TotalStatesUpdt\/TotalFeaturesRef\/TotalFeaturesUpdt\/CommonFeatures\/RatioFeatures\/RatioStates\/StatesFFSM://g' ./pair_merging_$an_spl.log
 echo -e "Pair dissimilarity:Reference\tUpdated\tConfigDissim" > ./pair_dissimilarity_$an_spl.log
 grep "^Pair dissimilarity:" ./pair_dissimilarity_$an_spl.txt >> ./pair_dissimilarity_$an_spl.log
 sed -i 's/Pair dissimilarity://g' ./pair_dissimilarity_$an_spl.log
done
