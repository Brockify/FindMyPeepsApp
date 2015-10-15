#! /usr/bin/python

import FindMyPeepsAPI
import cgi


print "Content-Type: text/html\r\n\r\n"

arguments = cgi.FieldStorage()
username = arguments.getvalue("username")
group = arguments.getvalue("group")
if username == None or group == None:
    print "Missing parameters"
else:
    print FindMyPeepsAPI.create_grop(username, group)
