import java.awt.Image;


class Alien extends BaseShape{
	
	public Alien(int x, int y, int w, int h, int s, int xd, Image a){
		super(x, y, w, h, s, a);
		setDir(xd, 0);
	}
}