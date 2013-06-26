#!/usr/bin/env python
# This file is part of AndroMine
# Copyright (C) 2013, Tegawende F. Bissyande <tegawende.bissyande@uni.lu>
# All rights reserved.
import re
import os
import sys
import codecs
import string
import MySQLdb
import argparse
import hashlib
from xml.dom import minidom

PATH_INSTALL = "./androguard/"
sys.path.append(PATH_INSTALL + "core/")
sys.path.append(PATH_INSTALL + "core/bytecodes")

import apk, androconf

def readManifest(apkfile):
	if (androconf.is_android(apkfile) != "APK"):
		print "Unknow file type"
		return ""
	a=apk.APK(apkfile)
	buff = a.xml[ "AndroidManifest.xml" ].toprettyxml()
	return buff
	#	return ""

#def getPermissions (document):
#	perms = document.getElementsByTagName("uses-permission")
#	ps = dict()
#	for perm in perms:
#		p = perm.getAttributeNode("android:name")
#		ps[string.replace(p.nodeValue, 'android.permission.', '')]=0
#	return ps

def getPermissions(apkfile):
	a=apk.APK(apkfile)
	perms = a.get_permissions()
	ps = dict()
	for perm in perms:
		ps[string.replace(perm, 'android.permission.', '')]=0
	return ps

def computeHash (manifestcontent):
	ha = hashlib.sha1(manifestcontent).hexdigest()
	return ha

