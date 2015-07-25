#! /usr/bin/python
import MySQLdb

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
    result = CUR.fetchone()[0]
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
    profanity = ["fuck", "bitch", "ass", "cunt", "shit", "twat", "dick", "douche", "vagina", "piss", "cock", "penis","nigga", "niggar", "nigger", "gay", "fag", "faggot", "bastard"]
    for badword in profanity:
        if word == badword:
            return "profanity!"

def update_location(latitude, longitude, username, address, comments):
    sql = "update Users set latitude = %s  , longitude = %s, address = %s, comments = %s  where username =  %s"
    CUR.execute(sql, (latitude, longitude, address, comments, username))
    return "location updated"