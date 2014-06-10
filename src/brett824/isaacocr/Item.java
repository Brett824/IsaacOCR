package brett824.isaacocr;

public class Item {

	String name;
	String type;
	String img;
	
	public Item(String name, String type, String img){
		
		this.name = name;
		this.type = type;
		if (img == null){
			this.img = "";
		}else{
			this.img = "img/" + img;
		}
		
	}
	
}
