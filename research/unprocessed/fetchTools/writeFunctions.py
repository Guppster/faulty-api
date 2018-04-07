# !/usr/bin/python

import sys,re
import utility
from os import EX_OK, EX_USAGE, EX_UNAVAILABLE, EX_IOERR

typeDefDict = {}

def parseFunctionDeclaration(line, input_file, output_file, files,\
							invokableEntityContainmentFile,\
							functionTypeFile, functionTypeDefFile):
	cdifId=line.split("M")[1]
	cdif_current_object = line
	name = ""
	uniqueName = ""
	parameters = ""
	declaredReturnType = ""
	declaredReturnClass = ""
	sourceFile = ""
	start = ""
	typeSourceFile = ""
	typeLineNr = ""
	
	for functionLine in input_file:
		cdif_current_object = cdif_current_object + functionLine
		functionLine = functionLine.strip()

		if functionLine.startswith(")"):
			break
		elif functionLine.startswith("(name \""):
			name=functionLine.split("\"")[1]
		elif functionLine.startswith("(uniqueName \""):
			uniqueName=functionLine.split("\"")[1]
			parameters=""
			uniqueNameBrokenUp=uniqueName.split("(")
			nrOfBrackets=len(uniqueNameBrokenUp)

			bracketIndex=1
			while ( bracketIndex < nrOfBrackets ):
				parameters += uniqueNameBrokenUp[bracketIndex]
				bracketIndex+=1

			parameters = parameters.rstrip(")")

		elif functionLine.startswith("(declaredReturnType \""):
			declaredReturnType=functionLine.split("\"")[1]
		elif functionLine.startswith("(declaredReturnClass \""):
			declaredReturnClass=functionLine.split("\"")[1]
		elif functionLine.startswith("(sourceAnchor "):
			sourceFile = functionLine.split("\"")[1]
			start = functionLine.split("\"")[2].split(" ")[2]
		elif functionLine.startswith("(typeSourceAnchor "):
			typeSourceFile = functionLine.split("\"")[1]
			typeLineNr = functionLine.split("\"")[2].split(" ")[2]
	
	functionInfo=cdifId + "\t\"" + uniqueName + "\"\n"
	output_file.write(functionInfo)
	typeId=""
	found = False;

	if ( sourceFile != "" ):
		if ( not (sourceFile in files) ):
			print sourceFile, files
			assert False, ("Unknown file "+sourceFile+" function "+cdifId)

		fileId=files[sourceFile]
		functionContainmentInfo=cdifId + "\t" + fileId + "\t" + start + "\n"
		invokableEntityContainmentFile.write(functionContainmentInfo)

	if ( declaredReturnClass != "" and typeSourceFile != ""):
		declaredReturnClassId = utility.getClassId(typeSourceFile,typeLineNr)
		#declaredReturnClassId = utility.getId(typeSourceFile,typeLineNr)
		declaredReturnTypeDefId = utility.getId(typeSourceFile,typeLineNr,declaredReturnType)

		if ( declaredReturnClassId == "" and declaredReturnTypeDefId == ""):
			#print ("Function Type not found: " + cdif_current_object)
			className = declaredReturnClass
			found = retrieveFunctionClass(className,cdifId,functionTypeDefFile,functionTypeFile,cdif_current_object,name)
			if not found:
				#print (cdif_current_object)	
				typeId = utility.getTypeIdHelper(className) #returns TypedefID
				if (typeId != ""):					
					functionTypeDefInfo=cdifId + "\t" + typeId + "\n"
					functionTypeDefFile.write(functionTypeDefInfo)
					if typeDefDict[typeId] != str(-1):
						functionTypeInfo=cdifId + "\t" + typeDefDict[typeId] + "\n"
						functionTypeFile.write(functionTypeInfo)
				#else:
					#print ("First Type not found: " + className)	

		elif declaredReturnClassId != declaredReturnTypeDefId:
			
		
			if declaredReturnTypeDefId != "":
				typeId = declaredReturnTypeDefId
				functionTypeDefInfo=cdifId + "\t" + typeId + "\n"
				functionTypeDefFile.write(functionTypeDefInfo)
				if typeDefDict[typeId] != str(-1):
					functionTypeInfo=cdifId + "\t" + typeDefDict[typeId] + "\n"
					functionTypeFile.write(functionTypeInfo)
			elif declaredReturnClassId != "":
				typeId = declaredReturnClassId
				functionTypeInfo=cdifId + "\t" + declaredReturnClassId + "\n"
				functionTypeFile.write(functionTypeInfo)
		else:
			if declaredReturnClassId != "":
				typeId = declaredReturnClassId
				functionTypeInfo=cdifId + "\t" + declaredReturnClassId + "\n"
				functionTypeFile.write(functionTypeInfo)
	else:
		className = declaredReturnType
		
		#className = className.split("<")[0]
		className = re.sub("\\((.*)\\)","" , className).strip()
		#className = className.split("(")[0]
		className = re.sub("\\<(.*)\\>","" , className).strip()
		className = className.replace("*","").replace("&","").replace("[","").replace("]","").strip()
		if " " in className:
			className = className.split(" ")[1]
		
		prefixBase = uniqueName
		#prefixBase = prefixBase.split("<")[0]
		prefixBase = re.sub("\\((.*)\\)","" , prefixBase).strip()
		#prefixBase = prefixBase.split("(")[0]
		prefixBase = re.sub("\\<(.*)\\>","" , prefixBase).strip()
		
		if "<" in className or "(" in className or "<" in prefixBase or "(" in prefixBase:
			print("trol")	

		prefix = '::'.join(prefixBase.split("::")[:(len(prefixBase.split("::"))-1)])
		while (prefix != ""):
			tempName = prefix + "::" + className
			found = retrieveFunctionClass(tempName,cdifId,functionTypeDefFile,functionTypeFile,cdif_current_object,name)
			if not found:
				typeId = utility.getTypeIdHelper(tempName)
				if (typeId != ""):
					functionTypeDefInfo=cdifId + "\t" + typeId + "\n"
					functionTypeDefFile.write(functionTypeDefInfo)
					if typeDefDict[typeId] != str(-1):
						functionTypeInfo=cdifId + "\t" + typeDefDict[typeId] + "\n"
						functionTypeFile.write(functionTypeInfo)
					break
				else:
					prefix = '::'.join(prefix.split("::")[:(len(prefix.split("::"))-1)])
			else:
				break
		if "::" not in prefixBase:
			found = retrieveFunctionClass(className,cdifId,functionTypeDefFile,functionTypeFile,cdif_current_object,name)
			if not found:
				typeId = utility.getTypeIdHelper(className)
				if (typeId != ""):
					functionTypeDefInfo=cdifId + "\t" + typeId + "\n"
					functionTypeDefFile.write(functionTypeDefInfo)
					if typeDefDict[typeId] != str(-1):
						functionTypeInfo=cdifId + "\t" + typeDefDict[typeId] + "\n"
						functionTypeFile.write(functionTypeInfo)
		#print ("Function ERROR " + cdif_current_object)
	#if not found:
	#	if (typeId != ""):
	#		print ("Function: " + name + "\n\t\t\t\t\t\t\t\t\tType: " + typeId)
	#	else:
	#		print ("Function: " + name + "\n\t\t\t\t\t\t\t\t\tName: " + declaredReturnType)
	#if not found and typeId == "":
	#	print ("Function: " + name + "\t" + cdifId + "\n\t\t\t\t\t\t\t\t\tType: " + declaredReturnType)
		

def retrieveFunctionClass(className,cdifId,functionTypeDefFile,functionTypeFile,cdif_current_object,functionName):
	
	classID=""
	classNameList = utility.getClassNameList(className)
	if (len(classNameList)>1):
		#print ("1: Two classes found"+ className)
		return False
	elif (len(classNameList)==1):
		classID = utility.getHelperId(classNameList[0])
		if (classID != ""):
			if classID in typeDefDict:
				functionTypeDefInfo=cdifId + "\t" + classID + "\n"
				functionTypeDefFile.write(functionTypeDefInfo)
				if typeDefDict[classID] != str(-1):
					functionTypeInfo=cdifId + "\t" + typeDefDict[classID] + "\n"
					functionTypeFile.write(functionTypeInfo)
			else:
				functionTypeInfo=cdifId + "\t" + classID + "\n"
				functionTypeFile.write(functionTypeInfo)
			
			#print ("Function: " + name + "\n\t\t\t\t\t\t\t\t\tClass: " + classID)
			#print ("2: Helper ID for " + className + " :" +classID)
			return True
		else:
			#print ("3: Helper ID for " + className)
			return False
	else:
		#print ("4: For: " + className + " : " + str(classNameList))
		return False



##
#(FunctionDefinition FM2047
#        (name "coltolong")
#        (declaredBy "coltolong(string)")
#        (uniqueName "coltolong(string)_1")
#        (sourceAnchor #[file "spreadsheet/util.cxx" start 69 end 69|]#)
#        (declSourceAnchor #[file "spreadsheet/util.h" start 90 end 90|]#)
#)
##
def parseFunctionDefinition(line, input_file, def_file, files, \
						invokableEntityContainmentFile):
	cdifId=line.split("M")[1].strip()

	name = ""
	declaredBy = ""
	uniqueName = ""
	sourceFile = ""
	lineNr = ""
	declSourceFile = ""
	declSourceLine = ""

	for functionLine in input_file:
		functionLine = functionLine.strip()

		if functionLine.startswith(")"):
			break
		elif functionLine.startswith("(name \""):
			name=functionLine.split("\"")[1]
		elif functionLine.startswith("(declaredBy \""):
			declaredBy=functionLine.split("\"")[1]
		elif functionLine.startswith("(uniqueName \""):
			uniqueName=functionLine.split("\"")[1]
		elif functionLine.startswith("(sourceAnchor "):
			sourceFile = functionLine.split("\"")[1]
			lineNr = functionLine.split("\"")[2].split(" ")[2]
		elif functionLine.startswith("(declSourceAnchor "):
			declSourceFile = functionLine.split("\"")[1]
			declSourceLine = functionLine.split("\"")[2].split(" ")[2]
	
	declId = utility.getEntityId(declSourceFile, declSourceLine)

	if declId != "":
		functionDefInfo=cdifId + "\t" + declId + "\n"
		def_file.write(functionDefInfo)

	if ( sourceFile != "" ):
		if ( not (sourceFile in files) ):
			assert False, ("Unknown file "+sourceFile+" function "+cdifId)

		fileId=files[sourceFile]
		invokableEntityContainmentInfo=cdifId + "\t" + fileId + "\t" + lineNr + "\n"
		invokableEntityContainmentFile.write(invokableEntityContainmentInfo)

def writeFunctions(cdif_file):
	output_file="functionsWithIDs.txt"
	
	files={}
	
	# build files dictionary
	files_file=open("filesWithIDs.txt", 'r')
	global typeDefDict
	typeDefDict = utility.initializeTypeDefDictionary()
	
	for line in files_file:
		lineSplittedInTabs=line.split("\t")
		fileId = lineSplittedInTabs[0]
		fileName = lineSplittedInTabs[1].strip().lstrip("\"").rstrip("\"")
	
		if ( not(fileName in files) ):
			files[fileName] = fileId
	
	files_file.close()
	
	input_file=open(cdif_file, 'r')
	output_file=open(output_file, 'w')
	invokableEntityContainmentFile=open("invokableEntityBelongsToFile.txt", 'a')
	functionTypeFile=open("functionHasClassAsReturnType.txt", 'w')
	functionTypeDefFile=open("functionHasTypeDefAsReturnType.txt", 'w')
	
	for line in input_file:
		line = line.strip()
	
		if line.startswith("(Function FM"):
			parseFunctionDeclaration(line, input_file, output_file, files,\
							invokableEntityContainmentFile,\
							functionTypeFile, functionTypeDefFile)
	
	input_file.close()
	invokableEntityContainmentFile.close()
	
	utility.reInitializeInvokableEntityContainmentDictionary()
	
	input_file=open(cdif_file, 'r')
	invokableEntityContainmentFile=open("invokableEntityBelongsToFile.txt", 'a')
	
	def_file_name="defsWithAssociation.txt"
	def_file=open(def_file_name, 'a')
	
	for line in input_file:
		line = line.strip()
	
		if line.startswith("(FunctionDefinition FM"):
			parseFunctionDefinition(line, input_file, def_file, files, \
						invokableEntityContainmentFile)
	
	invokableEntityContainmentFile.close()
	def_file.close()

	utility.reInitializeInvokableEntityContainmentDictionary()
	
	input_file.close()
	output_file.close()
	
	return EX_OK

## main
if __name__ == "__main__":
	if len(sys.argv) < 2:
	  print "Usage:",sys.argv[0],"cdif-input-file"
	  sys.exit(64)
	
	input_file=sys.argv[1]
	sys.exit(writeFunctions(input_file))
