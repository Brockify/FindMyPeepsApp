#! /usr/bin/python
import LogApi
import cgi
import shutil
import json

print "Content-type: text/html\r\n\r\n"

d = {}
arguments = cgi.FieldStorage()
username = arguments.getvalue('user')
password = arguments.getvalue('pass')   
email = arguments.getvalue('email')
currentpath = '/home1/skyrealm/public_html/img/default/basicprof.jpg'
newpathandname = '/home1/skyrealm/public_html/img/%s.jpg' % username 

if username == None or password == None or email == None:
    d['success'] = 0
    d['message'] = 'Field is empty'
    print json.dumps(d)
else:
    if len(username) <= 4 or len(username) >= 20:
        d['success'] = 0
        d['message'] = 'Username or password has an incorrect length(must be more than 4 characters and less than 20)'
        print json.dumps(d)
    else:
        if len(password) >= 20 or len(password) <= 4:
            d['success'] = 0
            d['message'] = 'Username or password has an incorrect length(must be more than 4 characters and less than 20)'
            print json.dumps(d)
        else:
            if LogApi.check_user(username):
                d['success'] = 0
                d['message'] = 'Username already exists'
                print json.dumps(d)
            else:
                if LogApi.validateEmail(email) == 1:
                    if LogApi.check_email(email):
                        d['success'] = 0
                        d['message'] = 'Email is already registered to another account'
                        print json.dumps(d)
                    else:
                    #Add password restrictions and add profanity filter to username.
                        password, salt = LogApi.hash_password(password)
                        LogApi.CreateUser(username, password, salt, email)
                        shutil.copyfile(currentpath, newpathandname)                 
                        d['success'] = 1
                        d['message'] = 'Account created!'
                        print json.dumps(d)
                else:
                    d['success'] = 0
                    d['message'] = 'Invalid Email'
                    print json.dumps(d)