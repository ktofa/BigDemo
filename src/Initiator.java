import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;
import java.util.Calendar;
import java.text.SimpleDateFormat;


/* 
Utility class built to act as a helper tool for the simulation of Telecom call data which can be used as 
Proof of concept for Big Data demonstrations.


The applications uses preconfigured parameters ( such as Phone numbers, Account Information, Product Information)
to simulate calls.

Simulated calls are recorded within generated CDR files.

Current version only support Voice Calls.

Author: Christopher Ogirri
Version: 1.0



*/
public class Initiator {



	Subscriber onnet;
	Subscriber offnet;
	List<Subscriber> onnetList=new ArrayList<Subscriber>();
	List<Subscriber>  offnetList=new ArrayList<Subscriber>();
	HashMap<String,Product> products = new HashMap<String,Product>();
	//int prepaidSubCount=0;
	//int postpaidSubCount=0;
	

	//Set up of Configuration Directory, set up as environment variable within the executing platform
	String confDir=System.getenv("BIGDEMO_CONF_DIR");
	
	//Set up data directory where all CDR information would be stored
	String dataDir=System.getenv("BIGDEMO_DATA_DIR");
	
	//Set date format for all string within the app
	String format="yyyyMMddHHmmss";
	
	//Set Timing variables
	private Calendar currentCal= null;
	
	//Set Maximum difference between simulated calls
	private int MAX_CALL_TIME_DIFFERENCE=3;
	
	//Recharge Value ..Fixed
	private String rechargeValue = "100000000";
	
	//Fixed values for Basestation information
	private String hlr_number="2348060001504";
	
	//Sequencing INformation
	String lastSeq= null;
	
	Initiator() throws IOException {
	
		if (( null == confDir) || (null == dataDir)){
			System.out.println(" Configuration Directory is not set.... Application exiting");
			System.exit(1);
		}
		//Get configured phone number resources
		String onnetConfFile = confDir + "onnet.numbers";
		String offnetConfFile = confDir + "offnet.numbers";
		
		//Get configured Product Information
		getProductInformation(confDir +"products.list");
		
		//Setup Customer/Information Data Structures and lifecycle
		createSubList(getSubInformation(onnetConfFile),0);
		createSubList(getSubInformation(offnetConfFile),1);
		
		//Perform post-setup check
		System.out.println("Total count of prepaid subscribers is " + onnetList.size());
		System.out.println("Total count of postpaid subscribers is "  + offnetList.size());
		
		//Initialise Sequencing
		
		if (null==System.getenv("BIGDEMO_LAST_SEQ")) {
			lastSeq="7654321000";
		}
		else {
			lastSeq=System.getenv("BIGDEMO_LAST_SEQ");
			
		}
		
		while(true) { initiateCall();}
		
			
		

	}
	
	
	//Read Subscriber information from the Configured Subscriber file. 
	// Please see the README for file template information
	private List<String> getSubInformation(String confFile) throws IOException {
		//String onnetConfFile = confDir + "onnet.numbers";
		ArrayList<String> tempList = new ArrayList<String>();
		StringBuilder text = new StringBuilder();
    		String NL = System.getProperty("line.separator");
    		Scanner scanner = new Scanner(new FileInputStream(confFile));
    		try {
     			 while (scanner.hasNextLine()){
        			tempList.add((scanner.nextLine()));
      			}
    		}
    		finally{
      			scanner.close();
    		}
    		return tempList;
		
	}

	//Read Product information from the Configured Product Information file.
	private void getProductInformation(String confFile) throws IOException {
		
		ArrayList<String> tempList = new ArrayList<String>();
                StringBuilder text = new StringBuilder();
                String NL = System.getProperty("line.separator");
                Scanner scanner = new Scanner(new FileInputStream(confFile));
		  try {
                         while (scanner.hasNextLine()){
                                tempList.add((scanner.nextLine()));
                        }
                }
                finally{
                        scanner.close();
                }

		
		for (String prod: tempList) {
			String[] partProd = prod.split(",");
			
			Product tempProd = new Product(partProd[0],partProd[1],partProd[2]);
			products.put(partProd[0],tempProd);
			
		} 

	}
	// Initialise Subscriber data structures along with their subscription type ( onnet or offnet)
	private void createSubList(List<String> subList,int subType) throws IOException {

		if (0==subType) {//Onnet Numbers
			for (String partSub : subList) {
				String[] currentSub = partSub.split(",");
				String dn=currentSub[0];
				String imsi=currentSub[1];
				String iccid=currentSub[2];
				Product product=getProduct(currentSub[3]);
				String acctType=currentSub[4];
				String balance=currentSub[5];
				onnetList.add(new Subscriber(dn,imsi,iccid,product,acctType,balance));
				//System.out.println("Adding new Subscriber to the Onnet Sub List... Total Onnet subs: " + onnetList.size());	
			}
		}
		else if (1==subType) {//Offnet Numbers
			for (String partSub : subList) {
                                String[] currentSub = partSub.split(",");
                                String dn=currentSub[0];
                                String imsi=currentSub[1];
                                String iccid=currentSub[2];
				offnetList.add(new Subscriber(dn,imsi,iccid,null,"0","0"));
				//System.out.println("Adding new Subscriber to the offnet Sub List... Total offnet subs: " + offnetList.size());

			}			
		}
	}
	
	// Function to get Product information ( charging information) as identified by their Product ID
	private Product getProduct(String prodID) {

		return products.get(prodID);

	}
	
	private int getMaximumDuration(String currentCredit, String currentPrice) {
		double currentCreditDouble = Double.parseDouble(currentCredit);
		double currentPriceDouble = Double.parseDouble(currentPrice);
		
		return (int)(currentCreditDouble/currentPriceDouble);
	}
	
	private int getRandom(int limit) {
		java.util.Random randomizer = new java.util.Random();
		int nextRandInt = randomizer.nextInt(limit);
		//System.out.println("Seeder for Random is : " + limit + " and random result is " + nextRandInt );
		return nextRandInt;
	
	}
	
	private Calendar getCurrentTime() {
		
		return Calendar.getInstance();
	}
	
	
	
	private String getCalendarTime(Calendar calendar) {
		SimpleDateFormat df = new SimpleDateFormat(format);
		Calendar cal = calendar;
		return df.format(cal.getTime());
	}
	
	private Calendar addChangedTime(Calendar newCalendar, int callDuration) {
		newCalendar.add(Calendar.SECOND, callDuration);
		return newCalendar;
	}
	
	
	
	//Method to simulate call
	private void initiateCall() {
		Subscriber MO=onnetList.get(getRandom(onnetList.size()));
		Subscriber MT=null;
		int callType =-1;
		
		if ( null == currentCal) currentCal= getCurrentTime();
		
	
		
		//Determine if the call is offnet or onnet; if generated value is less than 6 then the call is offnet else its onnet
		boolean callDecider=getRandom(10)>6 ? true : false;
		
		if ( callDecider) {
			MT=offnetList.get(getRandom(offnetList.size()));
			callType=1;
			}
		else {
			while ((MT == null) || (MT == MO)) { // ensure the same number is not selected
				MT=onnetList.get(getRandom(onnetList.size()));
				
			}
			callType=0;
		
		}
		
		//System.out.print(" Randomly selected MO is : " + MO.getDN() + " while selected MT is :" + MT.getDN() + " ");
		//System.out.println(" Call Type is " + callType + " with product " + MO.getProduct());
		
		//Determine call duration and call charge rate
		String rate=(0==callType)? MO.getProduct().getOnnetRate() : MO.getProduct().getOffnetRate() ;
		int usableCallDuration = getMaximumDuration(MO.getBalance(),rate);
		//System.out.println("Usable Duration is: " + usableCallDuration);
		if (2>=usableCallDuration) {
			recharge(MO);
			usableCallDuration = getMaximumDuration(MO.getBalance(),rate);
		}
		int callDuration = getRandom(usableCallDuration);
		
		
		int callCost= callDuration * (Integer.parseInt(rate));
		callCost = -callCost;
		MO.setBalance(getNewBalance(MO.getBalance(), callCost));
	
		logCall(MO,MT,callDuration,callCost,currentCal,callType);
		currentCal = addChangedTime(currentCal, getRandom(MAX_CALL_TIME_DIFFERENCE));
		
		
		
	}
	
	private int getNewBalance(String currentBal, int callCost) {
		int currentBalance= Integer.parseInt(currentBal);
		
		return currentBalance + callCost;
		
	
	}
	
	private void logCall(Subscriber MO, Subscriber MT,int callDuration,int callCost, Calendar cal,int callType)  {
		String s="|";
		String z="0";
		String o="1";
		String d= MO.getDN();
		String t=getCalendarTime(cal);
		String p=(MO.getProduct()).getProdID();
		String b= MO.getBalance();
		String c= new String(""+callDuration);
		String a=MO.getAcctType();
		String cc = new String(""+callCost);
		
		String callLog= incrSeq()+s+z+s+t+s+"91"+s+d+s+MT.getDN()+s+MO.getIMSI()+s+s+d+s+s+o+s+"00"+s+MO.getICCID()+s+hlr_number+s;
		callLog += MO.getIMSI()+s+hlr_number+s+s+t+"32"+s+z+s+z+s+t+s+z+s+"593"+s+z+s+"22724DE005"+s+s+"593"+s+s+s+z+s+"1403"+s;
		callLog += p +s+d+s+o+s+z+s+t+s+z+s+z+s+z+s+z+s+z+s+s+"234"+s+o+s+"2"+s+"234"+s+o+s+o+s+"234"+o+s+o+s+"234"+s+o+s+o+s;
		callLog += p+"1000000"+s+incrSeq()+"2"+s+s+z+s+z+s;
		callLog += b+s+b+s+c+s+z+s+z+s+"1003219384"+s+s+s+a+s+"2"+s+b+s+cc+s+"4500"+s+z+s+z+s+z+s+s+s+s+s+s+z+s+z+s+z+s+z+s+z+s+z+s+z+s;
		callLog += z+s+z+s+z+s+s+s+s+s+s+z+s+z+s+z+s+z+s+z+s+z+s+z+s+z+s+z+s+z+s+z+s+z+s+z+s+z+s+z+s;
		callLog += t+s+t+s;
		
		System.out.println(callLog);
		//callLog += 
	}
	
	private void recharge(Subscriber sub) {
		sub.setBalance(Integer.parseInt(rechargeValue));
	
	}

	private String incrSeq() {
		long seqInt = Long.parseLong(lastSeq);
		++seqInt;
		lastSeq=seqInt+"";
		return lastSeq;
	}	

	// Entry point of application
	public static void main(String[] a) throws IOException{
		new Initiator();
	}

}

// Representative Subscriber class
class Subscriber {


	String dn;
	String imsi;
	String iccid;
	Product product;
	String acctType;
	String balance;

	// Public constructor
	public Subscriber(String dn, String imsi, String iccid, Product product, String acctType, String balance) {
		this.dn=dn;
		this.imsi=imsi;
		this.iccid=iccid;
		this.product=product;
		this.acctType=acctType;
		this.balance=balance;
	}

	
	// Getter functions start ... no setter functions defined.  All property setting should be achieved via the Constructor
	public String getDN() {
		return dn;
	}

	public String getIMSI() {
		return imsi;
	}

	public String getICCID() {
		return iccid;
	}

	public Product getProduct() {
		if (null==product) System.out.println("All don Scatter!");
		return product;
	
	}
	
	public String getAcctType() {
		return acctType;
	}

	public String getBalance() {
		return balance;	
	}
	public void setBalance(int newBalance) {
		balance = new String(newBalance+"");
	}
}



// Representative Subscriber class
class Product {

	String prodID;
	String onnetRate;
	String offnetRate;

	
	public Product(String prodID, String onnetRate, String offnetRate) {
		this.prodID=prodID;
		this.onnetRate=onnetRate;
		this.offnetRate=offnetRate;
	}
	
	
	// Getter functions start ... no setter functions defined.  All property setting should be achieved via the Constructor
	public String getProdID() {
		return prodID;

	}

	public String getOnnetRate() {
		return onnetRate;
	}

	public String getOffnetRate() {
		return offnetRate;
	}

}


