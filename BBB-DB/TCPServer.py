#!/usr/bin/env python
# -*- coding: utf-8 -*-

""" importing packages """
import json
import socket
import sys
import signal

hostname = '192.168.1.9'
portListen = 7521;
portSend = 9876;

# reading the recipes JSON file
json_file = open("recipes.json").read()
recipeData = json.loads(json_file)

# Find the listening socket
sockListen = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
listen_addr = ('', portListen)
print >>sys.stderr, 'listening on %s port %s' % listen_addr 
sockListen.bind(listen_addr)

# Listen for incoming connections
sockListen.listen(1)

# Loop for new connections
while True:
    connection, client_addr = sockListen.accept()
    print 'Accepting connection'
    client_data = connection.recv(1024).strip('\n')
    #client_data = client_data[:-1]
    print client_data
    #Process data through JSON file
    recipe = recipeData["recipes"][client_data]
    recipeString = '3'
    for key, value in recipe.iteritems():
        recipeString += (',')
        recipeString += key
        recipeString += (',')
        recipeString += `value`

    # Create sending socket
    sockSend = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    send_addr = (hostname, portSend)
    print >>sys.stderr, 'sending on %s port %s' % send_addr
    sockSend.connect(send_addr)

    # Send out info
    sockSend.send(recipeString)
    sockSend.close()

sockListen.close()

