import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;


public abstract class BaseShape {
	
	private int xpos, ypos, height, width, speed, xdir, ydir;
	private Rectangle2D.Double c;
	private Image img;
	
	public BaseShape(int x, int y, int w, int h, int s, Image a){
		
		xpos = x;
		ypos = y;
		width = w;
		height = h;
		speed = s;
		xdir = 0;
		ydir = 0;
		img = a;
		c = new Rectangle2D.Double (xpos, ypos,  width,  height);		
	}
	
	public void move(){
		xpos = xpos + speed*xdir;
		ypos = ypos + speed*ydir;
		c.setFrame(xpos, ypos,  width,  height);
	}
	
	public int getX() { return xpos; }
	public int getY() { return ypos; }
	public int getHeight() { return height; }
	public int getWidth() { return width; }
	public int getXDir() { return xdir; }
	public int getYDir() { return ydir; }
	public int getSpeed() { return speed; }
	public Image getImage() { return img; }
	public Rectangle2D.Double getRectangle2D() { return c; }
	
	public void setDir(int x, int y){
	
		xdir = x;
		ydir = y;
	}
	
	public void setSize(int w, int h){
		width = w;
		height = h;
	}
	
	public void setPos(int x, int y){
	
		xpos = x;
		ypos = y;
	}
	
	public void setImage(Image i){
		
		img = i;
	}
	
	public void setSpeed(int speed){
		
		this.speed = speed;
	}
	
	public boolean intersects(int x, int y, int w, int h){
	
		return c.intersects(x, y, w, h);
	}
	
	public void paint(Graphics2D g, Container a){
		g.fill(c);
		g.drawImage(img, xpos, ypos, width, height, a);
	}
}