#! /usr/bin/python
import LogApi
import cgi
import json

print "Content-type: text/html\r\n\r\n"

d = {}
arguments = cgi.FieldStorage()
ueforgot= arguments.getvalue('useremail')
subject = 'Password Reset'
if ueforgot == None:
    d['success'] = 0
    d['message'] = 'Field is empty'
    print json.dumps(d)
else:
    if LogApi.check_ue(ueforgot): 
        emailadd = LogApi.get_email(ueforgot)
        newpass = LogApi.pass_generator()
        message = 'Your new temporary password is:%s \n To reset: \n Go to profile and change password' % newpass
        newpass, salt = LogApi.hash_password(newpass)
        LogApi.New_Pass(ueforgot, newpass, salt)
        LogApi.send_email(emailadd, subject, message)
        d['success'] = 1
        d['message'] = 'Your new password and instuctions have been sent to your e-mail!'
        print json.dumps(d)
    else:
        d['success'] = 0
        d['message'] = 'Username or email not found in our system'
        print json.dumps(d)