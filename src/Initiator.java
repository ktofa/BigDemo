import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class Initiator {



	Subscriber onnet;
	Subscriber offnet;
	List<Subscriber> onnetList=new ArrayList<Subscriber>();
	List<Subscriber>  offnetList=new ArrayList<Subscriber>();
	HashMap<String,Product> products = new HashMap<String,Product>();


	String confDir="/home/biadmin/bigdemo/ocs/conf/";

	Initiator() throws IOException {
		//Get configured resources
		String onnetConfFile = confDir + "onnet.numbers";
		String offnetConfFile = confDir + "offnet.numbers";
		

		getProductInformation(confDir +"products.list");
		createSubList(getSubInformation(onnetConfFile),0);
		createSubList(getSubInformation(offnetConfFile),1);

	}

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
			products.put(partProd[0], new Product(partProd[0],partProd[1],partProd[2]));
		} 

	}

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
				System.out.println("Adding new Subscriber to the Onnet Sub List... Total Onnet subs: " + onnetList.size());	
			}
		}
		else if (1==subType) {//Offnet Numbers
			for (String partSub : subList) {
                                String[] currentSub = partSub.split(",");
                                String dn=currentSub[0];
                                String imsi=currentSub[1];
                                String iccid=currentSub[2];
				offnetList.add(new Subscriber(dn,imsi,iccid,null,"0","0"));
				System.out.println("Adding new Subscriber to the offnet Sub List... Total offnet subs: " + offnetList.size());

			}			
		}
	}

	private Product getProduct(String prodID) {

		return products.get(prodID);

	}



	public static void main(String[] a) throws IOException{
		new Initiator();
	}

}

class Subscriber {


	String dn;
	String imsi;
	String iccid;
	Product product;
	String acctType;
	String balance;


	public Subscriber(String dn, String imsi, String iccid, Product product, String acctType, String balance) {
		this.dn=dn;
		this.imsi=imsi;
		this.iccid=iccid;
		this.product=product;
		this.acctType=acctType;
		this.balance=balance;
	}

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
		return product;
	}
	
	public String getAcctType() {
		return acctType;
	}

	public String getBalance() {
		return balance;	
	}
}

class Product {

	String prodID;
	String onnetRate;
	String offnetRate;

	public Product(String prodID, String onnetRate, String offnetRate) {
		this.prodID=prodID;
		this.onnetRate=onnetRate;
		this.offnetRate=offnetRate;
	}

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


