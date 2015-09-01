#! /usr/bin/python
import FindMyPeepsAPI
import cgi
import json
arguments = cgi.FieldStorage()
print "Content-Type: html/text\n\n"
username = arguments.getvalue("username")
Number = arguments.getvalue('Number')
if FindMyPeepsAPI.get_vnumber(Number, username):
    testList = []
    testList = FindMyPeepsAPI.get_notifications(FindMyPeepsAPI.friends_list(username))
    testList.sort()
    liststuff = []
    for notification in testList:
        result = {}
        result["notification"] = notification["notification"]
        result["username"] = notification["username"]
        liststuff.append(result)
    print json.dumps(liststuff[::-1])
else:
    print "ERROR: ID"