package com.alluvial.mlink.iress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.alluvial.mds.common.MDSHelper;


/**
 * This class is responsible for dumping data to dump file.
 * @author erepekto
 */
public class DataDumper {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 152 $");

	private FileChannel dumpChannel = null;
	private String IRESSChannelName;
	private String dumpDirectory = "dumps";
	private File dumpDir;
	
	public DataDumper(String IRESSChannelName, String dumpDirectory) {
		this.IRESSChannelName = IRESSChannelName;
		this.dumpDirectory = dumpDirectory;
	}

	/**
	 * Created dump directory if one doesn't exist and creates dump file with order number.
	 */
	void createFile() {
		try {
			createDirIfNotExists();
			
			// generate date part of name
	        Date dateNow = new Date ();
	        SimpleDateFormat dateformatYYYYMMDD = new SimpleDateFormat("yyyyMMdd");
	        String nowYYYYMMDD = new StringBuilder( dateformatYYYYMMDD.format( dateNow ) ).toString();

	        // get postfix
	        String nextPostifx = getNamePostfix(nowYYYYMMDD);
	        
	        // then create file
			dumpChannel = new FileOutputStream(dumpDirectory + File.separator + nowYYYYMMDD + "." + nextPostifx + "_" + IRESSChannelName + ".dat").getChannel();
			//dumpChannel.force(true);
		} catch (FileNotFoundException e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: file not found error occurred " + e);
		}
	}

	private String getNamePostfix(final String nowYYYYMMDD) {
		//final String fileName = nowYYYYMMDD + "_" + IRESSChannelName;
		
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.startsWith(nowYYYYMMDD) && name.endsWith(IRESSChannelName + ".dat");
		    }
		};
		
		String[] list = dumpDir.list(filter);
		
		String nextIndex;
		if (list.length!=0) {
			Arrays.sort(list);
			int last = list.length - 1;
			int dotPos = list[last].indexOf('.');
			int undescorePos = list[last].indexOf('_');
			String indexStr = list[last].substring(dotPos + 1, undescorePos);
			
			try {
				Integer index = Integer.parseInt(indexStr) + 1;
				nextIndex = index.toString();
			}
			catch (NumberFormatException ex) {
				Integer count = list.length;
				nextIndex = count.toString();
			}
		}
		else {
			Integer count = 1;
			nextIndex = count.toString();
		}
		
		return nextIndex;
	}

	private void createDirIfNotExists() {
		this.dumpDir = new File(dumpDirectory);
		
		if (!dumpDir.exists())
			dumpDir.mkdir();
	}

	void closeFile() {
		try {
			if (dumpChannel!=null)
				dumpChannel.close();
		} catch (FileNotFoundException e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: file not found error occurred " + e);
		}
		catch (IOException e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: I/O error occured while opening dump file " + e);
		}
	}
	
	/**
	 * Dumps the buffer from position to limit to dump file.
	 * @param inputBufer
	 * @param length
	 */
	void dump(ByteBuffer inputBufer) {
		if (dumpChannel != null) {
			try {
				int limitBeforeFlip = inputBufer.limit();
				inputBufer.flip();
				dumpChannel.write(inputBufer);
				inputBufer.limit(limitBeforeFlip);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Returns list of dump files.
	 * @param replayDate
	 * @return
	 */
	public String[] getDumpFilesForDate(final String replayDate) {
		this.dumpDir = new File(dumpDirectory);
		
		if (!dumpDir.exists())
			return new String[0];
		
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.startsWith(replayDate) && name.endsWith(IRESSChannelName + ".dat");
		    }
		};
		
		String[] list = dumpDir.list(filter);
		Arrays.sort(list);
		
		return list; 
	}
}
