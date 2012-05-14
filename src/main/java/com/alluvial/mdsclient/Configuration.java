package com.alluvial.mdsclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.alluvial.mds.common.MDSHelper;

/**
 * This class is used to access MDSClient configuration parameters.
 * @author erepekto
 */
public class Configuration {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 146 $");

	private Configuration() {}

	private static Properties prop = null;

	private static String CONFIG_FILENAME = "mds_client.config";

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

	static String getMdsLiveHost() throws IllegalArgumentException {
		try {
			String result = instance().getProperty("mds_live_host");
			return result==null?"":result;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'mds_host'", ex);
		}
	}

	static int getMdsLivePort() throws IllegalArgumentException {
		try {
			return Integer.parseInt(instance().getProperty("mds_live_port"));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'mds_port'", ex);
		}
	}

	static String getMdsReplayHost() throws IllegalArgumentException {
		try {
			String result = instance().getProperty("mds_replay_host");
			return result==null?"":result;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'mds_host'", ex);
		}
	}

	static int getMdsReplayPort() throws IllegalArgumentException {
		try {
			return Integer.parseInt(instance().getProperty("mds_replay_port"));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("error occurred while reading configration parameter 'mds_port'", ex);
		}
	}
}
