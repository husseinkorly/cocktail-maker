import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ApplicationStart {
	
	/**Server Variables*/
	private static final String SMSSERVERIP = "127.0.0.1";
	private static final String BBBKDBIP = "192.168.1.7";
	private static final String BBBKCONTROLLERIP = "192.168.1.11";
	private static final int SMSSERVERPORT  = 9431;
	private static final int BBBKDBPORT  = 7521;
	private static final int BBBKCONTROLLERPORT  = 6432;
	
	/**recordList is for utilizing current QR codes and Phone Numbers*/
	private static ArrayList<Record> recordList = new ArrayList<Record>();
	/**qrCodeList is for holding valid QR codes*/
	private static ArrayList<String> qrCodeList = new ArrayList<String>();
	/**inventory keeps track of the different liquids in machine*/
	private static ArrayList<Liquid> inventory = new ArrayList<Liquid>();
	/** The calculated amount to be dispensed **/
	private static ArrayList<Liquid> dispenseAmount = new ArrayList<Liquid>();
	
	/**Size Amounts**/
	private static int small = 6;
	
	private static int medium = 10;
	
	private static int large = 14;		
	
	/**current menu**/	
	private static ArrayList<String> menu = new ArrayList<String>();
	/**menu Message**/
	private static String menuMessage = "Drink Menu: ";
	/** Current Size of Order **/
	private static String currSize = "";
	/** Current name of Order **/
	private static String currOrder = "";
	/** Last QR Code Read from QR Reader **/
	private static String lastQRCodeRead = "";

	public static void main(String[] args) throws IOException {
		//Need to get the parameters name amount strength
				
		
		//**Create Liquid Array */
		for(int i = 0; i < 3; i ++ ){
			Liquid liquid = new Liquid(args[3*i], Double.parseDouble(args[3*i+1]), Integer.parseInt(args[3*i +2]));
			inventory.add(liquid);
			
		}
		//inventory
		System.out.println("Current Inventory");
		for(Liquid s: inventory){
			System.out.println(s.toString());
		}
		
		//Add the QRCode to the List
		qrCodeList.add("cupOneqr");
		qrCodeList.add("cupTwoqr");
		qrCodeList.add("cupThreeqr");
		
		//RegisterUser
		registerUser("cupOneqr","8049263633");
		
		menu.add("blue");
		menu.add("red");
		menu.add("yellow");
		menu.add("purple");
		menu.add("orange");
		menu.add("brown");
		//menu.add("aqua");
		
		
		for(int i = 0; i < menu.size(); i++){
			
			menuMessage += i + "-" + menu.get(i);
			if(i == menu.size() -1){
				menuMessage += ". Reply with Number and Size of Drink.";
			} else {
				menuMessage += ", ";
			}
		}
		//System.out.println(menuMessage);
		
		//TCP Server Code here
		
		ServerSocket serverSocket = new ServerSocket(9876);

		//byte[] receiveData = new byte[1024];
		String receiveData;

		while (true)

		{
			
			Socket connectionSocket = serverSocket.accept();
			
			BufferedReader incomingMessage = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

			receiveData = incomingMessage.readLine();
			
			System.out.println(handleReadString(receiveData));
			
			System.out.println("From Client: " + receiveData);
			
			connectionSocket.close();
			
			/*
			DatagramPacket receivePacket =

					new DatagramPacket(receiveData, receiveData.length);

			serverSocket.receive(receivePacket);

			String sentence = new String(receivePacket.getData());

			InetAddress IPAddress = receivePacket.getAddress();

			int port = receivePacket.getPort();
			
			System.out.println(handleReadString(sentence));
			
			receiveData = new byte [1024];
			*/

			//System.out.println("From Client" + sentence);

		}
		// serverSocket.close();
		
	}
	/**
	 * Splits all the input string
	 * @param inputString
	 * @return
	 */
	public static String handleReadString(String inputString){
		System.out.println("Handleing String " + inputString);
		char msgCode = inputString.charAt(0);
		String action = "";
		String splitString[] = inputString.split(",");
		//handles the first code
		switch(msgCode){
			case '1':
				action = "QR Code Recieved";				
				String sentQRCode = splitString[1].toString().trim();
				boolean isInRecord = false;
				
				
				lastQRCodeRead = sentQRCode;

				System.out.println("lastQRCodeRead: " + lastQRCodeRead);

				
				/**
				//if what is sent is a QR Code
				if (verifyCode(sentQRCode)){
					//if this is a code, now check the record list for who's number is associated
					for(int i = 0; i < recordList.size(); i ++){
						//print out the records
						//System.out.println("RecordList" + recordList.get(i).getStringQRCode());
						
						//This is what phone number I need to send the menu to
						if(recordList.get(i).getStringQRCode().equals(sentQRCode)){
														
							//System.out.println("Send Menu to: " + recordList.get(i).getPhoneNumber());
							
							//prompt user to send QR Code
							
							
							//sendtoSMS(recordList.get(i).getPhoneNumber(), menuMessage);
							
							isInRecord = true;
							break;
						}
					}
					if(!isInRecord){
						System.out.println("Not in List, Register Cup");
					}
				} else {
					System.out.println("Not valid cup QR Code");
				}				
				**/
				//need to send message to the text which will deliver the menu
				
				break;
			case '2':
				//From SMS Server
				action = "Message from SMS Server Receieved";
				char innerFlag = splitString[1].toString().trim().charAt(0);
				String phoneNumber = splitString[2].toString().trim();
				System.out.println("InnerFlagis: "+ innerFlag+ ".");
				switch(innerFlag){
					//Registering Phone Number and QR Code
					case '1':
						System.out.println("This is in inner flag");
						String qrcode = splitString[3].toString().trim();
						
						if(registerUser(qrcode,phoneNumber)){
							sendtoSMS("1","1");
						} else {
							sendtoSMS("1","2");
						}
												
						break;
					//Sending Order to UI Server Program PDU
					
					case '2':
						//Receive the Order
						currOrder = splitString[3].toString().trim();
						currSize = splitString[4].toString().toLowerCase().trim();
						
						//I receive the phone number 
						//check for QR Code
						
						for(Record rec: recordList){
							//phone number does equal qr code.
							System.out.println("Phone Number " + rec.getPhoneNumber() + "QR Code" + rec.getStringQRCode() + "Last QRCode" + lastQRCodeRead);
							if(phoneNumber.equals(rec.getPhoneNumber()) && lastQRCodeRead.equals(rec.getStringQRCode())){
								sendtoSMS("2","1");
								System.out.println("User: " + phoneNumber + " ordered " + menu.get(Integer.parseInt(currOrder)) + "drinkSize " + currSize);
								sendRecipeServer(menu.get(Integer.parseInt(currOrder)));
								return action;
								
							} else if(lastQRCodeRead.equals(rec.getStringQRCode())){
								sendtoSMS("2","3");
								break;
							}
						}
						
						sendtoSMS("2","2");			
						
						//take order then send it to BBBK DB 
						
						//then receive the order over there in case 3
						
						break;	
				}
				
				break;
			case '3':
				//From Recipe Server
				System.out.println("Message From Recipe Server");
			
				ArrayList<Liquid> inputAmount = new ArrayList<Liquid>();
				//store the orders in the amount
				//System.out.println("String Length: " + splitString.length);
				
				int totalParts = 0;
				
				//Parse and add the Recipe
				for(int i = 1; i < 6; i++){
					
					if(i < splitString.length ){
						
						System.out.println("Name: " + splitString[i].toString() + " Amount: " + splitString[i+1].toString());
						
						inputAmount.add(new Liquid (splitString[i], 1.0 , Integer.parseInt(splitString[i+1])));
						//calculate total parts
						totalParts += Integer.parseInt(splitString[i+1]);
						
					}
					i++;
				}
				//need to calculate
				
				double amount = 0;
				
				if(currSize.equalsIgnoreCase("s")){
					amount = small;
				} else if(currSize.equalsIgnoreCase("m")){
					amount = medium;
				} else {
					amount = large;	
				}
				
				//System.out.println("TotalParts: " + totalParts + " amount: " + amount);		
								
				
				System.out.println("Liquids from Recipe DB");
				
				boolean dispense = false;				
				
				//double totalStrength = 0.0;
							
				//Test Strength Array delete and turn oz into a double
				
				ArrayList<Double> ozresults = new ArrayList<Double>();
				
				double totalcoefficient = 0.0;
				double endingRatio = 0.0;			
				
				//check in the inventory
				for(Liquid t: inventory){
					//check the input amount
					dispense = false;
					for(Liquid s: inputAmount){
						//check the input amount name and the liquid name
						if(s.getLiquidName().equals(t.getLiquidName())){
							
							//need to also include the strength
							//System.out.println(t.getLiquidName() + " parts " + (((double)(s.getOunce())/(double)(totalParts))) * amount);
							//System.out.println(t.getLiquidName() + " parts " + (((double)(s.getOunce())/(double)(totalParts))) * amount);
							
							totalcoefficient += ((double) s.getOunce()/(double)t.getDrinkStrength());
																					
							//TODO need to see if we can do it by the decimal point
							//if so then I can allow decimals
							//totalStrength += t.getDrinkStrength();
							
							ozresults.add(((double) s.getOunce()/(double)t.getDrinkStrength()));
							dispenseAmount.add(new Liquid (t.getLiquidName(), t.getDrinkStrength(), s.getOunce()));
							dispense = true;
						} 
					}
					if(!dispense){
						dispenseAmount.add(new Liquid ("",0.0,0));
						ozresults.add(0.0);
					}
					
				}
				endingRatio = amount/totalcoefficient;
				
				//System.out.println("Total Coefficient: " + totalcoefficient + " EndingRatio: " + endingRatio);
				
				//System.out.println("I hope this works");
				for(Liquid s: dispenseAmount){
					//System.out.println(s.getLiquidName() + " parts " + ((double)s.getOunce()) *);
					//System.out.println("Name: " + s.getLiquidName());
					if(s.getOunce() != 0.0){
						//System.out.println("ozresults size: " + ozresults.size() + " dispenseAmount size: " + dispenseAmount.size() + "Indexof: " + dispenseAmount.indexOf(s) );
						
						//System.out.println(s.getLiquidName() + " rest of ratio " + endingRatio * ozresults.get(dispenseAmount.indexOf(s)));
						
						s.setOunce(endingRatio * ozresults.get(dispenseAmount.indexOf(s)));
						
					}
					//System.out.println(s.getLiquidName() + " results " + (s.getDrinkStrength()/totalStrength) * ozresults.get(dispenseAmount.indexOf(s)));
				}
				
				
				//Subtract the amount from the current inventory
				for(Liquid drink: inventory){
					for(Liquid s: dispenseAmount){
						//check the input amount name and the liquid name
						if(s.getLiquidName().equals(drink.getLiquidName())){
							
							double result = (double) drink.getOunce() - s.getOunce();							
							drink.setOunce(result);						
							
						}
					}
				}
				
				//Build the dispenseAmount String
				String dispenseString = new String();
				ArrayList<String> stringDrinks = new ArrayList<String>();
				stringDrinks.add("A");
				stringDrinks.add("B");
				stringDrinks.add("C");
				int count = 0;
				
				for(Liquid drink: dispenseAmount){
					dispenseString +=  stringDrinks.get(count) + "=" + drink.getOunce() + ",";
					count++;
				}
				
				dispenseString = dispenseString.substring(0, dispenseString.length()-1);
				
				System.out.println("Sent Dispensing String: " + dispenseString);	
				
				dispenseAmount.clear();
				
				System.out.println("Total Inventory");				
				for(Liquid total: inventory){
					System.out.println(total.toString());
				}
				
				//TODO UNCOMMENT THIS WHEN TIME TO SEND
				sendValveController(dispenseString);
				
				System.out.println("This is 3");
				break;	
			case '4':
				//From Valve Controller
				System.out.println("This is 4");
				break;	
			default:
				System.out.println("This was a default");
		}		
		
		return action;		
	}


	/**
	 * Used to Register a user within the current ArrayList.
	 * @param qrCode
	 * @param phoneNumber
	 * @return
	 */
	public static boolean registerUser (String qrCode, String phoneNumber){
		
		Record r = new Record();
		r.setPhoneNumber(phoneNumber);
		r.setStringQRCode(qrCode);	
		
		for(Record rec: recordList){
			//check if user is already registered
			if(rec.getPhoneNumber().equals(r.getPhoneNumber()) && rec.getStringQRCode().equals(r.getStringQRCode())){
				
				return true;
				//check if code has been registered
			} else if(rec.getStringQRCode().equals(qrCode)){
				return false;
				//check if phone number is already in system if so return false
			} else if (rec.getPhoneNumber().equals(phoneNumber)){
				return false;
			}
		}	
		
		//if new entry add
		recordList.add(r);
		System.out.println("Phone Number: " + phoneNumber + " is registered with cup: " + qrCode);
		return true;
		


	}
	/**
	 * Check if QRCode is in current list
	 * @param qrCode
	 * @return
	 */
	public static boolean verifyCode(String qrCode){
		if(qrCodeList.contains(qrCode)){
			return true;			
		}		
		return false;		
	}
	
	/**
	 * Send request out to Recipe Server
	 * @param message 
	 */
	public static void sendRecipeServer(String message){
		//TODO Communicate with the Recipe Server
		String ip = BBBKDBIP;
		int port = BBBKDBPORT;
		
		//send the information to the server.		
		try {
			sendTCP("", message, ip, port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Send request out to Valve Controller
	 */
	public static void sendValveController(String message){
		//TODO Communicate with the Valve Controller

		//TODO Communicate with the Recipe Server
		String ip = BBBKCONTROLLERIP;
		int port = BBBKCONTROLLERPORT;
		
		//send the information to the server.		
		try {
			sendTCP("", message, ip, port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * What to send to the server
	 * @param param1 - flag indicates whether it relates to registering or ordering
	 * @param param2 - flag indicates the actual result of the action
	 * @throws Exception 
	 */
	public static void sendtoSMS(String param1, String param2){		
		
		try {
			sendTCP(param1, param2, "127.0.0.1", 9431);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Method for sending information over UDP
	 * @param flag
	 * @param content
	 * @param ip
	 * @param port
	 * @return
	 */
	public static void sendUDP(String flag, String content, String ip, int port) throws Exception{
		
		String message = flag + "," + content;
		
		DatagramSocket clientSocket = new DatagramSocket();
		
		InetAddress IPAddress = InetAddress.getByName(ip); 
		
		byte[] sendData = new byte[1024]; 
		
		sendData = message.getBytes();
		
	    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
	    
	    clientSocket.send(sendPacket);
	    
	    clientSocket.close();	
		
	}
	/**
	 * Method for sending information over TCP
	 * @param flag
	 * @param content
	 * @param ip
	 * @param port
	 * @throws Exception
	 */
	public static void sendTCP(String flag, String content, String ip, int port) throws Exception{
		String message;
		if(flag.equals("")){
			message = content;
		}else {
			message = flag + "," + content;	
		}
		
		
		System.out.println("Sending by TCP " + message + "to IP: " + ip + " Port:" + port);
		//TCP Implementation
		Socket clientSocket = new Socket(ip, port);

		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
		
		out.println(message);
        
		
		//outToServer.writeBytes(message + '\n');
		
		//May need to look into sending acks back
		clientSocket.close();
		
	}
	
	/*

	public static String readQRCode(String filePath, String charset, Map hintMap)
			throws FileNotFoundException, IOException, NotFoundException {
		BinaryBitmap binaryBitmap = new BinaryBitmap(
				new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(filePath)))));
		Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
		return qrCodeResult.getText();
	}*/
}
