package ubicom.org.NoiseMonitoring;
import java.nio.channels.SocketChannel;

public class ServerDataEvent {
	public ServerSocket server;
	public SocketChannel socket;
	public byte[] data;
	
	//Constructor
	public ServerDataEvent(ServerSocket server, SocketChannel socket, byte[] data){
		this.server = server;
		this.socket = socket;
		this.data = data;
	}
}
