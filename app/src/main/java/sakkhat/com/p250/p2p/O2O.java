
package sakkhat.com.p250.p2p;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Rafiul Islam on 18-Oct-18.
 */

class O2O {
    /**
     * O2O is a protected class with 4 protected inner static class those will provide a socket
     * communication environment between two devices.
     *
     * @see Server
     * @see Client
     * @see Receiver
     * @see Sender
     *
     * @param PORT socket port from where this socket connection will be established.
     *
     * @param MESSAGE_FILE_RECEIVED file received confirmation code for handler.
     * @param MESSAGE_FILE_SENT file sent confirmation code for handler
     * @param MESSAGE_SOCKET_CONNECTED socket connected confirmation code for handler
     * @param MESSAGE_IO_ERROR socket and I/O error notify code for handler
     *
     * @param PATH default directory to store the files.
     * */
    private static final int PORT = 31000;
    public static final int MESSAGE_FILE_KNOCK = -1919190;
    public static final int MESSAGE_FILE_RECEIVED = -1919191;
    public static final int MESSAGE_FILE_SENT = -1919192;
    public static final int MESSAGE_SOCKET_CONNECTED = -1919193;
    public static final int MESSAGE_IO_ERROR = -1919194;
    public static final int MESSAGE_PROGRESS = -1919195;

    public static final String PATH = Environment.getExternalStoragePublicDirectory(Environment
            .DIRECTORY_DOCUMENTS).getPath();

    static class Server implements Runnable{
        /**
         * Server class is a runnable implemented class to bind a ServerSocket with a defined
         * port and accept a single socket request. Server class will be run in background and
         * communicate with UI using handler. After accepted the socket request server will create
         * its own socket and transfer to the base activity using handler.
         *
         * @param TAG tag name
         * @param handler to established a communication between background thread and UI thread
         * */
        private static final String TAG = "o2o_server_thread";
        private Handler handler;

        public Server(Handler handler){
            this.handler = handler;
        }

        @Override
        public void run(){
            try{
                ServerSocket ss = new ServerSocket(PORT);
                Socket socket = ss.accept();
                Log.d(TAG, "connected: "+socket.getRemoteSocketAddress());
                // notify activity that socket is connected and send the socket to the activity
                handler.obtainMessage(MESSAGE_SOCKET_CONNECTED, socket).sendToTarget();
            } catch (IOException ex){
                Log.e(TAG, ex.toString());
            }
        }
    }

    static class Client implements Runnable{
        /**
         * Client class is a runnable implemented class for request a server-socket with server
         * host address and opened port. Client class will run in background. Client class will
         * communicate with its base activity using handler and report all log.
         * After socket connected with the server-side the connected socket will be transferred to
         * the base activity using handler.
         *
         * @param TAG tag name
         * @param host INetAddress of the server device
         * @param handler to established a communication between background thread and UI thread
         * */
        private static final String TAG = "o2o_client_thread";
        private InetAddress host;
        private Handler handler;


        public Client(InetAddress host, Handler handler){
            this.host = host;
            this.handler = handler;
        }
        @Override
        public void run(){
            try{
                Socket socket = new Socket(host,PORT);
                Log.d(TAG, "connected: "+socket.getRemoteSocketAddress());
                handler.obtainMessage(MESSAGE_SOCKET_CONNECTED, socket).sendToTarget();
            } catch (IOException ex){
                Log.e(TAG, ex.getMessage());
            }
        }
    }


    static class Receiver implements Runnable{
        /**
         * Receiver class a runnable implemented class and used for checking upcoming
         * message from remote socket. Receiver will check new upcoming message from remote
         * socket till the its own socket connected. Receiver will run in background.
         *
         * @param TAG tag name
         * @param socket device socket
         * @param handler to established a communication between background thread and UI thread
         * @param dis DataInputStream object for receive custom data types from socket
         * @param bis BufferedInputStream for receive the bytes from socket
         * */
        private static final String TAG = "o2o_data_io";
        private Socket socket;
        private Handler handler;

        private DataInputStream dis;
        private BufferedInputStream bis;

        public Receiver(Socket socket, Handler handler){
            this.socket = socket;
            this.handler = handler;
        }

        /**
         * Thread will live til the socket alive.
         *
         * <h3>Reading Flow</h3>
         * <ol>
         *     <li>File Name</li>
         *     <li>File Size</li>
         *     <li>File Data</li>
         * </ol>
         *
         * */
        @Override
        public void run(){
            try {

                // set Data I/O streams
                dis = new DataInputStream(socket.getInputStream());

                // set the buffered I/O streams
                bis = new BufferedInputStream(dis);

                // store the file name
                String fileName;
                // store the file size
                long fileSize;
                // manage how many bytes of file received
                long received;
                // how many bytes read per time
                int len;
                // file object
                File file;
                // set the default directory to store the file
                file = new File(PATH, "P2P");
                // if the path doesn't exist in the current device then create it
                if(!file.exists()){
                    file.mkdir();
                }

                // default 64kb byte data can read from stream
                byte[] kb_64 = new byte[1024*64];

                // continue the thread til the socket alive
                while (!socket.isClosed() || socket != null){
                    try {
                        // read the file name from stream
                        fileName = dis.readUTF();
                        // read the file size from stream
                        fileSize = dis.readLong();

                        // create a new file with this @param fileName in default directory
                        file = new File(file.getAbsolutePath()+"\\"+fileName);
                        // starting a new output stream on this file
                        FileOutputStream fos = new FileOutputStream(file);
                        // initialize the received byte size
                        received = 0;

                        handler.obtainMessage(MESSAGE_FILE_KNOCK, new String[] {
                                fileName, Long.toString(fileSize)
                        }).sendToTarget();

                        // continue to read the file byte data til the last one
                        while ((len = bis.read(kb_64)) > 0){
                            // write the bytes on file
                            fos.write(kb_64);
                            // update the received bytes size
                            received += len;
                            // when all bytes received for this file close the file stream
                            if(received >= fileSize){
                                fos.flush();
                                fos.close();
                                Log.d(TAG, "file write complete");
                                break;
                            }
                        }
                        Log.d(TAG, "file received");


                        //After received the file notify the base activity using handler.
                        handler.obtainMessage(MESSAGE_FILE_RECEIVED,file).sendToTarget();

                    } catch (IOException ex){
                        Log.e(TAG, ex.toString());
                        break;
                    }
                }
            } catch (IOException ex){
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    static class Sender implements Runnable{

        /**
         * Sender class is a runnable implemented class designed for send a file through
         * a socket in background.
         *
         * @param TAG tag name
         * @param socket connected device socket
         * @param handler to established a communication between background thread and UI thread
         * @param file file to be sent
         * */
        private static final String TAG = "o2o_sender";

        private Socket socket;
        private Handler handler;
        private File file;

        public Sender(Socket socket, Handler handler, File file){
            this.socket = socket;
            this.handler = handler;
            this.file = file;
        }

        @Override
        public void run(){
            try {

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

                // sent the file name
                dos.writeUTF(file.getName());
                // sent the file sizes
                dos.writeLong(file.length());
                // starting an input stream from this file
                FileInputStream fis = new FileInputStream(file);

                // manage how many bytes already sent
                long sent = 0;
                // how many bytes read per time
                int len;
                // 64kb byte data as maximum i/o size
                byte[] kb_64 = new byte[64*1024];

                // continue to read the file tile the last byte
                while((len = fis.read(kb_64)) > 0){
                    // write the read byte on buffered output stream
                    bos.write(kb_64);
                    // update the sent bytes value
                    sent += len;
                }
                // close the file input stream
                fis.close();
                // flush the buffered output stream
                bos.flush();

                Log.d(TAG, "file sent");

                handler.obtainMessage(MESSAGE_FILE_SENT).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
