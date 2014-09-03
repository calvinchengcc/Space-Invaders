import java.awt.Image;

public class Bullet extends BaseShape {	
	
	public Bullet(int w, int h, int s, Image img){
		super(Integer.MIN_VALUE, Integer.MIN_VALUE, w, h, s, img);
	}
	
	public void fire(int x, int y, int ydir){
		setPos(x, y);
		setDir(super.getXDir(), ydir);
	}
}