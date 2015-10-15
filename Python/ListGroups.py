#! /usr/bin/python

import FindMyPeepsAPI
import cgi
import json

print "Content-Type: text/html\r\n\r\n"
arguments = cgi.FieldStorage()
username = arguments.getvalue("username")
if username == None:
    print "Missing parameters"
else:
    print json.dumps(FindMyPeepsAPI.list_groups(username))