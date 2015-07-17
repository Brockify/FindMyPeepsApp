import MySQLdb

db = MySQLdb.connect("173.254.28.39", "skyrealm","AndrewBrock@2013","skyrealm_FindMyPeeps")
CUR = db.cursor()

def get_password(username):
    sql = "select Password from Users where Username=%s"
    CUR.execute(sql, username)
    result = CUR.fetchone()[0]
    if result == None:
        return "User does not exist"
    else:
        return result[0]

def get_email(username):
    sql = "select Email from Users where Username=%s"
    CUR.execute(sql, username)
    result = CUR.fetchone()
    if result == None:
        return "User does not exist"
    else:
        return result[0]
def get_longitude(username):
    sql = "select Longitude from Users where Username=%s"
    CUR.execute(sql, username)
    result = CUR.fetchone()
    if result == None:
        return "User did not update location"
    else:
        return result[0]

def get_latitude(username):
    sql = "select Latitude from Users where Username=%s"
    CUR.execute(sql, username)
    result = CUR.fetchone()
    if result == None:
        return "User did not update location"
    else:
        return result[0]

def get_address(username):
    sql = "select Address from Users where Username=%s"
    CUR.execute(sql, username)
    result = CUR.fetchone()
    if result == None:
        return "User did not update email"
    else:
        return result[0]

def friends_list(username):
    sql = "select all friend from accepted_req where userLoggedIn=%s"
    CUR.execute(sql, username)
    result=[]
    query = CUR.fetchall()
    if query == None:
        return "User has no friends"
    else:
        for friend in query:
            result.append(friend[0])

    return result

def markers_on_map(username):
    friendsList = friends_list(username)

    for friend in friendsList:
        print "Username" + " : " + friend
        print "Latitude" + " : " + get_latitude(friend)
        print "Longitude" + " : " + get_longitude(friend)

