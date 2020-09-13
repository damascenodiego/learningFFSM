#!/bin/bash


for an_spl in "minepump" "aerouc5" "cpterminal"; do
	cd $an_spl/
	for i in ./products_*/*.config; do 
		java -cp ../learnFFSM.jar uk.le.ac.fts.FsmFromFTS -fts fts/*.fts -conf $i;
	done
	for a_prtz in ./products_*.prtz; do
		echo $an_spl" - "$a_prtz
		python ../emse_prtz.py $a_prtz &
	done
	cd -
done

for an_spl in "agm" "vm" "ws" ; do
	cd $an_spl/
	for a_prtz in ./products_*.prtz; do
		echo $an_spl" - "$a_prtz
		python ../emse_prtz.py $a_prtz &
	done
	cd -
done


for an_spl in "agm" "vm" "ws" "cpterminal" "minepump" "aerouc5"; do
	qsub -N "p_$an_spl" -v "name=emse_pairs_$an_spl"  ./emse.sh
done

for an_spl in "agm" "vm" "ws" "cpterminal" "minepump" "aerouc5"; do
	qsub -N "d_$an_spl" -v "name=emse_dissim_$an_spl" ./emse.sh
done


wait

mkdir ./exp_emse/pair/
# for an_spl in "aerouc5";
# for an_spl in "agm" "bcs2";
# for an_spl in "agm" "vm" "ws" "bcs2";
# for an_spl in "agm" "vm" "ws" "bcs2" "cpterminal" "minepump";
# for an_spl in "agm" "vm" "ws" "bcs2" "cpterminal" "minepump" "aerouc5";
for an_spl in "agm" "vm" "ws" "cpterminal" "minepump" "aerouc5"; do
 echo "Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM:Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM" > ./exp_emse/pair/pair_merging_$an_spl.log
 grep "^Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM" ./$an_spl/emse/pair_merging_$an_spl.txt >> ./exp_emse/pair/pair_merging_$an_spl.log
 sed -i 's/Reference\/Updated\/TotalStatesRef\/TotalStatesUpdt\/TotalTransitionsRef\/TotalTransitionsUpdt\/TotalFeaturesRef\/TotalFeaturesUpdt\/CommonFeatures\/RatioFeatures\/RatioStates\/RatioTransitions\/StatesFFSM\/TransitionsFFSM://g' ./exp_emse/pair/pair_merging_$an_spl.log
 
 echo -e "Pair dissimilarity:Reference\tUpdated\tConfigDissim" > ./exp_emse/pair/pair_dissimilarity_$an_spl.log
 grep "^Pair dissimilarity:" ./$an_spl/emse/pair_dissimilarity_$an_spl.txt >> ./exp_emse/pair/pair_dissimilarity_$an_spl.log
 sed -i 's/Pair dissimilarity://g' ./exp_emse/pair/pair_dissimilarity_$an_spl.log

 echo -e "ModelRef|ModelUpdt|Precision|Recall|F-measure:Reference|Updated|Precision|Recall|F-measure" > ./exp_emse/pair/pair_comparelanguages_$an_spl.log
 grep "^ModelRef|ModelUpdt|Precision|Recall|F-measure:" ./$an_spl/emse/pair_comparelanguages_$an_spl.txt >> ./exp_emse/pair/pair_comparelanguages_$an_spl.log
 sed -i 's/ModelRef|ModelUpdt|Precision|Recall|F-measure://g' ./exp_emse/pair/pair_comparelanguages_$an_spl.log

done


for an_spl in "agm" "vm" "ws" "cpterminal" "minepump" "aerouc5"; do
	for wise in "1wise" "2wise" "3wise" "4wise" "all"; do
		echo "Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM:Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM" > ./$an_spl/products_$wise/report.tab
		grep "^Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalTransitionsRef/TotalTransitionsUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/RatioTransitions/StatesFFSM/TransitionsFFSM" ./$an_spl/products_$wise/report.log >> ./$an_spl/products_$wise/report.tab
		sed -i 's/Reference\/Updated\/TotalStatesRef\/TotalStatesUpdt\/TotalTransitionsRef\/TotalTransitionsUpdt\/TotalFeaturesRef\/TotalFeaturesUpdt\/CommonFeatures\/RatioFeatures\/RatioStates\/RatioTransitions\/StatesFFSM\/TransitionsFFSM://g' ./$an_spl/products_$wise/report.tab

		echo -e "Precision|Recall|F-measure:Precision|Recall|F-measure" > ./$an_spl/products_$wise/report_prf.tab
		grep "^Precision|Recall|F-measure:" ./$an_spl/products_$wise/report.log >> ./$an_spl/products_$wise/report_prf.tab
		sed -i 's/Precision|Recall|F-measure://g' ./$an_spl/products_$wise/report_prf.tab


		echo -e "ModelRef|ModelUpdt|Precision|Recall|F-measure:Reference|Updated|Precision|Recall|F-measure" > ./$an_spl/products_$wise/report_fmeasure_l.tab
		grep "^ModelRef|ModelUpdt|Precision|Recall|F-measure:" ./$an_spl/products_$wise/report_fmeasure_l.log >> ./$an_spl/products_$wise/report_fmeasure_l.tab
		sed -i 's/ModelRef|ModelUpdt|Precision|Recall|F-measure://g' ./$an_spl/products_$wise/report_fmeasure_l.tab
	done
done