#! /usr/bin/python
import MySQLdb
import base64
import uuid
import hashlib
import smtplib
import string
import random
import re
import os
import os.path
import shutil

db = MySQLdb.connect("localhost", "skyrealm","AndrewBrock@2013","skyrealm_FindMyPeeps")
CUR = db.cursor()

def send_email(to, subject, text):
    from email.mime.multipart import MIMEMultipart
    from email.mime.text import MIMEText

    sky = "findmypeeps@skyrealmstudio.com"
    

    # Create header
    msg = MIMEMultipart('alternative')
    msg['Subject'] = subject
    msg['From'] = sky
    msg['To'] = to  
    text += " \n \n - DO NOT REPLY -"
    # Record the MIME types of both parts - text/plain and text/html.
    part1 = MIMEText(text, 'plain')
    #part2 = MIMEText(html, 'html')

    # Attach parts into message.
    msg.attach(part1)
    #msg.attach(part2)

    # Send the message via local SMTP server.
    s = smtplib.SMTP('localhost')
    s.sendmail(sky, to, msg.as_string())
    s.quit()

def check_user(username):
    sql = "select Username from Users where Username=%s"
    CUR.execute(sql, username)
    result = CUR.fetchone()
    if result == None:
        return False
    else:
        return True

def check_email(emailer):
    sql = "select email from Users where email=%s"
    CUR.execute(sql, emailer)
    result = CUR.fetchone()
    if result == None:
        return False
    else:
        return True

def check_ue(username):
    sql = "select Username from Users where Username=%s or email=%s"
    CUR.execute(sql, (username, username))
    result = CUR.fetchone()
    if result == None:
        return False
    else:
        return True        

def compare_user_verify(username, verify):
    if username.lower() == verify.lower():
        return True
    else:
        return False

def get_password(username):
    sql = "select Password from Users where Username=%s"
    CUR.execute(sql, username)
    result = CUR.fetchone()[0]
    if result == None:
        return "User does not exist"
    else:
        return result
        
def get_Salt(username):
    sql = "select Salt from Users where Username=%s"
    CUR.execute(sql, username)
    result = CUR.fetchone()[0]
    if result == None:
        return "User does not exist"
    else:
        return result
        
def get_email(username):
    sql = "select Email from Users where Username=%s OR email=%s"
    CUR.execute(sql, (username, username))
    result = CUR.fetchone()
    if result == None or result == "":
        return "User does not exist"
    else:
        return result[0]
 
def CreateUser(username, password, salt, email ):
    tempuser = username
    username = username.lower()
    sql = "insert into Users(Username, tempusername, Password, salt, email) values (%s, %s, %s, %s, %s)"
    CUR.execute(sql, (username, tempuser, password, salt, email))
    return True

def DeleteUser(username): 
    os.remove('/home1/skyrealm/public_html/img/%sorig.jpg' % username)
    username = username.lower()
    os.remove('/home1/skyrealm/public_html/img/%s.jpg' % username)
    sql = "delete from Users where Username=%s"
    CUR.execute(sql, username)
    sql1 = "DELETE FROM accepted_req WHERE friend=%s"
    CUR.execute(sql, username)
    sql = "delete from pending_req  where fromUser =%s"
    CUR.execute(sql, username)
    sql = "delete from accepted_req where userLoggedIn =%s"
    CUR.execute(sql, username)
    sql = "delete from pending_req where toUser =%s"
    CUR.execute(sql, username)
    return "Account Deleted"


def Change_User(username, newuser): 
    username = username.lower()
    sql = "update Users set Username=%s where Username=%s"
    CUR.execute(sql, (newuser.lower(), username))
    sql = "update Users set tempusername=%s where tempusername=%s"
    CUR.execute(sql, (newuser, username))
    sql = "update pending_req set fromUser=%s where fromUser=%s"
    CUR.execute(sql, (newuser, username))
    sql = "update pending_req set toUser=%s where toUser=%s"
    CUR.execute(sql, (newuser, username))
    sql = "update accepted_req set friend=%s where friend=%s"
    CUR.execute(sql, (newuser, username))
    sql = "update accepted_req set userLoggedIn=%s where userLoggedIn=%s"
    CUR.execute(sql, (newuser, username))
    return "Username Changed"
    
def hash_password(password, salt=None):
    if salt is None:
        salt = uuid.uuid4().hex
        hashed_password = hashlib.sha512(password + salt).hexdigest()
        return (hashed_password, salt)
    else:
        hashed_password = hashlib.sha512(password + salt).hexdigest()
        return (hashed_password)

def pass_generator(size=10, chars=string.ascii_uppercase + string.digits):
    return ''.join(random.choice(chars) for _ in range(size))

def New_Pass(username, password, salt):
    sql = "update Users set Password= %s, salt= %s where Username= %s or email= %s"
    CUR.execute(sql, (password, salt, username, username))
    return True
    
def validateEmail(email):

	if len(email) > 7:
		if re.match("^.+\\@(\\[?)[a-zA-Z0-9\\-\\.]+\\.([a-zA-Z]{2,3}|[0-9]{1,3})(\\]?)$", email) != None:
			return 1
	return 0