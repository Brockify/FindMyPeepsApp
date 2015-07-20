#! /usr/bin/python

import FindMyPeepsAPI
import cgi
import json

print "Content-Type: html/text\n\n"

arguments = cgi.FieldStorage()
username = arguments["username"]

if username == None:
    print "Missing parameters"
else:
    pendingList = []
    for person in FindMyPeepsAPI.pending_list(username):
        user = {}
        user["fromUser"] = username
        pendingList.append(user)

    print json.dumps(pendingList)