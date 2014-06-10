package brett824.isaacocr;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import net.sourceforge.tess4j.*;
import uk.ac.shef.wit.simmetrics.similaritymetrics.*;

public class OCR {

	static ArrayList<Item> items = readItemList();
	static ArrayList<ItemLabel> stack = new ArrayList<ItemLabel>();
	static Item trinket = new Item("","","");
	static int coins = 0;
	static int guppycount;
	
    public static void main(String[] args) throws AWTException, IOException, InterruptedException {
    	
    	//todo: move to setup method?
        Tesseract instance = Tesseract.getInstance();  // JNA Interface Mapping
        instance.setLanguage("isaac");
        instance.setPageSegMode(7);
    	
        //set up a whole mess of GUI garbage
    	JFrame.setDefaultLookAndFeelDecorated(true);
    	JFrame previewframe = new JFrame();
    	previewframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    	previewframe.setVisible(true);
    	Dimension thing = new Dimension(30,30);
    	Button b = new Button("Toggle");
		JTextField x = new JTextField();
		x.setPreferredSize(thing);
		x.setText("450");
		JTextField y = new JTextField();
		y.setPreferredSize(thing);
		y.setText("330");
		JTextField w = new JTextField();
		w.setPreferredSize(thing);
		w.setText("500");
		JTextField h = new JTextField();
		h.setPreferredSize(thing);
		h.setText("30");

		JFrame.setDefaultLookAndFeelDecorated(true);
        final JFrame captureframe = new JFrame("Capture");
        captureframe.setAlwaysOnTop(true);
        JPanel c = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(Color.gray);
                int w = getWidth();
                int h = getHeight();
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, w,h);
            }
        };
        c.setPreferredSize(new Dimension(300, 300));
        captureframe.getContentPane().add(c);
        captureframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        captureframe.pack();
        captureframe.setVisible(true);
        com.sun.awt.AWTUtilities.setWindowOpaque(captureframe,false);
    	
		final JFrame showitems = new JFrame();
		showitems.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    	showitems.setVisible(true);
    	showitems.setPreferredSize(new Dimension(1000,110));
    	showitems.setAlwaysOnTop(true);
    	Container contentP = showitems.getContentPane();
    	contentP.setLayout(new BorderLayout());
    	final JPanel itempane = new JPanel();
    	itempane.setLayout(new FlowLayout(FlowLayout.LEFT));
    	final JLabel damagelabel = new JLabel(" Damage: " + calculateDamage());
    	contentP.add(itempane, BorderLayout.CENTER);
    	contentP.add(damagelabel, BorderLayout.PAGE_START);
    	showitems.pack();
		
    	b.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {

				if(captureframe.isVisible()) captureframe.setVisible(false); else captureframe.setVisible(true);
				
			}
    		
    	});
    	
    	previewframe.add(b);
    	previewframe.add(x);
    	previewframe.add(y);
    	previewframe.add(w);
    	previewframe.add(h);
		Container contentPane = previewframe.getContentPane();
		contentPane.setLayout(new FlowLayout());
		previewframe.pack();
		
		JPanel pane = new JPanel();
    	
    	for(;;){
    		
    		//remove last update's preview
    		previewframe.remove(pane);
    		
    		//adjust capture to the size of capture preview
    		if(captureframe.isVisible()){
	    		Point p = c.getLocationOnScreen();
	    		Dimension d = c.getSize();
	    		x.setText(p.x + "");
	    		y.setText(p.y + "");
	    		w.setText(d.width + "");
	    		h.setText(d.height + "");
    		}

    		Rectangle screenRect = new Rectangle(Integer.parseInt(x.getText()),Integer.parseInt(y.getText()),Integer.parseInt(w.getText()),Integer.parseInt(h.getText()));
    		
    		final BufferedImage capture = new Robot().createScreenCapture(screenRect);
	    	
    		//ImageIO.write(capture, "bmp", new File("test.bmp"));
	    	
    		double percent = getBlackPercent(capture);
    		
    		if(percent < .30) {
    			previewframe.pack(); //do this to remove extra space if there was an item image before
    			continue; //dont bother doing anything if theres not an item acquired
    		}
    		
    		Pair sides = findTextEdge(capture); //look for left and right edges of text
    			    		
    		
    		final BufferedImage capture2 = capture.getSubimage(sides.left, 0, capture.getWidth()-sides.left-sides.right, capture.getHeight()); //crop
	    		
    		if (capture2.getWidth() < 20) continue; //something too small is an error probably?
    		
	    	pane = new JPanel() {
	    		
	            @Override
	            protected void paintComponent(Graphics g) {
	                super.paintComponent(g);
	                g.drawImage(capture2, 0, 0, null);
	                repaint();
	            }
	    	
	    	};
	    	
	    	pane.setPreferredSize(new Dimension(capture2.getWidth(),capture2.getHeight()));
	    	
	    	previewframe.add(pane);
	    	previewframe.pack();
	    	

	        try {
	            String result = instance.doOCR(capture2);
	            if(result.trim().equals("..")) result = ""; //nip a weird error in the bud
	            Item guess = findClosestMatch(result.trim());

	            if(result.trim().length() > 1) {
	            	
		            if(!guess.img.equals("") && (stack.size() == 0 || !stack.get(0).item.name.equals(guess.name))){ //stop immediate repeats
		            	
		            	//ImageIO.write(capture2, "bmp", new File("testdata/" + guess.name + "#" + result.trim() + "#" + new Date().getTime() + ".bmp"));
		            	
		            	if((guess.name.equals("GUPPYS PAW") && haveItem("GUPPYS PAW")) ||
		            		(guess.name.equals("GUPPYS HEAD") && haveItem("GUPPYS HEAD"))) continue; //dont place guppys paw or head twice because theyre held items.
		            	
		            	ItemLabel label = new ItemLabel(guess);
		            	
		            	//todo: do something about thim ess
		            	label.addMouseListener(new MouseListener(){

							@Override
							public void mouseClicked(MouseEvent arg0) {

								//utility on left click
								if(arg0.getButton() == MouseEvent.BUTTON1){
									
									//toggle thin/thick odd mushroom
									if(((ItemLabel)arg0.getSource()).item.name.equals("ODD MUSHROOM")){
										
										if(((ItemLabel)arg0.getSource()).item.img.equals("img/120_odd_mushroom_thin.png")) {
											((ItemLabel)arg0.getSource()).setImage("img/121_odd_mushroom_thick.png");
										}else{
											((ItemLabel)arg0.getSource()).setImage("img/120_odd_mushroom_thin.png");
										}
										
										damagelabel.setText(" Damage: " + calculateDamage());
										ImageSaver is = new ImageSaver(showitems);
										is.start();

										
									}
									
									//coins for money = power
									if(((ItemLabel)arg0.getSource()).item.name.equals("MONEY = POWER")){
										
										String input =  JOptionPane.showInputDialog("Coins?", "0");
										try {
											coins = Integer.parseInt(input);
										}catch(Exception ex){
											System.out.println("coin input not integer");
										}
										
										damagelabel.setText(" Damage: " + calculateDamage());
										ImageSaver is = new ImageSaver(showitems);
										is.start();


									}
									
								}
								
								//remove on right click
								if(arg0.getButton() == MouseEvent.BUTTON3){
									ItemLabel temp = (ItemLabel) arg0.getSource();
									itempane.remove((Component) arg0.getSource());
					            	if(temp.item.name.equals("GUPPYS PAW") ||
						            		temp.item.name.equals("GUPPYS HEAD") ||
						            		temp.item.name.equals("GUPPYS TAIL") ||
						            		temp.item.name.equals("DEAD CAT")){ 

						            		guppycount--;
						            		
						            }
									stack.remove(arg0.getSource());
									//printStack();
									damagelabel.setText(" Damage: " + calculateDamage());
									ImageSaver is = new ImageSaver(showitems);
									is.start();

									showitems.pack();
									itempane.repaint();
								}
								
							}

							public void mouseEntered(MouseEvent arg0) {}
							public void mouseExited(MouseEvent arg0) {}
							public void mousePressed(MouseEvent arg0) {}
							public void mouseReleased(MouseEvent arg0) {}
		            		
		            	});
		            	
		            	itempane.add(label);
		            	showitems.pack();
		            	itempane.repaint();
		            	
		            	stack.add(0,label);
		            	
		            	damagelabel.setText(" Damage: " + calculateDamage()); 

						ImageSaver is = new ImageSaver(showitems);
						is.start();
		            	//saveImage(showitems);
		            	
		            	//printStack();
		            	
		            	if(guess.name.equals("GUPPYS PAW") ||
		            		guess.name.equals("GUPPYS HEAD") ||
		            		guess.name.equals("GUPPYS TAIL") ||
		            		guess.name.equals("DEAD CAT")){ 

		            		guppycount++;
		            		
		            		if(guppycount == 3){
			            		
			            		ItemLabel guppy = new ItemLabel(new Item("GUPPY", "PASSIVE", "guppy.png"));
			            		guppy.addMouseListener(new MouseListener(){

									@Override
									public void mouseClicked(MouseEvent arg0) {

										//remove on right click
										if(arg0.getButton() == MouseEvent.BUTTON3){
											itempane.remove((Component) arg0.getSource());
											stack.remove(arg0.getSource());
											//printStack();
											damagelabel.setText(" Damage: " + calculateDamage());
											ImageSaver is = new ImageSaver(showitems);
											is.start();
											showitems.pack();
											itempane.repaint();
										}
										
									}

									public void mouseEntered(MouseEvent arg0) {}
									public void mouseExited(MouseEvent arg0) {}
									public void mousePressed(MouseEvent arg0) {}
									public void mouseReleased(MouseEvent arg0) {}
				            		
				            	});
			            		itempane.add(guppy);
				            	showitems.pack();
				            	itempane.repaint();
				            	stack.add(1,guppy);
				            	ImageSaver is2 = new ImageSaver(showitems);
								is2.start();
			            	
			            	}
		            		
		            	}
		            	    		
		            	
		            }else if (guess.type.equals("trinket") && !trinket.name.equals(guess.name)){
		            	
		            	trinket = guess;
		            	System.out.println("trinket is now " + guess.name);
		            	damagelabel.setText(" Damage: " + calculateDamage()); ;
						ImageSaver is = new ImageSaver(showitems);
						is.start();
		            	//saveImage(showitems);
		            	
		            }
		            
	            	System.out.println("OCR: " + result.trim() + " => " + guess.name + " (" + guess.type + ")");
	           
	            }
	            
	        } catch (TesseractException e) {
	            System.err.println(e.getMessage());
	        }
	        
	        Thread.sleep(500); //update every half a second
	        
    	}
    }

    //get the % of pixels in the image that are black (to a certain degree)
	private static double getBlackPercent(BufferedImage capture) {

		int blackcount = 0;
		for(int i = 0; i < capture.getWidth(); i++){
			for(int j = 0; j < capture.getHeight(); j++){
				int rgb = capture.getRGB(i, j);
				float[] hsb = Color.RGBtoHSB((rgb >> 16 & 0xFF), (rgb >> 8 & 0xFF), (rgb & 0xFF), null);
				if(hsb[2] < .05) blackcount++;
			}
		}
		
		return  (double) blackcount / (double) (capture.getWidth() * capture.getHeight());
		
		
	}

	//used for loading the current item list file
	private static ArrayList<Item> readItemList() {
		
		ArrayList<Item> ret = new ArrayList<Item>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("isaacitemlist.txt")));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				ret.add(new Item(line.split(",")[0], "passive", line.split(",")[1]));
			}
		 
			br.close();
			
			br = new BufferedReader(new FileReader(new File("isaactrinketlist.txt")));
			
			line = null;
			while ((line = br.readLine()) != null) {
				ret.add(new Item(line.split(",")[0], "trinket", null));
			}
		 
			br.close();
			
			br = new BufferedReader(new FileReader(new File("isaacuselist.txt")));
			
			line = null;
			while ((line = br.readLine()) != null) {
				ret.add(new Item(line.split(",")[0], "use", null));
			}
		 
			br.close();
			
			br = new BufferedReader(new FileReader(new File("isaacconsumelist.txt")));
			
			line = null;
			while ((line = br.readLine()) != null) {
				ret.add(new Item(line.split(",")[0], "consume", null));
			}
		 
			br.close();
			
			br = new BufferedReader(new FileReader(new File("isaacconditionlist.txt")));
			
			line = null;
			while ((line = br.readLine()) != null) {
				ret.add(new Item(line.split(",")[0], "condition", null));
			}
		 
			br.close();
			
			
		} catch (Exception ex){
			
			
		}
		
		return ret;
		
	}
	
	//find the edge of the text to isolate it within the image
	//go from the left and from the right until you find white enough pixels
	private static Pair findTextEdge(BufferedImage capture){
		
		int left = 0;
		int right = 0;
		int mid = capture.getHeight()/2;
		
		for(int i = 0; i < capture.getWidth(); i++){ //from left to right
			
			int rgb = capture.getRGB(i, mid);
			float[] hsb = Color.RGBtoHSB((rgb >> 16 & 0xFF), (rgb >> 8 & 0xFF), (rgb & 0xFF), null);
			if(hsb[2] > .80 && hsb[1] < .20) { //look for white pixels
				left = i;
				break;
			}
			
		}
		
		left -= 10;
		
		for(int i = capture.getWidth() - 1; i >= 0; i--){ //right to left
			
			int rgb = capture.getRGB(i, mid);
			float[] hsb = Color.RGBtoHSB((rgb >> 16 & 0xFF), (rgb >> 8 & 0xFF), (rgb & 0xFF), null);
			if(hsb[2] > .80 && hsb[1] < .20) {
				right = i;
				break;
			}
			
		}
		
		right = capture.getWidth() - right;
		
		right -= 10;
		
		//idk lazy way to prevent errors
		if(left < 0) left = 0;  
		if(right < 0) right = 0;
			
		return new Pair(left, right);
		
	}
    
	//get the closest match for the OCR input in the list of item strings
    private static Item findClosestMatch(String input){
    	
		Levenshtein compare = new Levenshtein();
		
		float max = 0.0f;
		float comp;
		Item maxitm = null;
		for(Item item : items){
			
			comp = compare.getSimilarity(input, item.name);
			
			if(comp > max){
				
				max = comp;
				maxitm = item;
				
			}
			
		}
		
		return maxitm;
		
    	
    }
    
    //debug output, print the current items
    private static void printStack(){
    	
    	for(ItemLabel item : stack){
    		
    		System.out.print(item.item.name + " ");
    		
    	}
    	
    	System.out.println();
    	
    }
    
    //damage calculation (adopted mostly from the game's source)
    //fix this up, not sure if it's accurate
    private static double calculateDamage(){
    	
    	double v2 = 0.0;
    	
        if (haveItem("BLOOD OF THE MARTYR")) {
          ++v2;
        }
        if (haveItem("THE SMALL ROCK")) {
          ++v2;
        }   
        if (haveItem("MONEY = POWER")) { //money. wait.
          v2 += coins * 0.04; 
        }
        
        if(haveItem("WHORE OF BABYLON")){
        	v2 += 1.5;
        }
        	
    	if (haveItem("STEVEN")) v2++;
    	if (haveItem("PENTAGRAM")) v2++;
    	if (haveItem("GROWTH HORMONES")) v2++;
    	if (haveItem("THE MARK")) v2++;
    	
    	if (haveItem("THE PACT")) v2 += 0.5;
    	if (haveItem("MAX'S HEAD")) v2 += 0.5;
    	if (haveItem("JESUS JUICE")) v2 += 0.5;
    	
    	if (haveItem("MAGIC MUSHROOM"))  v2 += 0.3;
    	if (haveItem("THE HALO")) v2 += 0.3;
    	//if (haveItem("ODD MUSHROOM (THICK)")) v2 += 0.3; //for later
    	if (haveItem("ODD MUSHROOM")) { //thick odd mushroom (could do this with thin odd mush for same semantic meaning but for purity preserve order in original isaac source)
            
        	Item odd = null;
        	
        	//gotta find the item to check which it is (inefficient, i guess, but how many items will you have?)
    		for(ItemLabel item : stack){
    			
    			if(item.item.name.equals("ODD MUSHROOM")) {
    				odd = item.item;
    				break;
    			}
    			
    		}
    		
    		if(odd.img.equals("img/121_odd_mushroom_thick.png")){
    		
    			v2 += 0.3;
	        	
    		}
        }
    	if (haveItem("STIGMATA")) v2 += 0.3;
    	if (haveItem("SMB SUPER FAN!")) v2 += 0.3;
    	if (haveItem("MEAT!")) v2 += 0.3;        

        if (haveItem("ODD MUSHROOM")) { //thin odd mushroom
        
        	Item odd = null;
        	
        	//gotta find the item to check which it is (inefficient, i guess, but how many items will you have?)
    		for(ItemLabel item : stack){
    			
    			if(item.item.name.equals("ODD MUSHROOM")) {
    				odd = item.item;
    				break;
    			}
    			
    		}
    		
    		if(odd.img.equals("img/120_odd_mushroom_thin.png")){
    		
	        	v2 *= 0.9;
	        	v2 -= 0.4;
	        	
    		}
        }


    	v2 = 3.5 * Math.sqrt(1 + v2 * 1.2);
    	
    	if (haveItem("POLYPHEMUS"))
    	{
    		v2 += 4;
            v2 *= 2;
    	}
    	
    	double v3 = 0;
    	
    	v2 *= 1 + v3;
    	
    	if (trinket.name.equals("CURVED HORN")) v2 += 2; //curved horn, later
    	
    	if (haveItem("SACRED HEART"))
    	{
    		v2 *= 2.3;
            v2++;
    	}
    	
    	if (haveItem("TECHNOLOGY 2")) v2 *= 0.65;
    	
    	if (haveItem("MAX'S HEAD") || haveItem("MAGIC MUSHROOM"))
    		v2 *= 1.5;
    		
    	
    	return v2;
    	
    }
    
    //upload an image of the current items to a server
    //moved to ImageSaver class to run in a new thread
    private static void saveImage(JFrame f){
    	
    	BufferedImage img = new BufferedImage(f.getContentPane().getWidth(),f.getContentPane().getHeight(),BufferedImage.TYPE_INT_RGB);
		
    	Graphics g = img.getGraphics();
    	f.getContentPane().printAll(g);
    	
    	try {
    		FTPConfig config = FTPConfig.LoadConfig();
    		System.out.println(config);
			ImageIO.write(img,"png", new File("items.png"));
			FTPClient ftp = new FTPClient();
			ftp.connect(config.getServer());
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				System.out.println("nope");
				//throw new Exception("Exception in connecting to FTP Server");
			}
			ftp.login(config.getUserName(), config.getPassword());
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			InputStream input = new FileInputStream(new File("items.png"));
			ftp.storeFile(config.getDirectory() + "items.png", input);
			ftp.logout();
			ftp.disconnect();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

	private static boolean haveItem(String string) {
		
		for(ItemLabel item : stack){
			
			if(item.item.name.equals(string)) return true;
			
		}
		
		return false;
		
	}
	
	//private static 
    
}