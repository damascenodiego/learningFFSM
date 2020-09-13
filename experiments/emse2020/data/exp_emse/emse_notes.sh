# # for an_spl in "aerouc5" "agm" "cpterminal" "minepump" "vm" "ws";
# for an_spl in "bcs2";
# do
#    qsub -N "xd_$an_spl" -v "name=emse_dissim_$an_spl" ./emse.sh
#    qsub -N "xp_$an_spl" -v "name=emse_pairs_$an_spl" ./emse.sh
# done


for an_spl in "agm" "vm" "ws" "bcs2" "aerouc5" "cpterminal" "minepump";
do
 echo "Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/StatesFFSM:Reference/Updated/TotalStatesRef/TotalStatesUpdt/TotalFeaturesRef/TotalFeaturesUpdt/CommonFeatures/RatioFeatures/RatioStates/StatesFFSM" > ./pair_merging_$an_spl.log
 grep "^Reference" ./$an_slp/emse/pair_merging_$an_spl.txt >> ./pair_merging_$an_spl.log
 
 sed -i 's/Reference\/Updated\/TotalStatesRef\/TotalStatesUpdt\/TotalFeaturesRef\/TotalFeaturesUpdt\/CommonFeatures\/RatioFeatures\/RatioStates\/StatesFFSM://g' ./pair_merging_$an_spl.log
 echo -e "Pair dissimilarity:Reference\tUpdated\tConfigDissim" > ./pair_dissimilarity_$an_spl.log
 grep "^Pair dissimilarity:" ./$an_slp/emse/pair_dissimilarity_$an_spl.txt >> ./pair_dissimilarity_$an_spl.log
 sed -i 's/Pair dissimilarity://g' ./pair_dissimilarity_$an_spl.log
done



#for an_spl in "aerouc5" "cpterminal" "minepump";
#for an_spl in "agm" "vm" "ws";
for an_spl in "aerouc5" "agm" "cpterminal" "minepump" "vm" "ws";
do
   #qsub -N "xd_$an_spl" -v "name=emse_dissim_$an_spl" ./emse.sh
   qsub -N "xp_$an_spl" -v "name=emse_pairs_$an_spl" ./emse.sh
done



qsub -N "xd_$an_spl"      -v "pyname=emse_$an_spl_pairs_dissim"       ./emse.sh

qsub -N "xaerouc5_p"      -v "pyname=emse_aerouc5_pairs"       ./emse.sh
qsub -N "xagm_p"         -v "pyname=emse_agm_pairs"          ./emse.sh
qsub -N "xcpterminal_p"      -v "pyname=emse_cpterminal_pairs"    ./emse.sh
qsub -N "xminepump_p"       -v "pyname=emse_minepump_pairs"       ./emse.sh
qsub -N "xvm_p"          -v "pyname=emse_vm_pairs"          ./emse.sh
qsub -N "xws_p"          -v "pyname=emse_ws_pairs"          ./emse.sh



for an_spl in "aerouc5" "agm" "cpterminal" "minepump" "vm" "ws";
do
   qsub -N "xd_$an_spl" -v "name=emse_dissim_$an_spl" ./emse.sh
   qsub -N "xp_$an_spl" -v "name=emse_pairs_$an_spl" ./emse.sh
done


for an_spl in "aerouc5" "agm" "cpterminal" "minepump" "vm" "ws";
do
 grep "^Reference" ./pair_merging_$an_spl.txt > ./pair_merging_$an_spl.log
 sed -i 's/Reference\/Updated\/TotalStatesRef\/TotalStatesUpdt\/TotalFeaturesRef\/TotalFeaturesUpdt\/CommonFeatures\/RatioFeatures\/RatioStates\/StatesFFSM://g' ./pair_merging_$an_spl.log
 grep "^Pair dissimilarity:" ./pair_dissimilarity_$an_spl.txt > ./pair_dissimilarity_$an_spl.log
 sed -i 's/Pair dissimilarity://g' ./pair_dissimilarity_$an_spl.log
done


for an_spl in "agm" "vm" "ws" "bcs2";
do
   mkdir -p $an_spl/emse/recover
   # ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml  -gmdp 2> $an_spl/emse/recover/gmdp_dis_$an_spl.err 1> $an_spl/emse/recover/gmdp_dis_$an_spl.txt
   ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml        2> $an_spl/emse/recover/lmdp_dis_$an_spl.err 1> $an_spl/emse/recover/lmdp_dis_$an_spl.txt
   # ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml  -gmdp -similar 2> $an_spl/emse/recover/gmdp_sim_$an_spl.err 1> $an_spl/emse/recover/gmdp_sim_$an_spl.txt
   ls -1 $an_spl/fsm/fsm_$an_spl*txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml        -similar 2> $an_spl/emse/recover/lmdp_sim_$an_spl.err 1> $an_spl/emse/recover/lmdp_sim_$an_spl.txt
   for a_prtzd in $an_spl/emse/recover/[gl]mdp_*_$an_spl.txt;
   do
      java -cp learnFFSM.jar uk.le.ac.prioritize.CalculateDissimilarity -fm $an_spl/model.xml -order $a_prtzd > $a_prtzd.score
   done    
done

for an_spl in "aerouc5" "cpterminal" "minepump";do
   mkdir -p $an_spl/emse/recover
   tar -xvf $an_spl/products.tar.gz
   # ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml  -gmdp 2> $an_spl/emse/recover/gmdp_dis_$an_spl.err 1> $an_spl/emse/recover/gmdp_dis_$an_spl.txt
   ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml        2> $an_spl/emse/recover/lmdp_dis_$an_spl.err 1> $an_spl/emse/recover/lmdp_dis_$an_spl.txt
   # ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml  -gmdp -similar 2> $an_spl/emse/recover/gmdp_sim_$an_spl.err 1> $an_spl/emse/recover/gmdp_sim_$an_spl.txt
   ls -1 $an_spl/products/products_all/*_kiss.txt | java -cp learnFFSM.jar uk.le.ac.prioritize.PrtzProducts -fm $an_spl/model.xml        -similar 2> $an_spl/emse/recover/lmdp_sim_$an_spl.err 1> $an_spl/emse/recover/lmdp_sim_$an_spl.txt
   for a_prtzd in $an_spl/emse/recover/[gl]mdp_*_$an_spl.txt; do
      java -cp learnFFSM.jar uk.le.ac.prioritize.CalculateDissimilarity -fm $an_spl/model.xml -order $a_prtzd > $a_prtzd.score
   done
done





for an_spl in "agm" "vm" "ws" "bcs2" "aerouc5" "cpterminal" "minepump"; do 
   for j in $i/emse/recover/*_$i.txt; do 
      echo $j; 
      python emse_prtz.py $j;
   done;
done

for i in */emse/recover/[lg]mdp*.txt; do 
   qsub -N "$i" -v "name=$i" ./emse_prtz.sh ;
done


sed -i 's/.var.autofs.home.home.cdnd1.git.learningFFSM.FFSM_diff/\/home\/damascdn\/remote_euler/g' */emse/recover/[lg]mdp*.txt

ls -1 */emse/recover/[lg]mdp*.txt


java -cp mylearn.jar br.usp.icmc.labes.mealyInference.utils.BuildOT -sul $sul_j -out $run_path/ -dot 
java -jar mylearn.jar -cache -cexh RivestSchapire -learn lstar -clos CloseFirst -eq wphyp -out my_out -sul 

rsync -CravspuP /home/cdnd1/git/learningFFSM/FFSM_diff/Benchmark_SPL damascdn@euler.cemeai.icmc.usp.br:/home/damascdn/remote_euler/


  tab_gmdp_dis<-read.table(paste("../",an_spl,"/emse/recover/gmdp_dis_",an_spl,".txt.score",sep = ""),sep="/", header=TRUE)
  tab_gmdp_sim<-read.table(paste("../",an_spl,"/emse/recover/gmdp_sim_",an_spl,".txt.score",sep = ""),sep="/", header=TRUE)
  tab_lmdp_dis<-read.table(paste("../",an_spl,"/emse/recover/lmdp_dis_",an_spl,".txt.score",sep = ""),sep="/", header=TRUE)
  tab_lmdp_sim<-read.table(paste("../",an_spl,"/emse/recover/lmdp_sim_",an_spl,".txt.score",sep = ""),sep="/", header=TRUE)

tab_gmdp_dis,tab_gmdp_sim,tab_lmdp_dis,tab_lmdp_sim


geom_line(aes(y = gmdp_dis))+
geom_line(aes(y = gmdp_sim))+
geom_line(aes(y = lmdp_dis))+
geom_line(aes(y = lmdp_sim))