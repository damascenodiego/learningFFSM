#!/bin/python

import random,subprocess,os

# prodOrder=["{:01d}".format(x) for x in range(1,8)]
prodOrder=["{:01d}".format(x) for x in (1,4,5,7,2,3,6,8)]

SPL_NAME="ws"

directory="./"+SPL_NAME+"/learnt_pairs/"
if not os.path.exists(directory):
	os.makedirs(directory)


for PROD_I in prodOrder:
	for PROD_J in prodOrder:
		# if(PROD_J==PROD_I): continue
		bashCommand = "java -jar ./learnFFSM.jar -fm ./"+SPL_NAME+"/model.xml -mref ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+PROD_I+".txt -updt ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+PROD_J+".txt -out ./"+SPL_NAME+"/learnt_pairs/ffsm_"+SPL_NAME+"_"+PROD_I+"_"+PROD_J+".txt"
		print(bashCommand)
		process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
		output, error = process.communicate()	
		print(output)



