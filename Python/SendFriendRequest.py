#! /usr/bin/python

import FindMyPeepsAPI
import cgi

print "Content-Type: text/html\r\n\r\n"

arguments = cgi.FieldStorage()
userLoggedIn = arguments.getvalue("userLoggedIn")
friend = arguments.getvalue("friend")
Number = arguments.getvalue("Number")
if FindMyPeepsAPI.get_vnumber(Number, userLoggedIn):
    if userLoggedIn == None or friend == None:
        print "Missing parameters"
    else:
        print FindMyPeepsAPI.delete_friend(userLoggedIn, friend)
else:
    print "Error: Try again."