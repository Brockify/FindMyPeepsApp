#! /usr/bin/python

import FindMyPeepsAPI
import cgi
import json

print "Content-Type: text/html\r\n\r\n"
arguments = cgi.FieldStorage()
username = arguments.getvalue("username")
group = arguments.getvalue("group")
groupnumber = arguments.getvalue("groupnumber")
raidleader = arguments.getvalue("raidleader")

if username == None or group == None or groupnumber == None or raidleader == None:
    print "Missing parameters"
else:
    print json.dumps(FindMyPeepsAPI.list_group_members(username, group, groupnumber, raidleader))