#! /usr/bin/python

import FindMyPeepsAPI
import cgi
import json

print "Content-type: text/html\n\n"

arguments = cgi.FieldStorage()
username = arguments.getvalue("username")

if username == None:
    print "Missing parameter"
else:
    print json.dumps(FindMyPeepsAPI.markers_on_map(username))