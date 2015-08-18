#! /usr/bin/python
import FindMyPeepsAPI
import cgi
import json
arguments = cgi.FieldStorage()
username = arguments.getvalue("username")

liststuff = []
for notification in FindMyPeepsAPI.get_notifications(FindMyPeepsAPI.friends_list(username)):
    result = {}
    result["notification"] = notification["notification"]
    result["username"] = notification["username"]
    liststuff.append(result)

print json.dumps(liststuff)