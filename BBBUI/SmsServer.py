# Twilio Python Server
# Jose Chavez @jchavez

import MySQLdb
import datetime
import time
import os
from contextlib import closing
from twilio.rest import TwilioRestClient
import socket

# =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
#                       VARIABLES
# =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

# Twilio credentials
twilio_account_sid = "<Account sid>"
twilio_auth_token = "<auth_token>"
# Twilio number
sTwilioNumber = "<phone number>"

iSID_Count = 0

# Status of the users; 0 - no action initated yet - can go to 1(asks 'text', 'scan' or 'main menu') or 2(asks 
#for drink number or main menu); 1 - register initiated - can go to 3(text), 4(scan), or 0(main menu); 2 - asked for drink # 
#or main menu - valid or goes to state 5, asking size, invalid loops back; 3 -  register by text -  valid QR code or main menu will 
#return to 0, invalid QR will loop back to 3; 4 - user wants to register by camera - 'ready' scans cup, 
#invalid will return to 4, valid QR code or 'main menu' will lead back to 0; 5 - waits for a drink size, 's', 'm', or 'l', invalid 
#loops back to state 5, valid dispenses drink and returns to main, main menu cancels and return to main

user_1_status = 0
user_2_status = 0
user_3_status = 0
curr_user_status = 0

user_1_order = ""
user_2_order = ""
user_3_order = ""
curr_user_order = ""

sLastCommand = "Startup sequence initiated at {0}.  No open requests, yet".format(time.strftime("%x %X"))
sAuthorized = ""
sSid = ""
sSMSSender = ""

# List of SMS IDs that have been recently scanned
currSids = list()

# Connect database
con = MySQLdb.connect('localhost', 'cocktail', 'cocktailpass', 'ReceivedMessages')
# Twilio client
TwilioClient = TwilioRestClient(twilio_account_sid, twilio_auth_token)

#TCP Params
HOST = "localhost"
PORT = 9876
REC_PORT = 9431
actionSuccess = 1

# Listening socket
sockListen = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
listen_addr = ('', REC_PORT)
sockListen.bind(listen_addr)
sockListen.listen(1)


# =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
#                       FUNCTIONS
# =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

# This function sends an SMS message, wrapped in some error handling
def SendSMS(sMsg):
  try:
    sms = TwilioClient.sms.messages.create(body="{0}".format(sMsg),to="{0}".format(sSMSSender),from_="{0}".format(sTwilioNumber))
  except:
    print "Error inside function SendSMS"
    pass
	
# This function sends an TCP message, wrapped in some error handling
def SendTCP(TCPMsg):
  try:
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((HOST, PORT))
    sock.sendall(TCPMsg)
    sock.close()
	
    connection, client_addr = sockListen.accept()
    data = connection.recv(1024)
	
    global actionSuccess
    #this mean it was successful, whatever the action was
    if ( data == "1,1\n" ): 
      print "{0} Valid QR code sent by {1}".format(time.strftime("%x %X"), sSMSSender)
      SendSMS("QR Code accepted. Registration complete.")
      global curr_user_status
      curr_user_status = 0	
	 
    #this mean it has failed, whatever the action was
    elif (data == "1,2\n"):
      print "{0} Invalid QR code sent by {1}".format(time.strftime("%x %X"), sSMSSender)
      SendSMS("QR Code not accepted. Registration failed. Please send another QR code or reply 'main menu'.")
	  
    elif (data == "2,1\n"):
      SendSMS("Order successfully placed. Dispensing drink now.")
    elif (data == "2,2\n"):
      SendSMS("Order failed. Cup is not registered.")
    elif (data == "2,3\n"):
      SendSMS("Order failed. Cup is registerd to another user.")

    sock.close()
    	  
  except:
    print "Error inside function SendTCP"
    pass

      
	
try:
  # Stores SMS IDs so that we do keep acting on the same texts over and over
  with closing(con.cursor()) as sid_cursor:
    rows = sid_cursor.execute("select sSid from Messages")   
    rows = sid_cursor.fetchall()
    for row in rows:
      for col in row:
        iSID_Count = iSID_Count + 1
        currSids.append(col)
        
  print "{0} Service loaded, {1} previous SMS messages".format(time.strftime("%x %X"),iSID_Count)

except:
  print "{0} Error! Quitting app!".format(time.strftime("%x %X"))
  if con: con.close() 
  exit(2)


# =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
#                       TWILIO SCAN LOOP
# =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

while (True):
 
  try:

    # Check messages from today
    messages = TwilioClient.messages.list(date_sent=datetime.datetime.utcnow())

    for p in messages:
      sSMSSender = p.from_

      # Only look at received messages
      if p.status == "received":
	  
        if p.sid not in currSids: # Is it a unique SMS SID ID from Twilio's list?
          # Insert this new SID ID into database and List, to avoid double processing
          currSids.append(p.sid)
          try:
            with closing(con.cursor()) as insert_sid_cursor:
              insert_sid_cursor = insert_sid_cursor.execute("insert into Messages(sSid) values('{0}')".format(p.sid))
              con.commit()
          except:
            print "Could not add SMS ID to db"
            pass
			
          if sSMSSender == "+19802416924":
            curr_user_status = user_1_status
          elif sSMSSender == "+18049263633":
            curr_user_status = user_2_status
          elif sSMSSender == "+19192709877":
            curr_user_status = user_3_status

          if curr_user_status == 0:
            if p.body.lower() == "register":
              print "{0} Request to register from {1}, replied".format(time.strftime("%x %X"), sSMSSender)
              SendSMS("What is your cup code? (or reply 'main menu')")
              curr_user_status = 3
			
            elif p.body.lower() == "drink menu":
              print "{0} Request for the menu from {1}, replied".format(time.strftime("%x %X"), sSMSSender)
              SendSMS("This is a list of drinks: 0-blue, 1-red, 2-yellow, 3-purple, 4-orange, 5-brown")
				
            elif p.body.lower() == "order":
              print "{0} Request to order from {1}, replied".format(time.strftime("%x %X"), sSMSSender)
              SendSMS("What drink would you like to order? (reply with a number 0-5; reply 'main menu' to cancel)")
              curr_user_status = 2
			  
            else:
              print "{0} Invalid message from {1}, replied".format(time.strftime("%x %X"), sSMSSender)
              SendSMS("Invalid command. Please reply with: 'register', 'drink menu', or 'order')")

          elif p.body.lower() == "main menu":
              print "{0} Request to return to main menu from {1}".format(time.strftime("%x %X"), sSMSSender)
              SendSMS("Canceling request and returning to main menu.")
              curr_user_status = 0	
			  
          elif curr_user_status == 2:
		  
            valid = True
            order = 0
			  
            try:
              order = int(p.body.lower())
            except ValueError:
              valid = False
			  
            if valid is True:
              
              if 0 <= order <= 5:
                curr_user_order = p.body.lower()
                curr_user_status = 4
                print "{0} Drink ordered by {1}, replied".format(time.strftime("%x %X"), sSMSSender)
                SendSMS("What size drink would you like? Reply ('s', 'm', 'l', or 'main menu')")
              else:
                print "{0} Invalid drink order from {1}".format(time.strftime("%x %X"), sSMSSender)
                SendSMS("Invalid drink order. (reply with a number 0-5; reply 'main menu' to cancel)")
            else:
              print "{0} Invalid drink order from {1}".format(time.strftime("%x %X"), sSMSSender)
              SendSMS("Invalid drink order. (reply with a number 0-5; reply 'main menu' to cancel)")
         
				
            if sSMSSender == "+19802416924":
              user_1_order = curr_user_order 
            elif sSMSSender == "+18049263633":
              user_2_order = curr_user_order
            elif sSMSSender == "+19192709877":
              user_3_order = curr_user_order
	
          elif curr_user_status == 3:
			
            message = "2, 1, " + sSMSSender + ", " + p.body + "\n"
            print message
            SendTCP(message)			
								
          elif curr_user_status == 4:
            sizePicked = False
			
            if p.body.lower() in ('s', 'm', 'l'):
              print "{0} {1} drink requested {2}, replied".format(time.strftime("%x %X"), p.body.upper(), sSMSSender)
              sizePicked = True
							
            else:
                print "{0} Invalid drink order from {1}".format(time.strftime("%x %X"), sSMSSender)
                SendSMS("Invalid drink size. (reply with 's', 'm', 'l'; reply 'main menu' to cancel)")
			
            if sizePicked is True:
              sizePicked = False
              curr_user_status = 0
              if sSMSSender == "+19802416924":
                SendTCP("2, 2, " + sSMSSender + ", " + user_1_order + ", " + p.body + "\n")
              elif sSMSSender == "+18049263633":
                SendTCP("2, 2, " + sSMSSender + ", " + user_2_order + ", " + p.body + "\n")
              elif sSMSSender == "+19192709877":
                SendTCP("2, 2, " + sSMSSender + ", " + user_3_order + ", " + p.body + "\n")
           		
          if sSMSSender == "+19802416924":
            user_1_status = curr_user_status
          elif sSMSSender == "+18049263633":
            user_2_status = curr_user_status
          elif sSMSSender == "+19192709877":
            user_3_status = curr_user_status
            
  except KeyboardInterrupt:  
    exit(4)
  
  except:
    print "Error! Quitting app!"
  
    exit(1)
