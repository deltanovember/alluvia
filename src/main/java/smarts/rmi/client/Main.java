package smarts.rmi.client;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DateFormat dfm = new SimpleDateFormat("dd/MM/yyyy");
			Date startDate;
            System.out.println(new Date());
			startDate = dfm.parse("01/03/2011");

			Date endDate = dfm.parse("01/03/2011");
            System.out.println(new Date());
			String market = "asx_mq";

			FavReader fr = new FavReader();
			fr.run(startDate, endDate);
			//fr.test_run(startDate, endDate);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
