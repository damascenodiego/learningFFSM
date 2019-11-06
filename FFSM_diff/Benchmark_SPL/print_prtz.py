import glob
import re

fsm_map = {}

for fsm_file in glob.glob("fsm/fsm*.txt"):
	f = open(fsm_file,"r")
	line = f.readline()
	line = line.split("\t")
	features = []
	for feat in line:
		feat = re.sub("\W+$","",feat)
		if feat[0]!='(':
			features.append(feat)
	features.sort()
	
	fsm_map["+".join(features)] = fsm_file

# print(fsm_map)

for folder in ["_all","_1wise","_2wise","_3wise","_4wise"]:
	prtz_f = open("products"+folder+".prtz","w")
	for config_file in glob.glob("./products"+folder+"/*.config"):
		f = open(config_file,"r")
		features = []
		for feat in f.readlines():
			feat = re.sub("\W+$","",feat)
			features.append(feat)
		features.sort()
		prtz_f.write(fsm_map["+".join(features)])
		prtz_f.write("\n")
	prtz_f.close()

