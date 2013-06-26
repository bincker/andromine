#!/usr/bin/env python

# Copyright (C) 2013, Tegawende F. Bissyande <tegawende.bissyande@uni.lu>
# All rights reserved.
#

import sys
from optparse import OptionParser
from xml.dom import minidom
import codecs

PATH_INSTALL = "./androguard/"
sys.path.append(PATH_INSTALL + "core/")
sys.path.append(PATH_INSTALL + "core/bytecodes")

import apk, androconf

def readManifest(apkfile):
   # ret_type = androconf.is_android(apkfile)
	if (androconf.is_android(apkfile) != "APK"):
		print "Unknow file type"
		return ""
	a=apk.APK(apkfile)
	buff = a.xml[ "AndroidManifest.xml" ].toprettyxml()
	return buff
    #if ret_type != "APK" :
	#	print "Unknown file type"
	#	return ""

def getPermissions (document):

if __name__ == "__main__" :
    #parser = OptionParser()
	buff = readManifest(sys.argv[1])
	dom = minidom.parseString(buff)
	print buff
