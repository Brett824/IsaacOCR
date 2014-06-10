package brett824.isaacocr;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ItemLabel extends JLabel {
	
	Item item;
	
	public ItemLabel(Item item){
		
		this.item = item;
		
		BufferedImage in = null;
		try {
			//System.out.println(item.img);
			in = ImageIO.read(new File(item.img));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	this.setIcon(new ImageIcon(in));
    	
	}

	public void setImage(String newimg) {

		item.img = newimg;
		
		BufferedImage in = null;
		try {
			in = ImageIO.read(new File(item.img));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	this.setIcon(new ImageIcon(in));
		
	}

}
