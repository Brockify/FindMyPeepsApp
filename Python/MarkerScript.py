#! /usr/bin/python

import FindMyPeepsAPI
import cgi
import json
print "Content-type: html/text\n\n"
arguments = cgi.FieldStorage()
username = arguments.getvalue("username")
Number = arguments.getvalue('Number')
if FindMyPeepsAPI.get_vnumber(Number, username):
    if username == None:
        print "Missing parameter"
    else:
            print json.dumps(FindMyPeepsAPI.markers_on_map(username))
else:
    print "ERROR: ID"