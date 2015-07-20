#! /usr/bin/python

import FindMyPeepsAPI
import cgi
import json

print "Content-Type: html/text\n\n"

arguments = cgi.FieldStorage()
username = arguments.getvalue('username')

if username == None:
    print "Missing parameter"
else:
    FriendsList = []
    for friend in FindMyPeepsAPI.friends_list(username):
        user = {}
        user["friend"] = friend
        FriendsList.append(user)

    print json.dumps(FriendsList)
