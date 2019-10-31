#!/bin/bash


for an_spl in "agm" "vm" "ws" "bcs2"; do
   mkdir -p $an_spl/emse/recover
   for idx in {00..99}; do
      ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -shuffle -gmdp 2> $an_spl/emse/recover/gmdp_dis_"$idx"_$an_spl.err 1> $an_spl/emse/recover/gmdp_dis_"$idx"_$an_spl.txt
      ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -shuffle 2> $an_spl/emse/recover/lmdp_dis_"$idx"_$an_spl.err 1> $an_spl/emse/recover/lmdp_dis_"$idx"_$an_spl.txt
      ls -1 $an_spl/fsm/fsm_$an_spl*txt | shuf > $an_spl/emse/recover/rndp_dis_"$idx"_$an_spl.txt
      # ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -shuffle -gmdp -similar 2> $an_spl/emse/recover/gmdp_sim_"$idx"_$an_spl.err 1> $an_spl/emse/recover/gmdp_sim_"$idx"_$an_spl.txt
      # ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -shuffle -similar 2> $an_spl/emse/recover/lmdp_sim_"$idx"_$an_spl.err 1> $an_spl/emse/recover/lmdp_sim_"$idx"_$an_spl.txt
   done
   # for a_prtzd in $an_spl/emse/recover/[gl]mdp_*_$an_spl.txt; do
   #    java -cp learnFFSM.jar uk.le.ac.prioritize.CalculateDissimilarity -fm $an_spl/model.xml -order $a_prtzd > $a_prtzd.score
   # done    
done

for an_spl in "cpterminal" "minepump"; do
# for an_spl in "aerouc5" "cpterminal" "minepump"; do
   mkdir -p $an_spl/emse/recover
   tar -xvf $an_spl/products.tar.gz -C $an_spl/
   for idx in {00..99}; do
      ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -shuffle -gmdp 2> $an_spl/emse/recover/gmdp_dis_"$idx"_$an_spl.err 1> $an_spl/emse/recover/gmdp_dis_"$idx"_$an_spl.txt
      ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -shuffle 2> $an_spl/emse/recover/lmdp_dis_"$idx"_$an_spl.err 1> $an_spl/emse/recover/lmdp_dis_"$idx"_$an_spl.txt
      ls -1 $an_spl/products/products_all/*_kiss.txt | shuf > $an_spl/emse/recover/rndp_dis_"$idx"_$an_spl.txt
      # ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -shuffle -gmdp -similar 2> $an_spl/emse/recover/gmdp_sim_"$idx"_$an_spl.err 1> $an_spl/emse/recover/gmdp_sim_"$idx"_$an_spl.txt
      # ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -shuffle -similar 2> $an_spl/emse/recover/lmdp_sim_"$idx"_$an_spl.err 1> $an_spl/emse/recover/lmdp_sim_"$idx"_$an_spl.txt
   done
   # for a_prtzd in $an_spl/emse/recover/[gl]mdp_*_$an_spl.txt; do
   #    java -cp learnFFSM.jar uk.le.ac.prioritize.CalculateDissimilarity -fm $an_spl/model.xml -order $a_prtzd > $a_prtzd.score
   # done
done


# # fix path: from UoL to Euler
# sed -i 's/.var.autofs.home.home.cdnd1.git.learningFFSM.FFSM_diff/\/home\/damascdn\/remote_euler/g' */emse/recover/[lg]mdp*.txt
# ls -1 */emse/recover/[lg]mdp*.txt