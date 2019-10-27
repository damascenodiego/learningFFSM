#!/bin/python

import random,subprocess,os


SPL_NAME="agm"
prodOrder=["{:01d}".format(x) for x in range(1,6+1)]

directory="./"+SPL_NAME+"/emse/"
if not os.path.exists(directory):
	os.makedirs(directory)


_writer = open(directory+"/pair_merging_"+SPL_NAME+".txt","w")
for idx_PROD_I in range(len(prodOrder)-1):
	for idx_PROD_J in range(idx_PROD_I,len(prodOrder)):
		PROD_I = prodOrder[idx_PROD_I]
		PROD_J = prodOrder[idx_PROD_J] 
		print("Running for "+PROD_I+"_"+PROD_J)
		# if(PROD_J==PROD_I): continue
		bashCommand = "java -jar ./learnFFSM.jar -fm ./"+SPL_NAME+"/model.xml -mref ./"+SPL_NAME+"/products/products_all/"+PROD_I+"_kiss.txt -updt ./"+SPL_NAME+"/products/products_all/"+PROD_J+"_kiss.txt -out "+directory+"/ffsm_"+SPL_NAME+"_"+PROD_I+"_"+PROD_J+".txt"
		
		_writer.write("#")
		_writer.write(bashCommand)
		_writer.write("\n")
		process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
		output, error = process.communicate()	
		_writer.write(output)
		# _writer.write("\n")
	_writer.flush()
_writer.close()
