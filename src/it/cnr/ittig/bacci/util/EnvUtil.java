package it.cnr.ittig.bacci.util;

import java.io.File;

public class EnvUtil {

	public static File getApplicationDataDir(String appName) {
		
		File dataDir = null;		
		String homeDir = System.getProperty("user.home");		
		String osName = System.getProperty("os.name").toLowerCase();		
		
		if(osName.indexOf("win") > -1) {
			//Windows environment
			File appData = new File(homeDir + File.separatorChar +
					"Application Data");
			if(appData.exists()) {
				dataDir = new File(homeDir + File.separatorChar +
					"Application Data" + File.separatorChar + appName);
			} else {
				//Put everything under c:\?
				File cDir = new File("c:" + File.separatorChar);
				if(!cDir.exists()) {
					System.err.println("EnvUtil - Unable to read C:\\ !");
					return null;
				}
				dataDir = new File("c:" + File.separatorChar + appName);
			}
		} else {
			//For Linux and Mac use /home/user/.app ?
			dataDir = new File(homeDir + File.separatorChar + 
					"." + appName);
		}

		return dataDir;
	}
}
