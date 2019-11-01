#!/bin/python

import random,subprocess,os,sys,re


prodOrder=[]
with open(sys.argv[1], 'r') as f: prodOrder = f.readlines()

# random.shuffle(prodOrder)
prodAnalyzed=list()
prodAnalyzed.append(prodOrder[0])
prodAnalyzed.append(prodOrder[1])
prodOrder.remove(prodOrder[1])
prodOrder.remove(prodOrder[0])


SPL_NAME=sys.argv[1].split("/")[0]	

out_directory=re.sub("\.txt$","/",sys.argv[1])
if not os.path.exists(out_directory):
	os.makedirs(out_directory)

_ktr=""
_ktr="-t 0.40"
_writer = open(out_directory+"/report.log","w")

MODEL_ID=1
bashCommand = "java -jar ./learnFFSM.jar "+_ktr+" -fm ./"+SPL_NAME+"/model.xml -mref "+prodAnalyzed[0]+" -updt "+prodAnalyzed[1]+" -out "+out_directory+"ffsm_"+str(MODEL_ID)+"_kiss.txt"
_writer.write(bashCommand)
_writer.write("\n")
process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
output, error = process.communicate()
_writer.write(output)
_writer.write("\n")

bashCommand = "java -jar ./learnFFSM.jar -fm ./"+SPL_NAME+"/model.xml -clean "+out_directory+"ffsm_"+str(MODEL_ID)+"_kiss.txt"
process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
output, error = process.communicate()
_writer.write(output)
_writer.write("\n")

for NEXT_ID in prodOrder:
	MODEL_ID+=1
	bashCommand = "java -jar ./learnFFSM.jar "+_ktr+" -fm ./"+SPL_NAME+"/model.xml -fref "+out_directory+"ffsm_"+str(MODEL_ID-1)+"_kiss.txt  -updt "+NEXT_ID+" -out "+out_directory+"ffsm_"+str(MODEL_ID)+"_kiss.txt"
	_writer.write(bashCommand)
	_writer.write("\n")
	process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
	output, error = process.communicate()	
	_writer.write(output)
	_writer.write("\n")
_writer.close()