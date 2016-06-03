#!/usr/bin/env python
# -*- coding: utf-8 -*-

""" importing packages """
import socket
import threading
import Adafruit_BBIO.GPIO as GPIO 
from time import sleep

""" Multi-threaded server class running on BBB-server,
	BBBK-server controlling the solonids"""
class Server(object):
    def __init__(self):
        self.host = ''
        self.port = 6432
        self.buff = 1024
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        #Three solonoids connected to pins P8_14, P9_15, P8_17
        self.solenoids = ["P8_14", "P9_15", "P8_17"]
        self.sock.bind((self.host, self.port))

    def listen(self):
    	#setting up the pins as an output pins
        GPIO.setup(self.solenoids[0],GPIO.OUT)
        GPIO.setup(self.solenoids[1],GPIO.OUT)
        GPIO.setup(self.solenoids[2],GPIO.OUT)
        
        self.sock.listen(5)
        while True:
            client, address = self.sock.accept()
            text = client.recv(self.buff)
            #getting the order that comming from BBBK-UI
            order = dict(itm.split("=") for itm in text.split(","))
            for (k,v),s in zip(order.items(), self.solenoids):
                t = threading.Thread(target = self.sendToSolenoids,args = (s,v))
                #starting each of the solenoids
                t.start()
                t.join()

    """ Running method for each solenoid """
    def sendToSolenoids(self, sol, amount):
        try:
            if amount:
                print "Solenoid " + sol + " is despensing: " + amount + "onces"
                GPIO.output(sol, GPIO.HIGH)
                sleep(float(amount))
                GPIO.output(sol, GPIO.LOW)
            else:
                raise error('Client disconnected')
        except:
            return False

if __name__ == "__main__":
    Server().listen()
