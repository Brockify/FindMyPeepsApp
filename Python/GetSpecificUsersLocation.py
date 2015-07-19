#! /usr/bin/python

import FindMyPeepsAPI
import cgi
import json

print "Content-Type: html/text\n\n"

arguments = cgi.FieldStorage()
username = arguments.getvalue("username")

if username == None:
    print "Missing parameters"
else:
    user = {}
    user["Latitude"] = FindMyPeepsAPI.get_latitude(username)
    user["Longitude"] = FindMyPeepsAPI.get_longitude(username)
    user["Comment"] = FindMyPeepsAPI.get_comment(username)
    list = []
    list.append(user)
    print json.dumps(list)