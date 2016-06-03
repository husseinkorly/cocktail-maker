#!/usr/bin/env python
# -*- coding: utf-8 -*-

""" importing packages """
import json
import socket
import sys
import signal

""" TCP server class for BBB-DB """
class tcpServer(object):
	def __init__(self):
		hostName = 192.168.1.9
		portListen = 7521
		