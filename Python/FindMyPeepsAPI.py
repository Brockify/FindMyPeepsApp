#! /usr/bin/python
import MySQLdb
import urllib2
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

db = MySQLdb.connect("173.254.28.39", "skyrealm","AndrewBrock@2013","skyrealm_FindMyPeeps")
CUR = db.cursor()

def check_user(username):
    sql = "select Username from Users where Username=%s"
    CUR.execute(sql, username)
    result = CUR.fetchone()
    if result == None:
        return False
    else:
        return True

def crypt(veriable):
    veriable = str(veriable)
    veriable = base64.b64encode(veriable)
    return veriable

def dcrypt(veriable):
    veriable = str(veriable)
    veriable = base64.b64decode(veriable)
    return veriable

def get_vnumber(number, username):
    username = username.lower()
    number = base64.b64decode(number)
    number = int(number)
    sql = "select ID from Users where Username=%s"
    CUR.execute(sql, [username])
    ID = CUR.fetchone()[0]
    if number == ID:
        return True
    else:
        return False

def get_verinum(username):
    sql = "select verified from Users where Username=%s"
    CUR.execute(sql, [username])
    ID = CUR.fetchone()[0]
    if ID == 1:
        return True
    else:
        return False

def get_user_notification(username):
    sql = "select notification from notifications where username=%s"
    CUR.execute(sql, [username])
    result = CUR.fetchall()
    return result

def get_verify(username, hashed):
    sql = "select Password from Users where Username=%s"
    CUR.execute(sql, [username])
    result = CUR.fetchone()[0]
    if result == hashed:
        sql = "select ID from Users where Username=%s"
        CUR.execute(sql, [username])
        ID = CUR.fetchone()
        if ID == None:
            return "User does not exist"
        else:
            numb = ID[0]
            numb = str(numb)
            numb = base64.b64encode(numb)
            return numb
    else:
        return "Error Occured"

def vryfy(username, hash):
    orig = get_password(username)
    if orig == hash:
        return True
    else:
        return False
def get_password(username):
    sql = "select Password from Users where Username=%s"
    CUR.execute(sql, [username])
    result = CUR.fetchone()
    if result == None:
        return "User does not exist"
    else:
        return result[0]

def get_last_updated(username):
    sql = "select LastUpdated from Users where Username=%s"
    CUR.execute(sql, [username])
    result = CUR.fetchone()
    if result == None:
        return "User doesn't exist"
    else:
        return result[0]

def get_comment(username):
    sql = "select Comments from Users where Username=%s"
    CUR.execute(sql, [username])
    result = CUR.fetchone()
    if result == None or result == "":
        return "User has not updated comment"
    else:
        return result[0]

def get_bio(username):
    sql = "select Bio from Users where Username=%s"
    CUR.execute(sql, [username])
    result = CUR.fetchone()
    if result == None or result == "":
        return "No Bio Exists"
    else:
        return result[0]

def update_bio(username, message):
    username = username.lower()
    sql = "update Users set Bio = %s where username =  %s"
    CUR.execute(sql, (message, username))
    return "bio updated"

def get_email(username):
    sql = "select Email from Users where Username=%s OR email=%s"
    CUR.execute(sql, (username, username))
    result = CUR.fetchone()
    if result == None or result == "":
        return "User does not exist"
    else:
        return result[0]

def get_longitude(username):
    sql = "select Longitude from Users where Username=%s"
    CUR.execute(sql, [username])
    result = CUR.fetchone()
    if result == None or result == "":
        return "User did not update location"
    else:
        return result[0]

def get_latitude(username):
    sql = "select Latitude from Users where Username=%s"
    CUR.execute(sql, [username])
    result = CUR.fetchone()
    if result == None or result == "":
        return "User did not update location"
    else:
        return result[0]

def get_address(username):
    sql = "select Address from Users where Username=%s"
    CUR.execute(sql, [username])
    result = CUR.fetchone()
    if result == None or result == "":
        return "User did not update email"
    else:
        return result[0]

def friends_list(username):
    sql = "select all friend from accepted_req where userLoggedIn=%s"
    CUR.execute(sql, [username])
    result=[]
    query = CUR.fetchall()
    if query == None or query == []:
        return "User has no friends"
    else:
        for friend in query:
            result.append(friend[0])

    return result

def markers_on_map(username):
    friendsList = friends_list(username)
    fullFriendsList = []
    for friend in friendsList:
        UserDictionary123 = {}
        UserDictionary123["Username"] = friend
        UserDictionary123["Comment"] = str(get_comment(friend))
        UserDictionary123["Latitude"] = get_latitude(friend)
        UserDictionary123["Longitude"] = get_longitude(friend)
        UserDictionary123["LastUpdated"] = get_last_updated(friend)
        fullFriendsList.append(UserDictionary123)

    return fullFriendsList

def check_if_two_users_are_friends(fromUser, toUser):
    sql = "select userLoggedIn from accepted_req where userLoggedIn=%s and friend=%s";
    CUR.execute(sql, (fromUser, toUser))
    result = CUR.fetchone()
    if result == None:
        return False
    else:
        return True

def check_if_two_users_are_pending(fromUser, toUser):
    sql = "select fromUser from pending_req where fromUser=%s and toUser=%s"
    CUR.execute(sql, (fromUser, toUser))
    result = CUR.fetchone()
    if result == None:
        return False
    else:
        return True

def send_friend_request(fromUser, toUser):
    sql = "insert into pending_req(fromUser, toUser) values (%s, %s)"
    CUR.execute(sql, (fromUser, toUser))
    return True

def delete_friend(userLoggedIn, friend):
    sql = "delete from accepted_req where userLoggedIn=%s and friend=%s"
    CUR.execute(sql, (userLoggedIn, friend))
    sql = "delete from accepted_req where userLoggedIn=%s and friend=%s"
    CUR.execute(sql, (friend, userLoggedIn))
    return "Friend deleted"

def pending_list(username):
    sql = "select all fromUser from pending_req where toUser=%s"
    CUR.execute(sql, username)
    result = CUR.fetchall()
    pendingList = []
    for person in result:
        pendingList.append(person[0])

    return pendingList

def profanity_filter(word):
    data = urllib2.urlopen("http://www.skyrealmstudio.com/profanity.txt").read().split("\n")
    trueFalse = False
    for badword in data:
        if badword in word:
            trueFalse = True

    return trueFalse

def update_location(latitude, longitude, notification, username, address, comments, lastUpdated):
    sql = "update Users set latitude = %s  , longitude = %s, address = %s, comments = %s, lastupdated = %s  where username =  %s"
    CUR.execute(sql, (latitude, longitude, address, comments, lastUpdated, username))
    sql = "insert into notifications(username, notification) values (%s, %s)"
    CUR.execute(sql, (username, notification))
    return "location updated"

def accept_or_deny_friend_request(username, friend, yesorno):
    if yesorno == 1:
        sql = "insert into accepted_req (friend, userLoggedIn, YesOrNo) values (%s, %s, %s)"
        CUR.execute(sql, (username, friend, yesorno))
        sql = "insert into accepted_req (friend, userLoggedIn, YesOrNo) values (%s, %s, %s)"
        CUR.execute(sql, (friend, username, yesorno))
        sql = "delete from pending_req where fromUser=%s and toUser=%s"
        CUR.execute(sql, (friend, username))
        sql = "delete from pending_req where fromUser=%s AND toUser=%s"
        CUR.execute(sql, (username, friend))
        return "Friend request accepted or denyed!"
    elif yesorno == 0:
        sql = "delete from pending_req where fromUser=%s and toUser=%s"
        CUR.execute(sql, (friend, username))
        sql = "delete from pending_req where fromUser=%s AND toUser=%s"
        CUR.execute(sql, (username, friend))
    else:
        return "Failed"

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

def login_hash_password(password, username):
    sql = "select Salt from Users where Username=%s"
    CUR.execute(sql, [username])
    salt = CUR.fetchone()[0]
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

def get_notifications(friendslist):
    result = []
    for friend in friendslist:
        notifications = get_user_notification(friend)
        for notification in notifications:
            if notification != None:
                for i in notification:
                    userDict = {}
                    userDict["username"] = friend
                    userDict["notification"] = i
                    result.append(userDict)
    result[::-1]
    totalResult = []
    if len(result) <= 50:
        for i in range(len(result)):
            totalResult.append(result[i])
    else:
        for i in range(50):
            totalResult.append(result[i])
    return totalResult
   
def profile_notification(username, notification):
    sql = "insert into notifications(username, notification) values (%s, %s)"
    CUR.execute(sql, (username, notification))   
    return True
    
def bio_notification(username, notification):
    sql = "insert into notifications(username, notification) values (%s, %s)"
    CUR.execute(sql, (username, notification))
    return True
    
def ever_generator(size=20, chars=string.ascii_uppercase + string.digits):
    return ''.join(random.choice(chars) for _ in range(size))

def store_ever(username, ever):
    sql = "update Users set vnum=%s where Username=%s"
    CUR.execute(sql, (ever, username))
    return True
    
def update_ever(username, ever):   
    sql = "select vnum from Users where Username=%s"
    CUR.execute(sql, [username])
    result = CUR.fetchone()[0]
    if ever == result:
        return "True"
    else:
        return "False"
                
def update_ever_final(username, ever, very):
    sql = "update Users set vnum='null', verified=%s where Username=%s"
    CUR.execute(sql, (very, username))
    return True

print get_vnumber(get_verify("Brockify", login_hash_password("Brockify", "Brock114039")), "Brockify")