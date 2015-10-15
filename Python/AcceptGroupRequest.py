#! /usr/bin/python

import FindMyPeepsAPI
import cgi


print "Content-Type: text/html\r\n\r\n"
arguments = cgi.FieldStorage()
username = arguments.getvalue("username")
group = arguments.getvalue("group")
groupnumber = arguments.getvalue("groupnumber")
otheruser = arguments.getvalue("otheruser")

if username == None or group == None or groupnumber == None or otheruser == None:
    print "Missing parameters"
else:
    print FindMyPeepsAPI.accept_group_request(username, otheruser, group, groupnumber)