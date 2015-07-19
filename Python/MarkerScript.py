#! /usr/bin/python
import FindMyPeepsAPI
import cgi

print "Content-type: text/html\r\n\r\n"

arguments = cgi.FieldStorage()
username = arguments.keys()

if username == []:
    print "Missing parameter"
else:
    for i in username:
        print json.dumps(FindMyPeepsAPI.markers_on_map(arguments[i].value))


