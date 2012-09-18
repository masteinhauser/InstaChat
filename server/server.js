"use strict"

var util = require('util')
var zmq = require('zeromq-port')

// pub
var pub = zmq.createSocket('pub')

var stocks = ['AAPL', 'GOOG', 'YHOO', 'MSFT', 'INTC']

pub.bind('tcp://127.0.0.1:12345', function(err){
	if(err){
		util.err(err)
	}

	util.log('bound!')
	setInterval(function() {
		var symbol = stocks[Math.floor(Math.random()*stocks.length)]
		var value = Math.random()*1000
		pub.send(symbol+' '+value)
		util.log('Sent: '+symbol+' '+value)
	}, 1000)
})
