#! /usr/bin/python

import FindMyPeepsAPI
import cgi

print "Context-Type: html/text\n\n"

arguments = cgi.FieldStorage()
latitude = arguments.getvalue("Latitude")
longitude = arguments.getvalue("Longitude")
username = arguments.getvalue("Username")
address = arguments.getvalue("Address")
comments = arguments.getvalue("Comments")
time = arguments.getvalue("Time")
notification = time + ":" + username + " has updated his location"

if latitude == None or longitude == None or username == None or address == None:
    print "Missing Parameters"
else:
    FindMyPeepsAPI.update_location(latitude, longitude, notification, username, address, comments)
    print "Location Added!"