#!/usr/bin/env python
# -*- coding: utf-8 -*-

#importing packages
import socket
import threading
import Adafruit_BBIO.GPIO as GPIO
from time import sleep


class Server():
    """
    Multi-threaded server class running on BBB-server,
	BBBK-server controlling the solonids
    """
    def __init__(self):
        self.host = ''
        self.port = 6432
        self.buff = 1024
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        #Three solonoids connected to pins P8_14, P9_15, P8_17
        self.solenoids = ["P8_14", "P9_15", "P8_17"]
        self.sock.bind((self.host, self.port))

    def listen(self):
        """
        A method for incomming orders
        """
    	#setting up the pins as an output pins
        GPIO.setup(self.solenoids[0], GPIO.OUT)
        GPIO.setup(self.solenoids[1], GPIO.OUT)
        GPIO.setup(self.solenoids[2], GPIO.OUT)

        self.sock.listen(5)
        while True:
            client, _ = self.sock.accept()
            text = client.recv(self.buff)
            #reading the order that comming from BBBK-UI
            order = dict(itm.split("=") for itm in text.split(","))
            for (k, v), s in zip(order.items(), self.solenoids):
                t = threading.Thread(target = self.send_to_solenoids,args = (s, v))
                #starting each solinod
                t.start()
                t.join()

    def send_to_solenoids(self, sol, amount):
        """ Running mathod for each solenoid """
        try:
            if amount:
                print("Using solenoid__" + sol + "to despense: " + amount + "onces")
                GPIO.output(sol, GPIO.HIGH)
                sleep(float(amount))
                GPIO.output(sol, GPIO.LOW)

            else:
                raise error('Client disconnected')
        # need to define a new exception
        except:
            return False


if __name__ == "__main__":
    Server().listen()
