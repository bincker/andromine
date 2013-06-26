#!/usr/bin/env python

# This file is part of Andromine
# Copyright (C) 2013, Tegawende F. Bissyande <tegawende.bissyande@uni.lu>
# All rights reserved.
import os
import sys
import string
import MySQLdb
import argparse

import manifestlib, dexlib

from xml.dom import minidom

from androguard.core import androconf
from androguard.core.bytecodes import apk
from androguard.core.bytecodes import dvm
from androguard.core.analysis import analysis

from manifestlib import readManifest
from manifestlib import getPermissions
from manifestlib import computeHash

from dexlib import extract_method_calls

from optparse import OptionParser

option_0 = { 'name' : ('-i', '--input'), 'help' : 'file : use this filename (APK)', 'nargs' : 1 }

options = [option_0]

#print "Anything works for me"
db=MySQLdb.connect('localhost', 'tegawende', 'tegawende', 'tegawende')
cursor=db.cursor()

def insertPermission(db, db_cursor, apk, hashvalue, permission):
    cm="""INSERT INTO apksPermissions(apkHash, apkName, permission) VALUES ('""" + hashvalue + """','""" + apk + """','""" + permission + """')"""
    db_cursor.execute(cm)
    db.commit()

if __name__ == "__main__" :
    parser = OptionParser()
    for option in options :
        param = option['name']
        del option['name']
        parser.add_option(*param, **option)

    options, arguments = parser.parse_args()
    if options.input == None:
        exit(1)

    apkfile = options.input
#	cursor.execute("""CREATE TABLE IF NOT EXISTS apkspermissions(apkName TEXT, manifestHash TEXT, permission TEXT)""")
#	cursor.execute("""CREATE TABLE IF NOT EXISTS apksmethodcalls(apkName TEXT, manifestHash TEXT, method TEXT)""")
    if not os.path.isfile (apkfile):
        print "'"+apkfile+"': No such File or Directory"
        exit(1)
    app = apk.APK(apkfile, zipmodule=2)
    if not app.is_valid_APK():
        print "'"+apkfile+"': INVALID APK file"
        exit(1)
    #Reset table
    #cursor.execute("""DELETE FROM apkspermissions where apkName like '%'""")
    buff = readManifest(apkfile)
    hashval = computeHash(buff)
    print hashval
    #dom = minidom.parseString(buff)
    permissions=getPermissions(apkfile)
    for k in permissions.keys():
        #insertPermission(db, cursor, os.path.basename(apkfile), hashval, k)
        insertPermission(db, cursor, apkfile, hashval, k)
    print hashval
