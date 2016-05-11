package ubicom.org.NoiseMonitoring;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class ServerSocket implements Runnable {
	
	
	protected ServerSocketChannel serverChannel;
    protected Selector selector;
    
    protected Map<SocketChannel,byte[]> dataTracking = new HashMap<SocketChannel, byte[]>();
    
    // The buffer into which we will read data when it is available
    protected ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    
    private static byte[] copyBuffer = new byte[6048];
    
    private static int numOfByteReceive = 0;
    
    protected final static long TIMEOUT = 10000;
    
    
    private Iterator<SelectionKey> keys;
    
    private SelectionKey key;
    
    protected Thread t = null;
    private Thread dataWorkerThread = null;
    
    private DataProcessWorker dataWorker;
    
    // A list of ChangeRequests instances   
	private LinkedList changeRequests = new LinkedList();
	
	// Maps a socketChannel to a list of ByteBuffer instances
	@SuppressWarnings("rawtypes")
	private Map pendingData = new HashMap();
    
    //protected String threadName;
    
    //Log
    private static final Logger logger = LogManager.getLogger(ServerSocket.class.getName());
    
    private static Set<Socket> clientList = Collections.synchronizedSet(new HashSet<Socket>());
    
    public ServerSocket (String ADDRESS,int PORT){
    	
    	init(ADDRESS,PORT);
    }
	
    public void start ()
    {
       
       if (t == null)
       {
          t = new Thread(this);
          t.start ();
       }
     
    }
    
    
    private void init(String ADDRESS,int PORT)
    {
        //System.out.println("initializing socket server: ADDRESS: " + ADDRESS + " PORT: " + PORT);
        
        logger.debug("initializing socket server ADDRESS: " + ADDRESS + " PORT " + PORT);
        
    	if (selector != null) return;
        if (serverChannel != null) return;
 
        try {
            // This is how you open a Selector
            selector = Selector.open();
            // This is how you open a ServerSocketChannel
            serverChannel = ServerSocketChannel.open();
            // You MUST configure as non-blocking or else you cannot register the serverChannel to the Selector.
            serverChannel.configureBlocking(false);
            // bind to the address that you will use to Serve.
            serverChannel.socket().bind(new InetSocketAddress(ADDRESS, PORT));
 
            /**
             * Here you are registering the serverSocketChannel to accept connection, thus the OP_ACCEPT.
             * This means that you just told your selector that this channel will be used to accept connections.
             * We can change this operation later to read/write, more on this later.
             */
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            
            
            //Start dataWorkerThread to process received packets
            this.dataWorker = new DataProcessWorker();
            dataWorkerThread = new Thread(dataWorker);
     	    dataWorkerThread.start();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	public void run()
	{
		//System.out.println("Now accepting connections...");
        try{
            // A run the server as long as the thread is not interrupted.
            while (!Thread.currentThread().isInterrupted()){
            	synchronized(this.changeRequests)
            	{
            		Iterator changes = this.changeRequests.iterator();
            		while(changes.hasNext()){
            			ChangeRequest change = (ChangeRequest)changes.next();
            			switch(change.type)
            			{
            				case ChangeRequest.CHANGEOPS:
            					SelectionKey key = change.socket.keyFor(this.selector);
            					key.interestOps(change.ops);
            			}
            		}
            		this.changeRequests.clear();
            	}
            	
            	
            	
                /**
                 * selector.select(TIMEOUT) is waiting for an OPERATION to be ready and is a blocking call.
                 * For example, if a client connects right this second, then it will break from the select()
                 * call and run the code below it. The TIMEOUT is not needed, but its just so it doesn't 
                 * block undefinitely.
                 */
                //this.selector.select(TIMEOUT);
            	this.selector.select();
 
                /**
                 * If we are here, it is because an operation happened (or the TIMEOUT expired).
                 * We need to get the SelectionKeys from the selector to see what operations are available.
                 * We use an iterator for this. 
                 */
                keys = selector.selectedKeys().iterator();
 
                while (keys.hasNext()){
                	
                    key = keys.next();
                    
                    // remove the key so that we don't process this OPERATION again.
                    keys.remove();
 
                    // key could be invalid if for example, the client closed the connection.
                    if (!key.isValid()){
                        continue;
                    }
                    /**
                     * In the server, we start by listening to the OP_ACCEPT when we register with the Selector.
                     * If the key from the keyset is Acceptable, then we must get ready to accept the client
                     * connection and do something with it. Go read the comments in the accept method.
                     */
                    if (key.isAcceptable()){
                       // System.out.println("Accepting connection");
                        accept(key);
                    }
                    /**
                     * If you already read the comments in the accept() method, then you know we changed
                     * the OPERATION to OP_WRITE. This means that one of these keys in the iterator will return
                     * a channel that is writable (key.isWritable()). The write() method will explain further.
                     */
                    if (key.isWritable()){
                       // System.out.println("Writing...");
                        write(key);
                    }
                    /**
                     * If you already read the comments in the write method then you understand that we registered
                     * the OPERATION OP_READ. That means that on the next Selector.select(), there is probably a key
                     * that is ready to read (key.isReadable()). The read() method will explain further. 
                     */
                    if (key.isReadable()){
                        //System.out.println("Reading connection");
                        read(key);
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally
        {
        }
 
	}// run

	public void closeConnection() 
	{
		System.out.println("Closing socket");
        if (selector != null){
            try {
                selector.close();
                serverChannel.socket().close();
                serverChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	protected void accept(SelectionKey key) throws IOException
	{
		
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		
		
		//Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		Socket socket = socketChannel.socket();
		socketChannel.configureBlocking(false);
		logger.debug("Accept Connection: " + " IP Address: " + socketChannel.getRemoteAddress());
		
		//Register the new SocketChannel with our Selector, indicating we'd like
		//to be notified when there's data waiting to be read
		socketChannel.register(this.selector, SelectionKey.OP_READ);
		
		synchronized(clientList){
			try
			{
				
				Socket tempSocket = null;
				for(Socket clientSocket:clientList)
				{
					if(clientSocket.getRemoteSocketAddress() == socket.getRemoteSocketAddress() &&
							clientSocket.getPort() == socket.getPort())
					{
						
						clientList.add(socket);
						break;
					}
					else if(clientSocket.getRemoteSocketAddress() == socket.getRemoteSocketAddress() &&
							clientSocket.getPort() != socket.getPort())
					{
						clientList.remove(tempSocket);
						clientSocket.close();
						clientList.add(socket);
						break;
					}
				}
				
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	protected void read(SelectionKey key) throws IOException
	{ 
		SocketChannel socketChannel = (SocketChannel) key.channel();
		
		// Clear out our read buffer so it is ready for new data
		this.readBuffer.clear();
		int numRead;
		try{
			numRead = socketChannel.read(this.readBuffer);
			
			
		}catch(IOException e){
			
			key.cancel();
			socketChannel.socket().close();
			socketChannel.close();
			
			return;
		}
		if(numRead == -1)
		{
			logger.debug("Number of Byte Received = -1");
			
			/*System.out.println("Total Byte received: " + copyBuffer.length);
			
			if((numOfByteReceive-8)%20 == 0)
			{
				this.dataWorker.processData(this,socketChannel, copyBuffer,numOfByteReceive);
			}*/
			numOfByteReceive = 0;
			key.cancel();
			socketChannel.socket().close();
			key.channel().close();
			return;
		}
		// Hand the data off to our worker thread
		logger.debug("Number of Byte Received: " + numRead);
		
		byte[] tempData = this.readBuffer.array();
		
		//System.out.println("length: " + copyBuffer.length);
		
		//System.arraycopy(tempData, 0, copyBuffer, numOfByteReceive, numRead);
		
		//numOfByteReceive += numRead;
		
		/*for(int i =0;i<numRead;i++)
		{
			logger.debug(tempData[i]);
		}*/
		
		
	    //byte[] copyData = new byte[numRead];
		
		//System.arraycopy(tempData, 0, copyData, 0, 4);
		
	    //String text = new String(copyData,"UTF-8");
	    
	    //logger.debug("String Text: " + text);
		
		if((numRead-8)%20 == 0)
		{
			this.dataWorker.processData(this,socketChannel, this.readBuffer.array(),numRead);
		}
		
		
		
	}
	@SuppressWarnings("rawtypes")
	protected void write (SelectionKey key) throws IOException
	{
		SocketChannel socketChannel = (SocketChannel)key.channel();
		synchronized(this.pendingData){
			ArrayList queue = (ArrayList)this.pendingData.get(socketChannel);
			
			//Write until there is no more data
			
			while(!queue.isEmpty()){
				ByteBuffer buf = (ByteBuffer)queue.get(0);
				
				while(buf.hasRemaining()){
					socketChannel.write(buf);
				}
				
				if(buf.remaining() > 0){
					break;
				}
				queue.remove(0);
			}
			
			if(queue.isEmpty()){
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}
	
	
	
	//////
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void send(SocketChannel socket, byte[] data){
		synchronized(this.changeRequests) {
			
			this.changeRequests.add(new ChangeRequest(socket,ChangeRequest.CHANGEOPS,SelectionKey.OP_WRITE));
			
			// Add queue the data we want to write
			synchronized(this.pendingData){
				ArrayList queue = (ArrayList)this.pendingData.get(socket);
				if(queue == null){
					queue = new ArrayList();
					this.pendingData.put(socket, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
			
		}
		this.selector.wakeup();
	}
	
	
	
	
}
