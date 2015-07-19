#! /usr/bin/python

import FindMyPeepsAPI
import cgi

print "Content-Type: text/html\n\n"

arguments = cgi.FieldStorage()
userLoggedIn = arguments.getvalue("userLoggedIn")
friend = arguments.getvalue("friend")

if userLoggedIn == None or friend == None:
    print "Missing parameters"
else:
    print FindMyPeepsAPI.delete_friend(userLoggedIn, friend)

