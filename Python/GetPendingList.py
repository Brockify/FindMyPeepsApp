#! /usr/bin/python

import FindMyPeepsAPI
import cgi
import json

print "Content-Type: html/text\n\n"

arguments = cgi.FieldStorage()
username = arguments.getvalue("username")
Number = arguments.getvalue('Number')
if FindMyPeepsAPI.get_vnumber(Number, username):
    if username == None:
        print "Missing parameters"
    else:
            pendingList = []
            for person in FindMyPeepsAPI.pending_list(username):
                user = {}
                user["fromUser"] = person
                pendingList.append(user)

            print json.dumps(pendingList)
else:
    print "ERROR: ID"