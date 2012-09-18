package org.jeromq;

import org.jeromq.ZMQ.Context;
import org.jeromq.ZMQ.Socket;

public class ZMQQueue implements Runnable {

    private final ZMQ.Socket inSocket;
    private final ZMQ.Socket outSocket;

    /**
     * Class constructor.
     * 
     * @param context
     *            a 0MQ context previously created.
     * @param inSocket
     *            input socket
     * @param outSocket
     *            output socket
     */
    public ZMQQueue(Context context, Socket inSocket, Socket outSocket) {
        this.inSocket = inSocket;
        this.outSocket = outSocket;
    }

    public void run() {
        zmq.ZMQ.zmq_device(ZMQ.QUEUE, inSocket.base(), outSocket.base());
        if (!zmq.ZError.is(zmq.ZError.ETERM))
            throw new ZMQException(zmq.ZError.errno());
    }

}
