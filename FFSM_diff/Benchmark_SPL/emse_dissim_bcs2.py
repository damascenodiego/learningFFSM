 -fsm#!/bin/python

import random,subprocess,os

# prodOrder=["{:01d}".format(x) for x in range(1,8)]
prodOrder=["{:01d}".format(x) for x in (1,2,3,4,5,6)]

SPL_NAME="bcs2"

directory="./"+SPL_NAME+"/emse/"
if not os.path.exists(directory):
	os.makedirs(directory)


_writer = open(directory+"/pair_dissimilarity_"+SPL_NAME+".txt","w")
for idx_PROD_I in range(len(prodOrder)):
	for idx_PROD_J in range(idx_PROD_I,len(prodOrder)):
		PROD_I = prodOrder[idx_PROD_I]
		PROD_J = prodOrder[idx_PROD_J] 
		print("Running for "+PROD_I+"_"+PROD_J)
		# if(PROD_J==PROD_I): continue
		bashCommand = "java -cp ./learnFFSM.jar uk.le.ac.prioritize.CalculateDissimilarity ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+PROD_I+".txt ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+PROD_J+".txt -fm ./"+SPL_NAME+"/model.xml"

		_writer.write("#")
		_writer.write(bashCommand)
		_writer.write("\n")
		process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
		output, error = process.communicate()	
		_writer.write(output)
		# _writer.write("\n")
	_writer.flush()
_writer.close()

_ktr="-t 0.40 -fsm"
_writer = open(directory+"/pair_comparelanguages_"+SPL_NAME+".txt","w")
for idx_PROD_I in range(len(prodOrder)):
	for idx_PROD_J in range(idx_PROD_I,len(prodOrder)):
		PROD_I = prodOrder[idx_PROD_I]
		PROD_J = prodOrder[idx_PROD_J] 
		print("Running for "+PROD_I+"_"+PROD_J)
		# if(PROD_J==PROD_I): continue
		bashCommand = "java -cp ./learnFFSM.jar uk.le.ac.compare.CompareLanguages ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+PROD_I+".txt ./"+SPL_NAME+"/fsm/fsm_"+SPL_NAME+"_"+PROD_J+".txt -fm ./"+SPL_NAME+"/model.xml "+_ktr

		_writer.write("#")
		_writer.write(bashCommand)
		_writer.write("\n")
		process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
		output, error = process.communicate()	
		_writer.write(output)
		# _writer.write("\n")
	_writer.flush()
_writer.close()



