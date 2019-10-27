#!/bin/python

import random,subprocess,os


prodOrder=["{:01d}".format(x) for x in range(1,7)]

SPL_NAME="agm"

directory="./"+SPL_NAME+"/emse/"
if not os.path.exists(directory):
	os.makedirs(directory)


_writer = open(directory+"/pair_dissimilarity_"+SPL_NAME+".txt","w")
for idx_PROD_I in range(len(prodOrder)-1):
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

