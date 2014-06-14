package brett824.isaacocr;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;


public class ImageSaver extends Thread
{

	JFrame f;
	
	public ImageSaver(JFrame f){
		
		this.f = f;
		
	}
	
	public void run(){
		
		BufferedImage img = new BufferedImage(f.getContentPane().getWidth(),f.getContentPane().getHeight(),BufferedImage.TYPE_INT_RGB);
		
    	Graphics g = img.getGraphics();
    	f.getContentPane().printAll(g);
    	
    	try {
    		FTPConfig config = FTPConfig.getConfig();
			ImageIO.write(img,"png", new File("items.png"));
			FTPClient ftp = new FTPClient();
			ftp.connect(config.getServer());
			System.out.println(config.getServer());
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				System.out.println(reply + " err");
				ftp.disconnect();
			}
			//TODO: check those booleans
			boolean login = ftp.login(config.getUserName(), config.getPassword());
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			InputStream input = new FileInputStream(new File("items.png"));
			boolean stored = ftp.storeFile(config.getDirectory() + "items.png", input);
			ftp.logout();
			ftp.disconnect();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
