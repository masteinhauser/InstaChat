"use strict"

var util = require('util')
var zmq = require('zeromq-port')

// pub
var pub = zmq.createSocket('pub')

pub.bind('tcp://127.0.0.1:12345', function(err){
	if(err){
		util.err(err)
	}

	util.log('bound!')
	//setInterval(stocks, 1000)
   setInterval(InstantMessage, 1000)

   function stocks(){
      var stocks = ['AAPL', 'GOOG', 'YHOO', 'MSFT', 'INTC']
	   var symbol = stocks[Math.floor(Math.random()*stocks.length)]
		var value = Math.random()*1000
		pub.send(symbol+' '+value)
		util.log('Sent: '+symbol+' '+value)
   }

   function InstantMessage(){
      var stocks = ['AAPL', 'GOOG', 'YHOO', 'MSFT', 'INTC']
      var symbol = stocks[Math.floor(Math.random()*stocks.length)]
      var value = Math.random()*1000

      var msg = {user: symbol, message: value}
      var json = JSON.stringify(msg)

      pub.send(symbol+' '+json)
      util.log(json)
   }
})
