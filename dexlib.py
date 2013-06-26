#!/usr/bin/env python

# This file is part of AndroMine.
# Copyright (C) 2013, Tegawende F. Bissyande <tegawende.bissyande@uni.lu>
# All rights reserved.

import sys, os
import string
from optparse import OptionParser

from androguard.core import androconf
from androguard.core.bytecodes import apk
from androguard.core.bytecodes import dvm
from androguard.core.analysis import analysis
from androguard.decompiler.dad import decompile

def extract_method_calls(apk):
	vm = dvm.DalvikVMFormat(apk.get_dex())
	vmx = analysis.uVMAnalysis(vm)
	vmu = analysis.VMAnalysis(vm)
	apis=dict()
	for i in vmx.get_methods():
		i.create_tags()
		cla=i.method.get_class_name()
		met=i.method.get_name()
		tags = i.tags
		cla = string.replace(cla[1:len(cla)-2], "/", ".")
		k=cla+"."+met
		apis[k]=tags
		ist = i.method.get_instructions()
		print k
		for inst in ist:
			print "\t", inst.get_name(), inst.get_output()	
	return dict()
	#return apis

def main(apkfile) :
	if (androconf.is_android(apkfile) == "APK") :
		try :
			a = apk.APK(apkfile, zipmodule=2)
			if a.is_valid_APK():
				apis=extract_method_calls( a )
				ks=apis.keys()
				print "Number of API calls:", len(ks)
			else :
				print "INVALID"
		except Exception, e :
			print "ERROR", e

if __name__ == "__main__" :
    main(sys.argv[1])
