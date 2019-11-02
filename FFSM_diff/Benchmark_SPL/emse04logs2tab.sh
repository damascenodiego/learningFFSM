#!/bin/bash

# for an_spl in "agm" "vm" "ws" "bcs2";
# for an_spl in "agm" "vm" "ws" "bcs2"; "cpterminal" "minepump";
for an_spl in "agm" "vm" "ws" "bcs2" "cpterminal" "minepump" "aerouc5";
do
 echo "Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/StatesFFSM:Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/StatesFFSM" > ./pair_merging_$an_spl.log
 grep "^Reference" ./pair_merging_$an_spl.txt >> ./pair_merging_$an_spl.log
 sed -i 's/Reference\/Updated\/TotalStatesRef\/TotalStatesUpdt\/TotalFeaturesRef\/TotalFeaturesUpdt\/CommonFeatures\/RatioFeatures\/RatioStates\/StatesFFSM://g' ./pair_merging_$an_spl.log
 
 echo -e "Pair dissimilarity:Reference\tUpdated\tConfigDissim" > ./pair_dissimilarity_$an_spl.log
 grep "^Pair dissimilarity:" ./pair_dissimilarity_$an_spl.txt >> ./pair_dissimilarity_$an_spl.log
 sed -i 's/Pair dissimilarity://g' ./pair_dissimilarity_$an_spl.log

 echo -e "ModelRef|ModelUpdt|Precision|Recall|F-measure:Reference|Updated|Precision|Recall|F-measure" > ./pair_comparelanguages_$an_spl.log
 grep "^ModelRef|ModelUpdt|Precision|Recall|F-measure:" ./pair_comparelanguages_$an_spl.txt >> ./pair_comparelanguages_$an_spl.log
 sed -i 's/ModelRef|ModelUpdt|Precision|Recall|F-measure://g' ./pair_comparelanguages_$an_spl.log

done


for i in */emse/recover/[gld][nm]dp*.txt; do
   echo "Starting $i";
   qsub -N "$i" -v "name=$i" ./emse_03prtz.sh  ; 
   while [ `qstat -u damascdn | wc -l` -gt 23 ]; do 
      sleep 5s;
   done; 
done;

for fname in */emse/recover/[gld][nm]dp*/report_fmeasure_l.log; do
   cp  $fname ./exp_emse/`echo $fname | cut -d\/ -f4`.txt
done


for i in ./exp_emse/[gld][nm]dp*.txt; do
   echo "ModelRef|ModelUpdt|Precision|Recall|F-measure:Reference|Updated|Precision|Recall|F-measure" > $i.tab
   grep "ModelRef|ModelUpdt|Precision|Recall|F-measure:" $i >> $i.tab
   sed -i "s/^ModelRef|ModelUpdt|Precision|Recall|F-measure://g" $i.tab
done

for i in */emse/recover; do
   zip -rm $i.zip $i & 
done
