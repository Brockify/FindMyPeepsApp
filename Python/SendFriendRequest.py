#! /usr/bin/python

import FindMyPeepsAPI
import cgi

print "Content-type: text/html\r\n\r\n"

arguments = cgi.FieldStorage()
fromUser = arguments.getvalue('fromUser')
toUser = arguments.getvalue('toUser')   

if fromUser == None or toUser == None:
    print "Missing parameters"
else:
    if FindMyPeepsAPI.check_user(toUser):
        if FindMyPeepsAPI.check_if_two_users_are_pending(fromUser, toUser):
            print "Request already sent to this user"
        elif FindMyPeepsAPI.check_if_two_users_are_friends(fromUser, toUser):
            print "Already friend"
        else:
            FindMyPeepsAPI.send_friend_request(fromUser, toUser)
            print "Friend added"
    else:
        print "User doesn't exist"
