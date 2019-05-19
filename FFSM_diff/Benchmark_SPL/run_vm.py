#!/bin/python

import random,subprocess,os


prodOrder=["{:01d}".format(x) for x in range(1,21)]
# random.shuffle(prodOrder)
prodAnalyzed=list()
prodAnalyzed.append(prodOrder[0])
prodAnalyzed.append(prodOrder[1])
prodOrder.remove(prodOrder[1])
prodOrder.remove(prodOrder[0])

SPL_NAME="vm"

directory="./"+SPL_NAME+"/learnt/"
if not os.path.exists(directory):
	os.makedirs(directory)


bashCommand = "java -jar ./learnFFSM.jar -fm ./"+SPL_NAME+"/model.xml -mref ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+prodAnalyzed[0]+".txt -updt ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+prodAnalyzed[1]+".txt -out ./"+SPL_NAME+"/learnt/ffsm_"+SPL_NAME+"_"+"_".join(prodAnalyzed)+".txt"
print(bashCommand)
process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
output, error = process.communicate()
print(output)

bashCommand = "java -jar ./learnFFSM.jar -fm ./"+SPL_NAME+"/model.xml -clean ./"+SPL_NAME+"/learnt/ffsm_"+SPL_NAME+"_"+"_".join(prodAnalyzed)+".txt"
process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
output, error = process.communicate()


for NEXT_ID in prodOrder:
	tmp=list(prodAnalyzed)
	tmp.append(NEXT_ID)
	bashCommand = "java -jar ./learnFFSM.jar -fm ./"+SPL_NAME+"/model.xml -fref ./"+SPL_NAME+"/learnt/ffsm_"+SPL_NAME+"_"+"_".join(prodAnalyzed)+".txt  -updt ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+NEXT_ID+".txt -out ./"+SPL_NAME+"/learnt/ffsm_"+SPL_NAME+"_"+"_".join(tmp)+".txt"
	print(bashCommand)
	process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
	output, error = process.communicate()	
	print(output)
	
	bashCommand = "java -jar ./learnFFSM.jar -fm ./"+SPL_NAME+"/model.xml -clean ./"+SPL_NAME+"/learnt/ffsm_"+SPL_NAME+"_"+"_".join(tmp)+".txt"
	process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
	output, error = process.communicate()
	prodAnalyzed=tmp

