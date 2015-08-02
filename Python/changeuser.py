#! /usr/bin/python
import LogApi
import cgi
import json
import os, sys

print "Content-type: text/html\r\n\r\n"

d = {}
arguments = cgi.FieldStorage()
newuser = arguments.getvalue('Newuser')
username = arguments.getvalue('username')
if newuser == None:
    d['success'] = 0
    d['message'] = 'Field is empty'
    print json.dumps(d)
else:
    if len(newuser) <= 4 or len(newuser) >= 20:
         d['success'] = 0
         d['message'] = 'Username has an incorrect length(must be more than 4 characters and less than 20)'
         print json.dumps(d)
    else:
        if LogApi.check_user(newuser):
            d['success'] = 0
            d['message'] = 'Username already exists'
            print json.dumps(d)
        else:
            LogApi.Change_User(username, newuser)
            username.lower()
            newuser = newuser.lower()
            currentbig = '/home1/skyrealm/public_html/img/%sorig.jpg' % username
            currentpath = '/home1/skyrealm/public_html/img/%s.jpg' % username 
            newpath = '/home1/skyrealm/public_html/img/%sorig.jpg' % newuser
            newpathandname = '/home1/skyrealm/public_html/img/%s.jpg' % newuser
            os.rename(currentbig,newpath)
            os.rename(currectpath,newpathandname)
            d['success'] = 1
            d['message'] = 'Username Changed'
            print json.dumps(d)