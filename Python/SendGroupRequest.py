#! /usr/bin/python

import FindMyPeepsAPI
import cgi


print "Content-Type: text/html\r\n\r\n"
arguments = cgi.FieldStorage()
username = arguments.getvalue("username")
group_name = arguments.getvalue("group_name")
group_number = arguments.getvalue("group_number")
otheruser = arguments.getvalue("otheruser")

if username == None or group_name == None or group_number == None or otheruser == None:
    print "Missing parameters"
else:
    print FindMyPeepsAPI.send_group_request(username, group_number, group_name, otheruser)
