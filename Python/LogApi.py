#! /usr/bin/python
import MySQLdb
import base64
import uuid
import hashlib
import smtplib
import string
import random
import re

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

    # Create the body of the message (HTML version).
    #html = """\
	#<html>
  	#	<head></head>
  	#	<body>
    	#		<p>Hi!<br>
       	#		How are you?<br>
       	#		Here is the <a href="http://www.python.org">link</a> you wanted.
    	#		</p>
  	#	</body>
	#</html>
   # """

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
    sql = "delete from Users where Username=%s"
    CUR.execute(sql, username)
    return "Account Deleted"
    
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