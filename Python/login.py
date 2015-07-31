#! /usr/bin/python
import LogApi
import cgi
import json

print "Content-type: text/html\r\n\r\n"

d = {}



arguments = cgi.FieldStorage()
username = arguments.getvalue('username')
password = arguments.getvalue('password')     
hashed_password = None
salt = None
if username == None or password == None:  
    d['success'] = 0
    d['message'] = 'Login or password is empty'
    print json.dumps(d)
else:
    if LogApi.check_user(username):
    	hashed_password = LogApi.get_password(username)
    	salt = LogApi.get_Salt(username)
        password = LogApi.hash_password(password, salt)
        if hashed_password == password:
            d['success'] = 1
            d['message'] = 'Logged in successfully'
            print json.dumps(d)
        else:
            d['success'] = 0
            d['message'] = 'Incorrect username or password'
            print json.dumps(d)
    else: 
        d['success'] = 0
        d['message'] = 'Incorrect username'
        print json.dumps(d)