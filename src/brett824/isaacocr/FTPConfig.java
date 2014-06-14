package brett824.isaacocr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FTPConfig {

	private String server;
	private String dir;
	private String user;
	private String pass;
	private static FTPConfig instance = null;
	
	
	protected FTPConfig(String server, String dir, String user, String pass){
		
		this.server = server;
		this.dir = dir;
		this.user = user;
		this.pass = pass;
		
	}
	
	public static FTPConfig LoadConfig() {
		
		BufferedReader br;
		String server, dir, user, pass = null;
		try {
			br = new BufferedReader(new FileReader(new File("ftpconfig.txt")));
			//first four lines, in order, are what we're using
			//should probably use YAML
			server = br.readLine();
			dir = br.readLine();
			user = br.readLine();
			pass = br.readLine();		
			
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return new FTPConfig(server, dir, user, pass);
		
	}
	
	public static FTPConfig getConfig(){
		
		if(instance == null){
			instance = LoadConfig();
		}
		
		return instance;
		
	}
	
	public String getServer(){
		return server;
	}
	
	public String getDirectory(){
		return dir;
	}
	
	public String getUserName(){
		return user;
	}
	
	public String getPassword(){
		return pass;
	}
	
	public String toString(){
		return user + "@" + server + dir;
	}
	
}
