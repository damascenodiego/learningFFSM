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


for PROD_I in prodOrder:
	for PROD_J in prodOrder:
		if(PROD_J==PROD_I): continue
		bashCommand = "java -jar ./learnFFSM.jar -fm ./"+SPL_NAME+"/model.xml -mref ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+PROD_I+".txt -updt ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+PROD_J+".txt -out ./"+SPL_NAME+"/learnt/ffsm_"+SPL_NAME+"_"+PROD_I+"_"+PROD_J+".txt"
		print(bashCommand)
		process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
		output, error = process.communicate()	
		print(output)



