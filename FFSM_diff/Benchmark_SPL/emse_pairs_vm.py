#!/bin/python

import random,subprocess,os
import datetime

SPL_NAME="vm"
prodOrder=["{:01d}".format(x) for x in range(1,20+1)]

directory="./"+SPL_NAME+"/emse/"
if not os.path.exists(directory):
	os.makedirs(directory)
if not os.path.exists(directory+"learnt/"):
	os.makedirs(directory+"learnt/")


_ktr=""
_ktr="-t 0.40"
_writer = open(directory+"/pair_merging_"+SPL_NAME+".txt","w")
for idx_PROD_I in range(len(prodOrder)):
	for idx_PROD_J in range(idx_PROD_I,len(prodOrder)):
		PROD_I = prodOrder[idx_PROD_I]
		PROD_J = prodOrder[idx_PROD_J] 
		print("Running for "+PROD_I+"_"+PROD_J)
		# if(PROD_J==PROD_I): continue
		bashCommand = "java -jar ./learnFFSM.jar "+_ktr+" -fm ./"+SPL_NAME+"/model.xml -mref ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+PROD_I+".txt -updt ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+PROD_J+".txt -out "+directory+"/learnt/ffsm_"+SPL_NAME+"_"+PROD_I+"_"+PROD_J+".txt"
		
		_writer.write("#")
		_writer.write(bashCommand)
		_writer.write("\n")
		a = datetime.datetime.now()
		process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
		output, error = process.communicate()	
		b = datetime.datetime.now()
		delta = b - a
		_writer.write(output)
		_writer.write("duration in ms:"+SPL_NAME+"|"+PROD_I+"|"+PROD_J+"|"+str(int(delta.total_seconds() * 1000)))
		_writer.write("\n")
	_writer.flush()
_writer.close()
