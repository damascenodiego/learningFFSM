#!/bin/bash


for an_spl in "agm" "vm" "ws" "bcs2"; do
   mkdir -p $an_spl/emse/recover
   ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -gmdp          2> $an_spl/emse/recover/gmdp_dis_$an_spl.err 1> $an_spl/emse/recover/gmdp_dis_$an_spl.txt
   ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml                2> $an_spl/emse/recover/lmdp_dis_$an_spl.err 1> $an_spl/emse/recover/lmdp_dis_$an_spl.txt
   ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -gmdp -similar 2> $an_spl/emse/recover/gmdp_sim_$an_spl.err 1> $an_spl/emse/recover/gmdp_sim_$an_spl.txt
   ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml       -similar 2> $an_spl/emse/recover/lmdp_sim_$an_spl.err 1> $an_spl/emse/recover/lmdp_sim_$an_spl.txt
   # for idx in {00..99}; do
   #    ls -1 $an_spl/fsm/fsm_$an_spl*txt | shuf > $an_spl/emse/recover/rndp_dis_"$idx"_$an_spl.txt
   # done
done

# for an_spl in "cpterminal" "minepump"; do
for an_spl in "aerouc5" "cpterminal" "minepump"; do
   mkdir -p $an_spl/emse/recover
   tar -xvf $an_spl/products.tar.gz -C $an_spl/
   ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -gmdp          2> $an_spl/emse/recover/gmdp_dis_$an_spl.err 1> $an_spl/emse/recover/gmdp_dis_$an_spl.txt
   ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml                2> $an_spl/emse/recover/lmdp_dis_$an_spl.err 1> $an_spl/emse/recover/lmdp_dis_$an_spl.txt
   ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml -gmdp -similar 2> $an_spl/emse/recover/gmdp_sim_$an_spl.err 1> $an_spl/emse/recover/gmdp_sim_$an_spl.txt
   ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml       -similar 2> $an_spl/emse/recover/lmdp_sim_$an_spl.err 1> $an_spl/emse/recover/lmdp_sim_$an_spl.txt
   # for idx in {00..99}; do
   #    ls -1 $an_spl/products/products_all/*_kiss.txt | shuf > $an_spl/emse/recover/rndp_dis_"$idx"_$an_spl.txt
   # done
done