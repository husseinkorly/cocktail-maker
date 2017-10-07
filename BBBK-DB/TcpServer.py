#!/usr/bin/env python
# -*- coding: utf-8 -*-

import json
import socket
import sys


class DBServer():
    """
    DB server running in different BBBK
    """
    def __init__(self):
        self.hostname = '192.168.1.9'
        self.port_listen = 7521
        self.port_send = 9876

		# Grab the file first
        json_file = open("recipes.json").read()
        self.recipe_data = json.loads(json_file)

		# Find the listening socket
        self.sock_listen = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        listen_addr = ('', self.port_listen)
        print >>sys.stderr, 'listening on %s port %s' % listen_addr
        self.sock_listen.bind(listen_addr)
		# Listen for incoming connections
        self.sock_listen.listen(1)

    def listen(self):
        """
        Loop for new connections
        """
        while True:
            # accept new connection
            connection, _ = self.sock_listen.accept()
            print('Accepting connection')
            client_data = connection.recv(1024).strip('\n')

            print(client_data)
            #Process data through JSON file
            recipe = self.recipe_data["recipes"][client_data] # Find the single object
            recipe_string = '3'                          # Starting string
            for key, value in recipe.iteritems():       # Loop through each object
                recipe_string += (',')                   # Add comma
                recipe_string += key                     # Add ingredient name
                recipe_string += (',')                   # Add comma
                recipe_string += value                 # Add value of name

            # Create sending socket
            sock_send = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            send_addr = (self.hostname, self.port_send)
            print >>sys.stderr, 'sending on %s port %s' % send_addr
            # Connect to the already existing socket
            sock_send.connect(send_addr)

            # Send out info
            sock_send.send(recipe_string)      # Send the string to the other side
            sock_send.close()

        self.sock_listen.close()                   # Close the socket


if __name__ == "__main__":
    DBServer().listen()
