package CoAP_example;

public class CoAPMenu {
	
	static int nRet = -1;
	static int nInput = -1;

	public CoAPMenu() {
	}
	
	protected static int showmenu() {
		System.out.println("=================================");
		System.out.println(" 1) Client action");
		System.out.println(" 2) Server action");
		System.out.println(" 0) exit");
		System.out.println("=================================");
		
		nInput = CoAPMain.sc.nextInt();
		if (nInput == 1)
		{
			nRet = subMenu(1);
		}
		else if (nInput == 2)
		{
			nRet = subMenu(2);
		}
		else if (nInput == 0)
		{
			nRet = -1;
		}
		else
		{
			System.out.println("Wrong input.");
			nRet = -1;
		}

		return nRet;
	}
	
	private static int subMenu(int role) {
		System.out.println("=================================");
		System.out.println(" 0) Stop thread");
		System.out.println(" 1) Generate Packet");
		System.out.println(" 2) Send Packet");
		System.out.println(" 99) Main menu");
		System.out.println("=================================");
		
		nInput = CoAPMain.sc.nextInt();
		int nRet = role * 10 + nInput;

		return nRet;
	}
}
