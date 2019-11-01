#!/bin/python

import random,subprocess,os


prodOrder=["{:05d}".format(x) for x in range(1,256+1)]

SPL_NAME="aerouc5"

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
		bashCommand = "java -cp ./learnFFSM.jar uk.le.ac.prioritize.CalculateDissimilarity ./"+SPL_NAME+"/products/products_all/"+PROD_I+"_kiss.txt ./"+SPL_NAME+"/products/products_all/"+PROD_J+"_kiss.txt -fm ./"+SPL_NAME+"/model.xml"

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
		bashCommand = "java -cp ./learnFFSM.jar uk.le.ac.compare.CompareLanguages ./"+SPL_NAME+"/products/products_all/"+PROD_I+"_kiss.txt ./"+SPL_NAME+"/products/products_all/"+PROD_J+"_kiss.txt -fm ./"+SPL_NAME+"/model.xml "+_ktr

		_writer.write("#")
		_writer.write(bashCommand)
		_writer.write("\n")
		process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
		output, error = process.communicate()	
		_writer.write(output)
		# _writer.write("\n")
	_writer.flush()
_writer.close()



