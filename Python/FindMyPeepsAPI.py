#! /usr/bin/python
import MySQLdb
import urllib2

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

def get_email(username):
    sql = "select Email from Users where Username=%s"
    CUR.execute(sql, [username])
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