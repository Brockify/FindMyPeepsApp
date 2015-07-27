#! /usr/bin/python
import FindMyPeepsAPI
import cgi

print "Context-Type: text/html\n\n"

arguments = cgi.FieldStorage()
username = arguments.getvalue("Username")
friend = arguments.getvalue("Friend")
yesorno = int(arguments.getvalue("YesOrNo"))

if username == None or friend == None or yesorno == None:
    print "Missing parameters"
else:
    print FindMyPeepsAPI.accept_or_deny_friend_request(username, friend, yesorno)