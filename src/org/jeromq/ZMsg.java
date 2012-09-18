/*
    Copyright (c) 1991-2011 iMatix Corporation <www.imatix.com>
    Copyright other contributors as noted in the AUTHORS file.
                
    This file is part of 0MQ.

    0MQ is free software; you can redistribute it and/or modify it under
    the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.
            
    0MQ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.
        
    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.jeromq;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jeromq.ZMQ.Socket;

/**
 * The ZMsg class provides methods to send and receive multipart messages
 * across 0MQ sockets. This class provides a list-like container interface,
 * with methods to work with the overall container.  ZMsg messages are
 * composed of zero or more ZFrame objects.
 * 
 * <pre>
 * // Send a simple single-frame string message on a ZMQSocket "output" socket object
 * ZMsg.newStringMsg("Hello").send(output);
 * 
 * // Add several frames into one message
 * ZMsg msg = new ZMsg();
 * for (int i = 0;i< 10;i++) {
 *     msg.addString("Frame" + i);
 * }
 * msg.send(output);
 * 
 * // Receive message from ZMQSocket "input" socket object and iterate over frames
 * ZMsg receivedMessage = ZMsg.recvMsg(input);
 * for (ZFrame f : receivedMessage) {
 *     // Do something with frame f (of type ZFrame)
 * }
 * </pre>
 *
 * Based on <a href="http://github.com/zeromq/czmq/blob/master/src/zmsg.c">zmsg.c</a> in czmq
 *
 */

public class ZMsg implements Iterable<ZFrame>, Deque<ZFrame> {
    /**
     * Hold internal list of ZFrame objects
     */
    private ArrayDeque<ZFrame> frames;
    
    /**
     * Class Constructor
     */
    public ZMsg() {
        frames = new ArrayDeque<ZFrame>();
    }
    
    /**
     * Destructor.
     * Explicitly destroys all ZFrames contains in the ZMsg
     */
    public void destroy() {
        if (frames == null)
            return;
        for (ZFrame f : frames) {
            f.destroy();
        }
        frames.clear();
    }
    
    
    /**
     * Return total number of bytes contained in all ZFrames in this ZMsg
     * @return
     */
    public long contentSize() {
        long size = 0;
        for (ZFrame f : frames) {
            size += f.size();
        }
        return size;
    }

    /**
     * Add a String as a new ZFrame to the end of list
     * @param str
     *              String to add to list
     */
    public void addString(String str) {
        if (frames == null)
            frames = new ArrayDeque<ZFrame>();
        frames.add(new ZFrame(str));
    }
    
    /**
     * Creates copy of this ZMsg.
     * Also duplicates all frame content.
     * @return
     *          The duplicated ZMsg object, else null if this ZMsg contains an empty frame set
     */
    public ZMsg duplicate() {
        if (frames != null) {
            ZMsg msg = new ZMsg();
            for (ZFrame f : frames)
                msg.add(f.duplicate());
            return msg;
        } else
            return null;
    }
    
    /**
     * Push frame plus empty frame to front of message, before 1st frame.
     * Message takes ownership of frame, will destroy it when message is sent.
     * @param frame
     */
    public void wrap(ZFrame frame) {
        if (frame != null) {
            push(new ZFrame(""));
            push(frame);
        }
    }
    
    /**
     * Pop frame off front of message, caller now owns frame.
     * If next frame is empty, pops and destroys that empty frame
     * (e.g. useful when unwrapping ROUTER socket envelopes)
     * @return
     *          Unwrapped frame
     */
    public ZFrame unwrap() {
        if (size() == 0)
            return null;
        ZFrame f = pop();
        ZFrame empty = getFirst();
        if (empty.hasData() && empty.size() == 0) {
            empty = pop();
            empty.destroy();
        }
        return f;
    }

    /**
     * Send message to 0MQ socket.
     *
     * @param socket
     *              0MQ socket to send ZMsg on.
     */
    public void send(Socket socket) {
        send(socket, false);
    }

    /**
     * Send message to 0MQ socket, destroys contents after sending if destroy param is set to true.
     * If the message has no frames, sends nothing but still destroy()s the ZMsg object
     * @param socket
     *              0MQ socket to send ZMsg on.
     */
    public void send(Socket socket, boolean destroy) {
        if (socket == null)
            throw new IllegalArgumentException("socket is null");
        if (frames.size() == 0)
            return;
        Iterator<ZFrame> i = frames.iterator();
        while(i.hasNext()) {
            ZFrame f = i.next();
            f.sendAndKeep(socket, (i.hasNext()) ? ZMQ.SNDMORE : 0);
        }
        if (destroy) {
            destroy();
        }
    }
    
    
    
    /**
     * Receives message from socket, returns ZMsg object or null if the
     * recv was interrupted. Does a blocking recv, if you want not to block then use
     * the ZLoop class or ZMQ.Poller to check for socket input before receiving or recvMsg with flag ZMQ.DONTWAIT.
     * @param   socket
     * @return
     */
    public static ZMsg recvMsg(Socket socket) {
        return recvMsg(socket, 0);
    }

    /**
     * Receives message from socket, returns ZMsg object or null if the
     * recv was interrupted. Does a blocking recv, if you want not to block then use
     * the ZLoop class or ZMQ.Poller to check for socket input before receiving.
     * @param   socket
     * @param   flag see ZMQ constants
     * @return
     */
    public static ZMsg recvMsg(Socket socket, int flag) {
        if (socket == null)
            throw new IllegalArgumentException("socket is null");

        ZMsg msg = new ZMsg();
        
        while (true) {
            ZFrame f = ZFrame.recvFrame(socket, flag);
            if (f == null) {
                // If receive failed or was interrupted
                msg.destroy();
                break;
            }
            msg.add(f);
            if (!f.hasMore())
                break;
        }
        return msg;
    }

    /**
     * Save message to an open data output stream.
     * 
     * Data saved as:
     *      4 bytes: number of frames
     *  For every frame:
     *      4 bytes: byte size of frame data
     *      + n bytes: frame byte data
     * 
     * @param msg
     *          ZMsg to save
     * @param file
     *          DataOutputStream
     * @return
     *          True if saved OK, else false
     */
    public static boolean save(ZMsg msg, DataOutputStream file) {
        if (msg == null)
            return false;

        try {
            // Write number of frames
            file.writeInt(msg.size());
            if (msg.size() > 0 ) {
                for (ZFrame f : msg) {
                    // Write byte size of frame
                    file.writeInt(f.size());
                    // Write frame byte data
                    file.write(f.getData());
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Load / append a ZMsg from an open DataInputStream
     * 
     * @param file
     *          DataInputStream connected to file
     * @return
     *          ZMsg object
     */
    public static ZMsg load(DataInputStream file) {
        if (file == null)
            return null;
        ZMsg rcvMsg = new ZMsg();
        
        try {
            int msgSize = file.readInt();
            if (msgSize > 0) {
                int msgNbr = 0;
                while (++msgNbr <= msgSize) {
                    int frameSize = file.readInt();
                    byte[] data = new byte[frameSize];
                    file.read(data);
                    rcvMsg.add(new ZFrame(data));
                }
            }
            return rcvMsg;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Create a new ZMsg from one or more Strings
     *
     * @param strings
     *      Strings to add as frames.
     * @return
     *      ZMsg object
     */
    public static ZMsg newStringMsg(String... strings) {
        ZMsg msg = new ZMsg();
        for (String data : strings) {
            msg.addString(data);
        }
        return msg;
    }

    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZMsg zMsg = (ZMsg) o;

        if (frames == null || zMsg.frames == null) return false;

        //based on AbstractList
        Iterator<ZFrame> e1 = frames.iterator();
        Iterator<ZFrame> e2 = zMsg.frames.iterator();
        while (e1.hasNext() && e2.hasNext()) {
            ZFrame o1 = e1.next();
            ZFrame o2 = e2.next();
            if (!(o1 == null ? o2 == null : o1.equals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    
    public int hashCode() {
        if (frames == null || frames.size() == 0)
            return 0;

        int result = 1;
        for (ZFrame frame : frames)
            result = 31 * result + (frame == null ? 0 : frame.hashCode());

        return result;
    }

        /**
         * Dump the message in human readable format. This should only be used
         * for debugging and tracing, inefficient in handling large messages. 
         **/
        public void dump(Appendable out) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.printf("--------------------------------------\n");
                for (ZFrame frame : frames) {
                    pw.printf("[%03d] %s\n", frame.getData().length, 
                            frame.toString());
                }
                out.append(sw.getBuffer());
                sw.close();
            } catch (IOException e) {
                throw new RuntimeException( "Message dump exception " 
                        + super.toString(), e);
            } 
        }

    // ********* Convenience Deque methods for common data types *** //
        
        public void addFirst (String stringValue) {
            addFirst(new ZFrame(stringValue));
        }

        public void addFirst (byte[] data) {
            addFirst(new ZFrame(data));
        }
        
        public void addLast (String stringValue) {
            addLast(new ZFrame(stringValue));
        }

        public void addLast (byte[] data) {
            addLast(new ZFrame(data));
        }
          
    // ********* Convenience Queue methods for common data types *** //

    public void push(String str) {
        push(new ZFrame(str));
    }

    public void push(byte[] data) {
        push(new ZFrame(data));
    }

    public boolean add (String stringValue) {
        return add(new ZFrame(stringValue));
    }

    public boolean add (byte[] data) {
        return add(new ZFrame(data));
    }
        
    // ********* Implement Iterable Interface *************** //
    
    public Iterator<ZFrame> iterator() {
        // TODO Auto-generated method stub
        return frames.iterator();
    }

    // ********* Implement Deque Interface ****************** //
    
    public boolean addAll(Collection<? extends ZFrame> arg0) {
        return frames.addAll(arg0);
    }

    
    public void clear() {
        frames.clear();
        
    }

    
    public boolean containsAll(Collection<?> arg0) {
        return frames.containsAll(arg0);
    }

    
    public boolean isEmpty() {
        return frames.isEmpty();
    }

    
    public boolean removeAll(Collection<?> arg0) {
        return frames.removeAll(arg0);
    }

    
    public boolean retainAll(Collection<?> arg0) {
        return frames.retainAll(arg0);
    }

    
    public Object[] toArray() {
        return frames.toArray();
    }

    
    public <T> T[] toArray(T[] arg0) {
        return frames.toArray(arg0);
    }

    
    public boolean add(ZFrame e) {
        if (frames == null)
            frames = new ArrayDeque<ZFrame>();
        return frames.add(e);
    }

    
    public void addFirst(ZFrame e) {
        if (frames == null)
            frames = new ArrayDeque<ZFrame>();
        frames.addFirst(e);
    }

    
    public void addLast(ZFrame e) {
        if (frames == null)
            frames = new ArrayDeque<ZFrame>();
        frames.addLast(e);
        
    }

    
    public boolean contains(Object o) {
        return frames.contains(o);
    }

    
    public Iterator<ZFrame> descendingIterator() {
        return frames.descendingIterator();
    }

    
    public ZFrame element() {
        return frames.element();
    }

    
    public ZFrame getFirst() {
        try {
            return frames.getFirst();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    
    public ZFrame getLast() {
        try {
            return frames.getLast();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    
    public boolean offer(ZFrame e) {
        if (frames == null)
            frames = new ArrayDeque<ZFrame>();
        return frames.offer(e);
    }

    
    public boolean offerFirst(ZFrame e) {
        if (frames == null)
            frames = new ArrayDeque<ZFrame>();
        return frames.offerFirst(e);
    }

    
    public boolean offerLast(ZFrame e) {
        if (frames == null)
            frames = new ArrayDeque<ZFrame>();
        return frames.offerLast(e);
    }

    
    public ZFrame peek() {
        return frames.peek();
    }

    
    public ZFrame peekFirst() {
        try {
            return frames.peekFirst();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    
    public ZFrame peekLast() {
        try {
            return frames.peekLast();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    
    public ZFrame poll() {
        return frames.poll();
    }

    
    public ZFrame pollFirst() {
        return frames.pollFirst();
    }

    
    public ZFrame pollLast() {
        return frames.pollLast();
    }

    
    public ZFrame pop() {
        if (frames == null)
            frames = new ArrayDeque<ZFrame>();
        try {
            return frames.pop();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Pop a ZFrame and return the toString() representation of it.
     *
     * @return toString version of pop'ed frame, or null if no frame exists.
     */
    public String popString() {
        ZFrame frame = pop();
        if(frame == null)
            return null;

        return frame.toString();
    }

    
    public void push(ZFrame e) {
        if (frames == null)
            frames = new ArrayDeque<ZFrame>();
        frames.push(e);
    }

    
    public ZFrame remove() {
        return frames.remove();
    }

    
    public boolean remove(Object o) {
        return frames.remove(o);
    }

    
    public ZFrame removeFirst() {
        try {
            return frames.removeFirst();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    
    public boolean removeFirstOccurrence(Object o) {
        return frames.removeFirstOccurrence(o);
    }

    
    public ZFrame removeLast() {
        try {
            return frames.removeLast();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    
    public boolean removeLastOccurrence(Object o) {
        return frames.removeLastOccurrence(o);
    }

    
    public int size() {
        return frames.size();
    }
}
