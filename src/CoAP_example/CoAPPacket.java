package CoAP_example;

import java.util.Random;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;

public class CoAPPacket {

	private byte[] pktHeader = new byte[4];
	final private int VERSION = 1;
	private int TYPE = 0;
	private int TOKENLENGTH = 0;
	private int CODE = 0;
	private int MESSAGEID = 0x0000;
	private static int MIDPool = 0; 
	private byte TOKEN = 0x00;
	private static byte TokenPool = 0x00;
	private StringBuffer OPTION = new StringBuffer();
	private int OPTIONLENGTH = 0;
	private StringBuffer PAYLOAD = new StringBuffer();
	private int PAYLOADLENGTH = 0;
	
	private void setMessageID() {
		if (MIDPool == 0)
		{
			Random rand = new Random();
			MIDPool = rand.nextInt(1 << 14) + 1;
			this.MESSAGEID = MIDPool;
		}
		else
		{
			MIDPool++;
			this.MESSAGEID = MIDPool;
		}
	}
	private void setMessageID(int MID) {
		this.MESSAGEID = MID;
		if (MID == 0)
		{
			setMessageID();
		}
	}
	private void setToken() {
		if (TokenPool == 0x00)
		{
			Random rand = new Random();
			TokenPool = (byte)(rand.nextInt(0x70) + 1);
			this.TOKEN = TokenPool;
		}
		else
		{
			TokenPool++;
			this.TOKEN = TokenPool;
		}
	}
	private void setToken(byte tok) {
		this.TOKEN = tok;
		if (tok == 0x00)
		{
			this.TOKENLENGTH = 0;
		}
	}
	
	private void setPacketContents(int role) {
		if (role == 1) 
		{
			System.out.println("Packet generation for Client");
			System.out.println("Message Type?");
			System.out.println(" 0) Confirmable");
			System.out.println(" 1) Non-confirmable");
			System.out.println(" 2) Acknowledgement");
			System.out.println(" 3) Reset");
			this.TYPE = CoAPMain.sc.nextInt();
			this.TOKENLENGTH = 1;
			if (this.TYPE == 2)
			{
				System.out.println("Type Message ID to respond");
				this.setMessageID(CoAPMain.sc.nextInt());
				System.out.println("Type Token ID to respond");				
				this.setToken((byte)CoAPMain.sc.nextInt());
			}
			else if (this.TYPE == 3)
			{
				System.out.println("Type Message ID to respond");
				this.setMessageID(CoAPMain.sc.nextInt());
				this.TOKENLENGTH = 0;
			}
			else
			{
				this.setMessageID();
				this.setToken();
			}

			System.out.println("Request Method?");
			System.out.println(" 0) empty");
			System.out.println(" 1) GET");
			System.out.println(" 2) POST");
			System.out.println(" 3) PUT");
			System.out.println(" 4) DELETE");
			this.CODE = CoAPMain.sc.nextInt();
			System.out.println("Type Uri-Path for Option");
			CoAPMain.sc.nextLine();
			this.OPTION.append(CoAPMain.sc.nextLine());
			this.OPTIONLENGTH = this.OPTION.length();
		}
		else if (role == 2)
		{
			System.out.println("Packet generation for Server");
			System.out.println("Message Type?");
			System.out.println(" 0) Confirmable");
			System.out.println(" 1) Non-confirmable");
			System.out.println(" 2) Acknowledgement");
			System.out.println(" 3) Reset");
			this.TYPE = CoAPMain.sc.nextInt();
			this.TOKENLENGTH = 1;
			System.out.println("Type Message ID to respond. To generate, type 0");
			this.setMessageID(CoAPMain.sc.nextInt());
			System.out.println("Type Token ID to respond");				
			this.setToken((byte)CoAPMain.sc.nextInt());
			
			System.out.println("Response Code?");
			System.out.println(" 2.01) Created");
			System.out.println(" 2.02) Deleted");
			System.out.println(" 2.03) Valid");
			System.out.println(" 2.04) Changed");
			System.out.println(" 2.05) Content");
			float rspCode = CoAPMain.sc.nextFloat(); 
			int nClass = (int)rspCode;
			int nDetail = (int)(rspCode * 100) - (nClass * 100);
			this.CODE = (nClass << 5) | (nDetail);
			
			System.out.println("Type payload");
			CoAPMain.sc.nextLine();
			this.PAYLOAD.append(CoAPMain.sc.nextLine());
			this.PAYLOADLENGTH = this.PAYLOAD.length();			
		}
	}
	
	protected static byte[] generatePacket(int role) {
		CoAPPacket pkt = new CoAPPacket();
		pkt.setPacketContents(role);
		
		int tmp = (pkt.VERSION << 6) | (pkt.TYPE << 4) | (pkt.TOKENLENGTH);
		pkt.pktHeader[0] = (byte)tmp;
		pkt.pktHeader[1] = (byte)pkt.CODE;
		pkt.pktHeader[2] = (byte)((pkt.MESSAGEID & 0xFF00) >> 8);
		pkt.pktHeader[3] = (byte)(pkt.MESSAGEID & 0x00FF);
		
		int pktLength = 4 + pkt.TOKENLENGTH + pkt.OPTIONLENGTH + pkt.PAYLOADLENGTH;
		// for option delta + option length field
		if (pkt.OPTIONLENGTH > 0)
			pktLength++;
		// for payload marker
		if (pkt.PAYLOADLENGTH > 0)
			pktLength++;
		
		byte[] bytePacket = new byte[pktLength];
		
		for (int i = 0; i < 4; i++)
		{
			bytePacket[i] = pkt.pktHeader[i];
		}
		int bytePos = 3 + pkt.TOKENLENGTH;
		if (pkt.TOKENLENGTH > 0)
		{
			bytePacket[bytePos] = pkt.TOKEN;
		}
		bytePos++;
		
		if (role == 1)
		{
			if (pkt.OPTIONLENGTH > 0)
			{
				bytePacket[bytePos++] = (byte)((11 << 4) | (pkt.OPTIONLENGTH));
				System.arraycopy(pkt.OPTION.toString().getBytes(), 0, bytePacket, bytePos, pkt.OPTIONLENGTH);				
			}
		}
		else if (role == 2)
		{
			bytePacket[bytePos++] = (byte)0xFF;
			System.arraycopy(pkt.PAYLOAD.toString().getBytes(), 0, bytePacket, bytePos, pkt.PAYLOADLENGTH);
		}

		// Check bytes of generated packet
		for (byte b : bytePacket)
		{
			System.out.printf("%02x ", b);
		}
		System.out.println();
		
		return bytePacket;
	}
	
	protected static void sendPacket(byte[] bytePacket) throws IOException {
		FileOutputStream output = new FileOutputStream("d:/packet.txt", false);
		output.write(bytePacket);
		
		output.close();
	}
	
	protected static void parsePacket() throws IOException  {
		File file = new File("d:/packet.txt");
		FileInputStream input = new FileInputStream(file);
		byte[] bytePacket = new byte[(int)file.length()];
		input.read(bytePacket);

		// Check source bytes to parse
		/*
		for (byte b : bytePacket)
		{
			System.out.printf("%02x ", b);
		}
		System.out.println();
		*/
		
		CoAPPacket rcvPacket = new CoAPPacket();
		for (int i = 0; i < 4; i++)
		{
			rcvPacket.pktHeader[i] = bytePacket[i];
		}
		
		// parse Version
		int tmpVer = (rcvPacket.pktHeader[0] & 0x40) >>> 6;
		if (rcvPacket.VERSION != tmpVer)
		{
			System.err.println("Version mismatch." + tmpVer);
			input.close();
			return;
		}
		else
		{
			System.out.println("...Version: " + rcvPacket.VERSION);
		}
		// parse Type
		rcvPacket.TYPE = (rcvPacket.pktHeader[0] & 0x30) >>> 4;
		System.out.print("...Type: " + rcvPacket.TYPE + ") ");
		switch (rcvPacket.TYPE)
		{
		case 0:
			System.out.println("Confirmable");
			break;
		case 1:
			System.out.println("Non-confirmable");
			break;
		case 2:
			System.out.println("Acknowledgement");
			break;
		case 3:
			System.out.println("Reset");
			break;
		default:
			System.err.println("Unknown Type");
			break;
		}
		// parse Token Length
		rcvPacket.TOKENLENGTH = rcvPacket.pktHeader[0] & 0x0F;
		System.out.println("...Token Length: " + rcvPacket.TOKENLENGTH);
		// parse Code
		rcvPacket.CODE = rcvPacket.pktHeader[1];
		int nClass = (rcvPacket.CODE & 0xE0) >>> 5;
		int nDetail = rcvPacket.CODE & 0x1F;
		System.out.printf("...Code: %d.%02d) ", nClass, nDetail);

		switch (rcvPacket.CODE)
		{
		case 0:
			System.out.println("empty");
			break;
		case 1:
			System.out.println("GET");
			break;
		case 2:
			System.out.println("POST");
			break;
		case 3:
			System.out.println("PUT");
			break;			
		case 4:
			System.out.println("DELETE");
			break;
		case 65:
			System.out.println("Created");
			break;
		case 66:
			System.out.println("Deleted");
			break;
		case 67:
			System.out.println("Valid");
			break;
		case 68:
			System.out.println("Changed");
			break;
		case 69:
			System.out.println("Content");
			break;
			
		default:
			System.err.println("Unknown Code");
			break;			
		}
		// parse MessageID
		rcvPacket.MESSAGEID = (int)((rcvPacket.pktHeader[2] << 8) | rcvPacket.pktHeader[3]);
		System.out.printf("...Message ID: 0x%02x%02x (%d)\r\n", rcvPacket.pktHeader[2], rcvPacket.pktHeader[3], rcvPacket.MESSAGEID);
		
		if (bytePacket.length < 5)
		{
			input.close();
			return;
		}
		
		// parse Token
		int bodyPos = 3;
		if (rcvPacket.TOKENLENGTH > 0)
		{
			bodyPos += rcvPacket.TOKENLENGTH;
			rcvPacket.TOKEN = bytePacket[bodyPos];
			System.out.printf("...Token ID: 0x%02x (%d)\r\n", rcvPacket.TOKEN, rcvPacket.TOKEN);
		}
		bodyPos++;
		
		if (bytePacket[bodyPos] != (byte)0xFF)
		{
			// parse Option
			int opDelta = (bytePacket[bodyPos] & 0xF0) >>> 4;
			System.out.print("...Option Delta: " + opDelta + ") ");
			switch (opDelta)
			{
			case 11:
				System.out.println("Uri-Path");
				break;				
			default:
				System.err.println("Unknown Option");
				break;
			}
			rcvPacket.OPTIONLENGTH = bytePacket[bodyPos] & 0x0F;
			System.out.println("...Option Length: " + rcvPacket.OPTIONLENGTH);
			bodyPos++;
			
			for (int i = bodyPos; i < bytePacket.length; i++)
			{
				rcvPacket.OPTION.append((char)bytePacket[i]);
			}
			System.out.println("...Option: " + rcvPacket.OPTION);
		}
		else
		{
			for (int i = bodyPos + 1; i < bytePacket.length; i++)
			{
				rcvPacket.PAYLOAD.append((char)bytePacket[i]);
			}
			System.out.println("...Payload: " + rcvPacket.PAYLOAD);
		}
		
		input.close();
	}
}
