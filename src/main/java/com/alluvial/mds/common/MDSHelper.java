package com.alluvial.mds.common;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TimeZone;

public class MDSHelper {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 151 $");

	// timezone setting withoyt DST offset, because IRESS sends already adjusted time, see TRDPLTFM-54
	static TimeZone timezone10 = TimeZone.getTimeZone("GMT+10");
	static
	{
		TimeZone.setDefault(timezone10);
	}

	private static long maxVersion = -1;
	
	public static long getVersion() {
		if (maxVersion == -1)
			try {
				extractClassVersions();
			} catch (Exception e) {
				throw new IllegalArgumentException("the error occured while retrieving class versions", e);
			}

		return maxVersion;		
	}

	private static void extractClassVersions() throws Exception {
		//String s = MDSHelper.getPackageName(cls);
		ArrayList<Class<?>> classes = MDSHelper.getClasses("com.alluvial");

		for (Class<?> c : classes) {
			if (c.getName().contains("com.alluvial.mds.contract") || c.getName().contains("$"))
				continue;
			
			Field[] fields = c.getDeclaredFields();
			
			boolean versionWasFound = false;
			for (Field f : fields)
				if (Modifier.isStatic(fields[0].getModifiers()) && f.getName().equals("Revision"))
				{
					fields[0].setAccessible(true);
					Long version = f.getLong(c);
					fields[0].setAccessible(false);
					
					maxVersion=maxVersion<version?version:maxVersion;
					
					versionWasFound = true;
					break;
				}
			
			if (!versionWasFound)
				throw new IllegalArgumentException("could not find field Revision in class " + c.getName());
		}
	}
	
    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static ArrayList<Class<?>> getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        
        List<File> dirs = new ArrayList<File>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            try {
				dirs.add(new File(resource.toURI()));
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("couldn't convert '" + resource.getFile() + "'");
			}
        }

        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }

        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
            	Class<?> cls = (Class<?>) Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                classes.add(cls);
            }
        }
        return classes;
    }
    
    public static long svnRevToLong(String svnRev) {
    	svnRev = svnRev.replaceFirst("\\$Rev: ", "");
    	svnRev = svnRev.replaceFirst(" \\$", "");
    	return Long.parseLong(svnRev);
    }
}
