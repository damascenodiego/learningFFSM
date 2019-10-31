#!/bin/bash

# for an_spl in "agm" "vm" "ws" "aerouc5" "bcs2" "cpterminal" "minepump";
# do
#  echo "Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/StatesFFSM:Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/StatesFFSM" > ./pair_merging_$an_spl.log
#  grep "^Reference" ./pair_merging_$an_spl.txt >> ./pair_merging_$an_spl.log
#  sed -i 's/Reference\/Updated\/TotalStatesRef\/TotalStatesUpdt\/TotalFeaturesRef\/TotalFeaturesUpdt\/CommonFeatures\/RatioFeatures\/RatioStates\/StatesFFSM://g' ./pair_merging_$an_spl.log
#  echo -e "Pair dissimilarity:Reference\tUpdated\tConfigDissim" > ./pair_dissimilarity_$an_spl.log
#  grep "^Pair dissimilarity:" ./pair_dissimilarity_$an_spl.txt >> ./pair_dissimilarity_$an_spl.log
#  sed -i 's/Pair dissimilarity://g' ./pair_dissimilarity_$an_spl.log
# done


for i in */emse/recover/[gld][nm]dp*.txt; do
   echo "Starting $i";
   qsub -N "$i" -v "name=$i" ./emse_03prtz.sh  ; 
   while [ `qstat -u damascdn | wc -l` -gt 23 ]; do 
      sleep 5s;
   done; 
done;

for fname in */emse/recover/[gld][nm]dp*/report_fmeasure.log; do
   cp  $fname `echo $fname | cut -d\/ -f4`.txt
done


for i in [gld][nm]dp*.log; do
   echo "Precision|Recall|F-measure:Precision|Recall|F-measure" > $i.tab
   grep "Precision|Recall|F-measure" $i >> $i.tab
   sed -i "s/^Precision|Recall|F-measure://g" $i.tab
done

for i in */emse/recover; do
   zip -rm $i.zip $i & 
done
