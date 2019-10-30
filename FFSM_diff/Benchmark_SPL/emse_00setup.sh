#!/bin/bash

for idx in {00..00}; do
   for an_spl in "agm" "vm" "ws" "bcs2"; do
      mkdir -p $an_spl/emse/recover
      # ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml  -gmdp 2> $an_spl/emse/recover/gmdp_dis_"$idx"_$an_spl.err 1> $an_spl/emse/recover/gmdp_dis_"$idx"_$an_spl.txt
      ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml        2> $an_spl/emse/recover/lmdp_dis_"$idx"_$an_spl.err 1> $an_spl/emse/recover/lmdp_dis_"$idx"_$an_spl.txt
      # ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml  -gmdp -similar 2> $an_spl/emse/recover/gmdp_sim_"$idx"_$an_spl.err 1> $an_spl/emse/recover/gmdp_sim_"$idx"_$an_spl.txt
      ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml        -similar 2> $an_spl/emse/recover/lmdp_sim_"$idx"_$an_spl.err 1> $an_spl/emse/recover/lmdp_sim_"$idx"_$an_spl.txt
      for a_prtzd in $an_spl/emse/recover/[gl]mdp_*_$an_spl.txt; do
         java -cp learnFFSM.jar uk.le.ac.prioritize.CalculateDissimilarity -fm $an_spl/model.xml -order $a_prtzd > $a_prtzd.score
      done    
   done

   for an_spl in "aerouc5" "cpterminal" "minepump"; do
      mkdir -p $an_spl/emse/recover
      tar -xvf $an_spl/products.tar.gz -C $an_spl/
      # ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml  -gmdp 2> $an_spl/emse/recover/gmdp_dis_"$idx"_$an_spl.err 1> $an_spl/emse/recover/gmdp_dis_"$idx"_$an_spl.txt
      ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml        2> $an_spl/emse/recover/lmdp_dis_"$idx"_$an_spl.err 1> $an_spl/emse/recover/lmdp_dis_"$idx"_$an_spl.txt
      # ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml  -gmdp -similar 2> $an_spl/emse/recover/gmdp_sim_"$idx"_$an_spl.err 1> $an_spl/emse/recover/gmdp_sim_"$idx"_$an_spl.txt
      ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml        -similar 2> $an_spl/emse/recover/lmdp_sim_"$idx"_$an_spl.err 1> $an_spl/emse/recover/lmdp_sim_"$idx"_$an_spl.txt
      for a_prtzd in $an_spl/emse/recover/[gl]mdp_*_$an_spl.txt; do
         java -cp learnFFSM.jar uk.le.ac.prioritize.CalculateDissimilarity -fm $an_spl/model.xml -order $a_prtzd > $a_prtzd.score
      done
   done
done

# # fix path: from UoL to Euler
# sed -i 's/.var.autofs.home.home.cdnd1.git.learningFFSM.FFSM_diff/\/home\/damascdn\/remote_euler/g' */emse/recover/[lg]mdp*.txt
# ls -1 */emse/recover/[lg]mdp*.txt