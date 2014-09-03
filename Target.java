import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class Target {
	
	private int xpos1, ypos1, xpos2, ypos2;
	private Line2D.Double line;
	
	public Target(int x1, int y1, int x2, int y2){
		
		xpos1 = x1;
		ypos1 = y1;
		xpos2 = x2;
		ypos2 = y2;
		
		line = new Line2D.Double(xpos1, ypos1, xpos2, ypos2);
	}
	
	public void move(){
				
		line.setLine(xpos1, ypos1,  xpos2,  ypos2);
	}
	
	public boolean intersects(int x, int y, int width, int height){
		
		return line.intersects(x, y, width, height);
	}
	
	public boolean intersects(Rectangle2D.Double r){
		
		return line.intersects(r);
	}
	
	public void setPos1(int x1, int y1){
		
		xpos1 = x1;
		ypos1 = y1;
	}
	
	public void setPos2(int x2, int y2){
		
		xpos2 = x2;
		ypos2 = y2;
	}
	
	public void setPos(int x1, int y1, int x2, int y2){
		
		xpos1 = x1;
		ypos1 = y1;
		xpos2 = x2;
		ypos2 = y2;
	}
	
	public int getX1() { return xpos1; }
	public int getY1() { return ypos1; }
	public int getX2() { return xpos2; }
	public int getY2() { return ypos2; }
	
	public void paint(Graphics2D g){
		
		g.fill(line);
		g.draw(line);
	}
}