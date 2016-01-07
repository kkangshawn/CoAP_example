package CoAP_example;

import java.util.Scanner;

class CoAPThread implements Runnable {

	String strRole;
	boolean bServerThread = false;
	boolean bClientThread = false;
	boolean bCallback = false;
	
	public CoAPThread() {
		this("default");
	}
	public CoAPThread(String str) {
		this.strRole = str;
		if (str.equals("Server"))
			this.bServerThread = true;
		else if (str.equals("Client"))
			this.bClientThread = true;
	}
	
	public void run() {
		System.out.println(strRole + " thread starts");
		while (bServerThread || bClientThread)
		{
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				this.bClientThread = false;
				this.bServerThread = false;
			}
			
			if (bCallback)
			{
				System.out.println(strRole + " received callback!");
				this.bCallback = false;
				try {
					CoAPPacket.parsePacket();	
				} catch (Exception e) {
					
				}
			}
//			System.out.println(strRole + " thread running");
		}
	}
}

public class CoAPMain {

	public static CoAPThread serverThread;
	public static CoAPThread clientThread;	
	public static Scanner sc = new Scanner(System.in);
	private static byte[] btPacket;

	
	public static void invokeCommand(int nRet) {
		switch (nRet)
		{
		// 1x for Client
		case 10:
			System.out.println("Stop Client Thread");
			clientThread.bClientThread = false;
			break;
		
		case 11:
			btPacket = CoAPPacket.generatePacket(1);
			break;
		
		case 12:
			try {
				CoAPPacket.sendPacket(btPacket);
				serverThread.bCallback = true;
			}
			catch (Exception e) {
				
			}
			break;
		
		// 2x for Server
		case 20:
			System.out.println("Stop Server Thread");
			serverThread.bServerThread = false;
			break;

		case 21:
			btPacket = CoAPPacket.generatePacket(2);
			break;
			
		case 22:
			try {
				CoAPPacket.sendPacket(btPacket);
				clientThread.bCallback = true;
			}
			catch (Exception e) {
				
			}
			break;

		default:
//			System.out.println("invoke command: " + nRet);
			break;
		}
	}
	
	public static void main(String args[]) {
		serverThread = new CoAPThread("Server");
		clientThread = new CoAPThread("Client");
		Thread tServer = new Thread(serverThread);
		Thread tClient = new Thread(clientThread);
		tServer.start();
		tClient.start();
		
		while (true)
		{
			int nMenuReturn = CoAPMenu.showmenu();
			invokeCommand(nMenuReturn);
			if (nMenuReturn == -1)
			{
				clientThread.bClientThread = false;
				serverThread.bServerThread = false;
				break;
			}
		}
		
		sc.close();
	}
}
