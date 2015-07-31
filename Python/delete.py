#! /usr/bin/python

import LogApi
import cgi

print "Content-type: text/html\r\n\r\n"

arguments = cgi.FieldStorage()
username = arguments.getvalue('username')
vuser = arguments.getvalue('Verify')     

if username == None or vuser == None:
    print "Verification is empty"
else:
    if LogApi.check_user(username):
        if LogApi.compare_user_verify(username, vuser):
            print LogApi.DeleteUser(username)
        else:
            print "Username Incorrect!"
    else: 
        print "Username Incorrect!"
