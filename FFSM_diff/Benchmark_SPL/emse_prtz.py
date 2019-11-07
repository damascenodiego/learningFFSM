#!/bin/python

import random,subprocess,os,sys,re,glob


prodOrder=[]
with open(sys.argv[1], 'r') as f: prodOrder = f.readlines()

current_directory=re.sub("[a-z_0-9]+\.prtz$","/",sys.argv[1])
out_directory=re.sub("\.prtz$","/",sys.argv[1])
if not os.path.exists(out_directory):
	os.makedirs(out_directory)

_ktr=""
# _ktr="-t 0.40 -k 0.6 -r ."
_writer = open(out_directory+"/report.log","w")

MODEL_ID=1
bashCommand = "java -jar ../learnFFSM.jar "+_ktr+" -fm "+current_directory+"/model.xml -mref "+prodOrder[0].replace("\n","")+" -updt "+prodOrder[1].replace("\n","")+" -out "+out_directory+"ffsm_"+str(MODEL_ID)+"_kiss.txt"
_writer.write(bashCommand)
_writer.write("\n")
process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
output, error = process.communicate()
_writer.write(output)
_writer.write("\n")
_writer.flush()

# remove two first models already analyzed
prodOrder.remove(prodOrder[1])
prodOrder.remove(prodOrder[0])

for NEXT_MODEL in prodOrder:
	NEXT_MODEL = NEXT_MODEL.replace("\n","")
	MODEL_ID+=1
	bashCommand = "java -jar ../learnFFSM.jar "+_ktr+" -fm "+current_directory+"/model.xml -fref "+out_directory+"ffsm_"+str(MODEL_ID-1)+"_kiss.txt  -updt "+NEXT_MODEL+" -out "+out_directory+"ffsm_"+str(MODEL_ID)+"_kiss.txt"
	_writer.write(bashCommand)
	_writer.write("\n")
	process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
	output, error = process.communicate()	
	_writer.write(output)
	_writer.write("\n")
	_writer.flush()
_writer.close()

	
# _ktr="-t 999 -r 0"
_writer = open(out_directory+"/report_fmeasure_l.log","w")
MAX_ID = MODEL_ID
with open(current_directory+"/products_all.prtz", 'r') as f:
	for fsm_file in f.readlines():
		for AN_ID in range(1,MAX_ID+1):
			fsm_file = fsm_file.replace("\n","")
			# bashCommand = "java -cp ./learnFFSM.jar uk.le.ac.compare.CompareStructure "+SPL_NAME+"/ffsms/ffsm_"+SPL_NAME+".txt "+out_directory+"ffsm_"+str(AN_ID)+"_kiss.txt "+_ktr+" -fm ./"+SPL_NAME+"/model.xml"
			bashCommand = "java -cp ../learnFFSM.jar uk.le.ac.compare.CompareStructure "+out_directory+"ffsm_"+str(AN_ID)+"_kiss.txt "+fsm_file+" "+_ktr+" -fm "+current_directory+"model.xml -both"
			process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
			output, error = process.communicate()
			_writer.write(bashCommand)
			_writer.write("\n")
			_writer.write(output)
			_writer.write("\n")
_writer.close()
