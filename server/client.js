"use strict"

var util = require('util')
var zmq = require('zeromq-port')

var sub = zmq.createSocket('sub')
sub.connect('tcp://127.0.0.1:12345')
sub.subscribe('AAPL')

util.log('Connected!')

sub.on('message', function(data){
	util.log('received data: ' + data.toString('utf8'))
})
