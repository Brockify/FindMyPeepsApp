#! /usr/bin/python

import FindMyPeepsAPI
import cgi
import json

print "Content-Type: html/text\n\n"

arguments = cgi.FieldStorage()
username = arguments.getvalue("Username")
Number = arguments.getvalue('Number')
if FindMyPeepsAPI.get_vnumber(Number, username):
    if username == None:
        print "Missing parameters"
    else:
        user = {}
        user["latitude"] = FindMyPeepsAPI.get_latitude(username)
        user["longitude"] = FindMyPeepsAPI.get_longitude(username)
        user["comments"] = FindMyPeepsAPI.get_comment(username)
        list = []
        list.append(user)
        print json.dumps(list)
else:
    print "ERROR: ID"