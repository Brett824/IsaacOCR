package brett824.isaacocr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FTPConfig {

	private String server;
	private String dir;
	private String user;
	private String pass;
	
	
	public FTPConfig(String server, String dir, String user, String pass){
		
		this.server = server;
		this.dir = dir;
		this.user = user;
		this.pass = pass;
		
	}
	
	public static FTPConfig LoadConfig() throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(new File("ftpconfig.txt")));
		//first four lines, in order, are what we're using
		//should probably use YAML
		String server = br.readLine();
		String dir = br.readLine();
		String user = br.readLine();
		String pass = br.readLine();		
		
		br.close();
		
		return new FTPConfig(server, dir, user, pass);
		
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
