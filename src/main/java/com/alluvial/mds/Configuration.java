package com.alluvial.mds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.alluvial.mds.common.MDSHelper;

/**
 * This class is used to access MDS configuration parameters.
 * @author erepekto
 */
public class Configuration {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");
	
	private Configuration() {}
	
	private static Properties prop = null;
	
	private static String CONFIG_FILENAME = "app.config";
	
	static Properties instance() throws IllegalArgumentException {
		if (prop==null)	{
		    InputStream is;
			
		    try {
				is = new FileInputStream(System.getProperty("user.dir") + File.separator + CONFIG_FILENAME);
				prop = new Properties();
			    prop.load(is);
			    is.close();
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("could not find configuration file " + CONFIG_FILENAME, e);
			} catch (IOException e) {
				throw new IllegalArgumentException("error occurred wile reading configuration file " + CONFIG_FILENAME, e);
			}
		}

		return prop;
	}

	static String getHost() throws IllegalArgumentException {
		try {
			String result = instance().getProperty("host");
			return result==null?"":result;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'host'", ex);
		}
	}

	static int getQuotePort() throws IllegalArgumentException {
		try {
			return Integer.parseInt(instance().getProperty("port_quote"));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'port_quote'", ex);
		}
	}

	static int getDepthPort() throws IllegalArgumentException {
		try {
			return Integer.parseInt(instance().getProperty("port_depth"));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'port_depth'", ex);
		}
	}

	static int getTradePort() throws IllegalArgumentException {
		try {
			return Integer.parseInt(instance().getProperty("port_trade"));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'port_trade'", ex);
		}
	}

	static String getUsername() throws IllegalArgumentException {
		try {
			return instance().getProperty("username");
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'username'", ex);
		}
	}

	static String getPassword() throws IllegalArgumentException {
		try {
			String result = instance().getProperty("password");
			return result==null?"":result;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'password'", ex);
		}
	}

	static String getExchange() throws IllegalArgumentException {
		try {
			String result = instance().getProperty("exchange");
			return result==null?"":result;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'exchange'", ex);
		}
	}
	
	static String getDataSource() throws IllegalArgumentException {
		try {
			String result = instance().getProperty("data_source");
			return result==null?"":result;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'data_source'", ex);
		}
	}
	
	static String getDataSourceBoard() throws IllegalArgumentException {
		try {
			String result = instance().getProperty("data_source_board");
			return result==null?"":result;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred whule reading configration parameter 'data_source_board'", ex);
		}
	}

	public static int getMDSPort() throws IllegalArgumentException {
		try {
			return Integer.parseInt(instance().getProperty("mds.port"));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'mds.port'", ex);
		}
	}

	public static boolean getEmulationMode() {
		try {
			return Boolean.parseBoolean(instance().getProperty("emulation_mode"));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'emulation_mode'", ex);
		}
	}
	
	public static boolean getDumpData() {
		try {
			return Boolean.parseBoolean(instance().getProperty("dump_data"));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'dump_data'", ex);
		}
	}
	
	public static String getDumpDirectory() {
		try {
			return instance().getProperty("dump_directory");
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'dump_data'", ex);
		}
	}
}
