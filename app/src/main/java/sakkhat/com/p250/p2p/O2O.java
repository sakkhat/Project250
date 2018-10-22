package sakkhat.com.p250.p2p;

import android.net.Uri;
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

/**
 * Created by Rafiul Islam on 18-Oct-18.
 */

class O2O {
    /**
     * O2O is a protected class with 3 protected inner static class those will provide a socket
     * communication environment between two devices.
     *
     * @see Server
     * @see Client
     * @see DataIO
     *
     * @param PORT socket port from where this socket connection will be established.
     * @param MESSAGE_READ_REQUEST request id for handler.
     * @param PATH default directory to store the files.
     * */
    private static final int PORT = 9191;
    public static final int MESSAGE_FILE_RECEIVED = -1919191;

    public static final String PATH = Environment.getExternalStoragePublicDirectory(Environment
            .DIRECTORY_DOCUMENTS).getPath();

    static class Server extends Thread{
        /**
         * Server class is also a thread itself that will used to create a ServerSocket with O2O.PORT.
         *
         * Server initialize with a handler object that already instanced in base activity and
         * a dataIO reference which will be initialized in server thread with this handler to
         * make sure that the dataIO thread for the server can communication with the base
         * activity UI.
         *
         * @param TAG name of the server thread
         * @param dataIO reference a copy DataIO from base activity
         * @param handler reference a copy of handler from base activity
         * */
        private static final String TAG = "o2o_server_thread";
        private DataIO dataIO;


        public Server(DataIO dataIO){
            this.dataIO = dataIO;

            this.setName(TAG);
            this.start();
        }


        @Override
        public void run(){
            try{
                ServerSocket ss = new ServerSocket(PORT);
                Socket socket = ss.accept();
                Log.d(TAG, "connected with: "+socket.getRemoteSocketAddress());
                dataIO.setSocket(socket);

            } catch (IOException ex){
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    static class Client extends Thread{
        /**
         * Client class is also a thread itself that will used to create a Socket with a
         * host address and O2O.PORT
         *
         * Client initialize with the server host address, handler object that already instanced in
         * base activity and a dataIO reference which will be initialized in server thread with this
         * handler to make sure that the dataIO thread for the server can communication with the base
         * activity UI.
         *
         * @param TAG name of the server thread
         * @param host INetAddress of the server device
         * @param dataIO reference a copy DataIO from base activity
         * @param handler reference a copy of handler from base activity
         * */
        private static final String TAG = "o2o_client_thread";
        private InetAddress host;
        private DataIO dataIO;

        public Client(InetAddress host, DataIO dataIO){
            this.host = host;
            this.dataIO = dataIO;

            this.setName(TAG);
            this.start();
        }
        @Override
        public void run(){
            try{
                Socket socket = new Socket(host,PORT);
                Log.d(TAG, "connected: "+socket.getRemoteSocketAddress());
                dataIO.setSocket(socket);
            } catch (IOException ex){
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    static class DataIO extends Thread{
        /**
         * DataIO class is a thread itself used to send and receive data between two sockets in
         * background.
         * Socket reference is for I/O buffer data and handler to make a request to base activity
         * for pass the streamed data.
         *
         * @param TAG thread name
         * @param socket reference
         * @param handler handler reference
         * @param bos write byte data on bufferd output stream
         * @param bis read byte data from  buffered input stream
         * @param dis read particular data type from input stream
         * @param dos write particular data type on output stream
         * */
        private static final String TAG = "o2o_data_io";
        private Socket socket;
        private Handler handler;

        private DataInputStream dis;
        private DataOutputStream dos;

        private BufferedOutputStream bos;
        private BufferedInputStream bis;

        public DataIO(Handler handler){
            this.handler = handler;

            this.setName(TAG);
        }

        public void setSocket(Socket socket){
            this.socket = socket;

            this.start();
        }

        /**
         * Thread will live til the socket alive.
         *
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
                dos = new DataOutputStream(socket.getOutputStream());

                // set the buffered I/O streams
                bos = new BufferedOutputStream(dos);
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

        /**
         * send method used to transfer file using DataIO background thread.
         * data output stream first sent the file nama and file size.
         * The write the file on buffed output stream 64kb per time.
         *
         * @param uri explored file uri data.
         * */
        public void send(Uri uri){

            // access the file using the uri path location
            File file = new File(uri.toString());
            // get the absolute file
            file = file.getAbsoluteFile();

            try {
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

            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
