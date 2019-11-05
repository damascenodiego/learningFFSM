#!/bin/bash

cd ~/remote_euler/
mv ~/remote_euler/Benchmark_SPL/ ~/remote_euler/xxx
rm -rf ~/remote_euler/xxx/ &
cd ~/git/learningFFSM/
git reset --hard
git clean -fd
git pull origin master
git pull origin master
cp -Rvv ~/git/learningFFSM/FFSM_diff/Benchmark_SPL ~/remote_euler/
cd ~/remote_euler/Benchmark_SPL


./emse00setup.sh
# ./emse01dissim.sh &
# ./emse02pairwise.sh &


for i in */emse/recover/[glr][nm]dp*.txt; do
   echo "Starting $i";
   qsub -N "$i" -v "name=$i" ./emse03prtz.sh  ; 
   while [ `qstat -u damascdn | wc -l` -gt 23 ]; do 
      sleep 5s;
   done; 
done;


while [ `qstat -u damascdn | wc -l` -gt 1 ]; do 
sleep 5s;
done;    

# mkdir ./exp_emse/pair/
# # for an_spl in "agm" "bcs2";
# # for an_spl in "agm" "vm" "ws" "bcs2";
# # for an_spl in "agm" "vm" "ws" "bcs2" "cpterminal" "minepump";
# for an_spl in "agm" "vm" "ws" "bcs2" "cpterminal" "minepump" "aerouc5";
# # for an_spl in "aerouc5";
# do
#  echo "Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM:Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM" > ./exp_emse/pair/pair_merging_$an_spl.log
#  grep "^Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM" ./$an_spl/emse/pair_merging_$an_spl.txt >> ./exp_emse/pair/pair_merging_$an_spl.log
#  sed -i 's/Reference\/Updated\/TotalStatesRef\/TotalStatesUpdt\/TotalTransitionsRef\/TotalTransitionsUpdt\/TotalFeaturesRef\/TotalFeaturesUpdt\/CommonFeatures\/RatioFeatures\/RatioStates\/RatioTransitions\/StatesFFSM\/TransitionsFFSM://g' ./exp_emse/pair/pair_merging_$an_spl.log
 
#  echo -e "Pair dissimilarity:Reference\tUpdated\tConfigDissim" > ./exp_emse/pair/pair_dissimilarity_$an_spl.log
#  grep "^Pair dissimilarity:" ./$an_spl/emse/pair_dissimilarity_$an_spl.txt >> ./exp_emse/pair/pair_dissimilarity_$an_spl.log
#  sed -i 's/Pair dissimilarity://g' ./exp_emse/pair/pair_dissimilarity_$an_spl.log

#  echo -e "ModelRef|ModelUpdt|Precision|Recall|F-measure:Reference|Updated|Precision|Recall|F-measure" > ./exp_emse/pair/pair_comparelanguages_$an_spl.log
#  grep "^ModelRef|ModelUpdt|Precision|Recall|F-measure:" ./$an_spl/emse/pair_comparelanguages_$an_spl.txt >> ./exp_emse/pair/pair_comparelanguages_$an_spl.log
#  sed -i 's/ModelRef|ModelUpdt|Precision|Recall|F-measure://g' ./exp_emse/pair/pair_comparelanguages_$an_spl.log

# done



mkdir ./exp_emse/prtz/

for fname in */emse/recover/[glr][nm]dp*/report.log; do
   cp  $fname ./exp_emse/prtz/`echo $fname | cut -d\/ -f4`_r.txt
done

for fname in ./exp_emse/prtz/[glr][nm]dp*_r.txt; do
   dest_file=$fname.log
   echo "Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM:Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM" > $dest_file
   grep "^Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM" $fname >> $dest_file
   sed -i 's/Reference\/Updated\/TotalStatesRef\/TotalStatesUpdt\/TotalTransitionsRef\/TotalTransitionsUpdt\/TotalFeaturesRef\/TotalFeaturesUpdt\/CommonFeatures\/RatioFeatures\/RatioStates\/RatioTransitions\/StatesFFSM\/TransitionsFFSM://g' $dest_file
done


cd exp_emse
zip -rm prtz.zip prtz/* & 
#zip -rm pair.zip pair/* & 

cd ..

zip -rm ~/emse.zip */emse/[lr]*