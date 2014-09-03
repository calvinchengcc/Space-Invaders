import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class SpaceInvaders extends Panel implements KeyListener{
	
	public static final int WIDTH = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 100);
	public static final int HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 200);;
	
	private static final int START_BULLETS = 60;
	
	private static final long serialVersionUID = 1L;

	private Ship ship;
	
	private Bullet machineGun;
	private Bullet[] alienSlob;
	
	private Alien[] alien1;
	private Alien[] alien2;
	private Alien[] alien3;
	private Alien[] alien4;
	
	private Shield[] shield;
	
	private Target laserSight;
	
	private BufferedImage imageBuffer;
	private Graphics2D  graphicsBuffer;
	
	private ScheduledExecutorService t;
	private ScheduledFuture<?> future;
	private Image alienPic, ShipPicGreen, ShipPicYellow, ShipPicRed, skull, humanBullet, alienBullet;
	
	private int counter;
	private int bulletsLeft = START_BULLETS;
	private int AlienSlobTimeout=0;
	private int lives = 3;
	private int[] shieldLife = {10, 10, 10, 10};
	private int deadAlien = 0;
	private int timeCount = 0;
	private int hitAlien = 0;
	private int randomTime = 200;
	
	private boolean reloading = false;
	private boolean won = false;
	private boolean lost = false;
	private boolean stopped = false;
	
	private boolean cheated = false;
	private boolean GodMode = false;
	private boolean UnlimitedBullets = false;
	private boolean ShieldRegen = false;
	private boolean AlienFreeze = false;
	
	private long startTime;
	private int timeTaken;
	
	public SpaceInvaders() {
		
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setSize(WIDTH, HEIGHT);
		
		//initiate ship
		ShipPicGreen = getImage("Green Ship.png");
		ShipPicYellow = getImage("Yellow Ship.png");
		ShipPicRed = getImage("Red Ship.png");
		ship = new Ship((getWidth()/2)-30, getHeight()-88, 60, 60, 6, ShipPicGreen);
		
		//get images
		alienPic = getImage("alien.png");
		skull = getImage("Skull and Crossbones.png");
		humanBullet = getImage("Player Bullet.png");
		alienBullet = getImage("Alien Bullet.png");
	}
	
	public void init() {
		//double buffering
		imageBuffer = (BufferedImage)createImage(getWidth(), getHeight());
		graphicsBuffer = (Graphics2D) imageBuffer.getGraphics();

		//initiate bullets (for both player and aliens)
		machineGun = new Bullet(8, 26, 20, humanBullet);	//create bullet offscreen, and move it to the ship when only when firing
		alienSlob = new Bullet[20];							//an array of bullets to be used by random aliens
		
		for(int i=0; i<alienSlob.length; i++){
			alienSlob[i] = new Bullet(36, 36, 4, alienBullet);
		}
		
		//initiate aliens
		alien1 = new Alien[10];
		alien2 = new Alien[alien1.length];
		alien3 = new Alien[alien2.length];
		alien4 = new Alien[alien3.length];
		
		int[] rnd = {(int)(Math.random()*3+1), (int)(Math.random()*3+1), (int)(Math.random()*3+1), (int)(Math.random()*3+1)};		//int array of random speeds
		
		for(int i=0; i<alien1.length; i++){
			alien1[i] = new Alien(i*getWidth()/10, 0, 48, 35, rnd[0], 1, alienPic);
			alien2[i] = new Alien(i*getWidth()/10, 80, 48, 35, rnd[1], -1, alienPic);
			alien3[i] = new Alien(i*getWidth()/10, 160, 48, 35, rnd[2], 1, alienPic);
			alien4[i] = new Alien(i*getWidth()/10, 240, 48, 35, rnd[3], -1, alienPic);
		}
		
		//initiate shields
		shield = new Shield[4];
		
		for(int i=0; i<shield.length; i++){
			shield[i] = new Shield((getWidth()/shield.length)*i+100, getHeight() - 150, 200, 25);
		}
		
		//initiate laser sight
		laserSight = new Target(ship.getX()+ship.getWidth()/2, 0, ship.getX()+ship.getWidth()/2, ship.getY());
		
		/***************************************************************************************************************************************************/
		
		startTime = System.currentTimeMillis();
		
		addKeyListener(this); 
		setFocusable(true); 
	
		Runnable gameLoop = new TimerTask() {
			public void run(){

				Graphics g = getGraphics();
				
				//call everything to move
				ship.move();
				machineGun.move();
				laserSight.move();
				for(int i=0; i<alienSlob.length; i++){
					alienSlob[i].move();
				}
				for(int i=0; i<alien1.length; i++){
					alien1[i].move();
					alien2[i].move();
					alien3[i].move();
					alien4[i].move();
				}
				for(int i=0; i<shield.length; i++){
					shield[i].move();
				}
				
				//prevent ship from moving out of boundaries (edges of screen and a set area for up and down movement)
				if(ship.getX()+32 <= 0){
					ship.setPos(ship.getX()+ship.getSpeed(), ship.getY());
				}else if(ship.getX()+28 >= getWidth()){	
					ship.setPos(ship.getX()-ship.getSpeed(), ship.getY());
				}
				if(ship.getY() <= getHeight() - 100){
					ship.setPos(ship.getX(), ship.getY()+ship.getSpeed());
				}else if(ship.getY()+ship.getHeight() >= getHeight()){	
					ship.setPos(ship.getX(), ship.getY()-ship.getSpeed());
				}
				
				//prevent aliens from moving off the screen
				for(int i=0; i<alien1.length; i++){
					if(alien1[i].getX() > getWidth()){
						alien1[i].setPos(0-alien1[i].getWidth(), alien1[i].getY());
					}else if(alien2[i].getX()+alien2[i].getWidth() < 0){
						alien2[i].setPos(getWidth(), alien2[i].getY());
					}else if(alien3[i].getX() > getWidth()){
						alien3[i].setPos(0-alien3[i].getWidth(), alien3[i].getY());
					}else if(alien4[i].getX()+alien4[i].getWidth() < 0){
						alien4[i].setPos(getWidth(), alien4[i].getY());
					}
				}
				
				//if the aliens go off the bottom of the screen, just stop them and put them in a single spot (off the screen, of course)
				for(int i=0; i<alien1.length; i++){
					if(alien1[i].getY() > getHeight()){
						alien1[i].setSpeed(0);
						alien1[i].setPos(0, getHeight());
					}else if(alien2[i].getY() > getHeight()){
						alien2[i].setSpeed(0);
						alien2[i].setPos(0, getHeight());
					}else if(alien3[i].getY() > getHeight()){
						alien3[i].setSpeed(0);
						alien3[i].setPos(0, getHeight());
					}else if(alien4[i].getY() > getHeight()){
						alien4[i].setSpeed(0);
						alien4[i].setPos(0, getHeight());
					}
				}
				
				//target line attributes - make line follow ship, and check if the laser hits any shield/alien
				if(!reloading){
					laserSight.setPos(ship.getX()+ship.getWidth()/2-1, 0, ship.getX()+ship.getWidth()/2-1, ship.getY());
				}else{
					laserSight.setPos(0, 0, 0, 0);
				}
				//if laser hits shield, let shield block it
				for(int i=0; i<shield.length; i++){
					if(laserSight.intersects(shield[i].getRectangle2D())){
						laserSight.setPos(ship.getX()+ship.getWidth()/2, shield[i].getY()+shield[i].getHeight(), ship.getX()+ship.getWidth()/2, ship.getY());
					}
				}
				//if laser hits alien(s), let aliens block it
				for(int i=0; i<alien1.length; i++){
					if(laserSight.intersects(alien1[i].getRectangle2D())){
						laserSight.setPos(ship.getX()+ship.getWidth()/2, alien1[i].getY()+alien1[i].getHeight(), ship.getX()+ship.getWidth()/2, ship.getY());
					}
					if(laserSight.intersects(alien2[i].getRectangle2D())){
						laserSight.setPos(ship.getX()+ship.getWidth()/2, alien2[i].getY()+alien2[i].getHeight(), ship.getX()+ship.getWidth()/2, ship.getY());
					}
					if(laserSight.intersects(alien3[i].getRectangle2D())){
						laserSight.setPos(ship.getX()+ship.getWidth()/2, alien3[i].getY()+alien3[i].getHeight(), ship.getX()+ship.getWidth()/2, ship.getY());
					}
					if(laserSight.intersects(alien4[i].getRectangle2D())){
						laserSight.setPos(ship.getX()+ship.getWidth()/2, alien4[i].getY()+alien4[i].getHeight(), ship.getX()+ship.getWidth()/2, ship.getY());
					}
				}
				
				//code for machine gun bullets (reload/reset, intersects)
				//if bullet missed:
				if(machineGun.getY() <= 0){
					machineGun.setPos(0, 0-machineGun.getHeight());
					reloading = false;
				}
				//if bullet hit an alien:
				for(int i=0; i<alien1.length; i++){
					if(machineGun.intersects(alien1[i].getX(), alien1[i].getY(), alien1[i].getWidth(), alien1[i].getHeight())){
						alien1[i].setSpeed(0);
						alien1[i].setPos(0, getHeight());
						machineGun.setPos(0, 0-machineGun.getHeight());
						reloading = false;
						hitAlien++;
					}else if(machineGun.intersects(alien2[i].getX(), alien2[i].getY(), alien2[i].getWidth(), alien2[i].getHeight())){
						alien2[i].setSpeed(0);
						alien2[i].setPos(0, getHeight());
						machineGun.setPos(0, 0-machineGun.getHeight());
						reloading = false;
						hitAlien++;
					}else if(machineGun.intersects(alien3[i].getX(), alien3[i].getY(), alien3[i].getWidth(), alien3[i].getHeight())){
						alien3[i].setSpeed(0);
						alien3[i].setPos(0, getHeight());
						machineGun.setPos(0, 0-machineGun.getHeight());
						reloading = false;
						hitAlien++;
					}else if(machineGun.intersects(alien4[i].getX(), alien4[i].getY(), alien4[i].getWidth(), alien4[i].getHeight())){
						alien4[i].setSpeed(0);
						alien4[i].setPos(0, getHeight());
						machineGun.setPos(0, 0-machineGun.getHeight());
						reloading = false;
						hitAlien++;
					}
				}
				
				//once in a while, move aliens closer to the ship
				if(counter>5 && !stopped){
					for(int i=0; i<alien1.length; i++){
						alien1[i].setPos(alien1[i].getX(), alien1[i].getY()+1);
						alien2[i].setPos(alien2[i].getX(), alien2[i].getY()+1);
						alien3[i].setPos(alien3[i].getX(), alien3[i].getY()+1);
						alien4[i].setPos(alien4[i].getX(), alien4[i].getY()+1);
					}
					counter=0;
				}
				
				//if the aliens hit the ship, automatic death (game over)
				if(!GodMode){
					for(int i=0; i<alien1.length; i++){
						if(alien1[i].intersects(ship.getX(), ship.getY(), ship.getWidth(), ship.getHeight()) ||
									alien2[i].intersects(ship.getX(), ship.getY(), ship.getWidth(), ship.getHeight()) ||
									alien3[i].intersects(ship.getX(), ship.getY(), ship.getWidth(), ship.getHeight()) ||
									alien4[i].intersects(ship.getX(), ship.getY(), ship.getWidth(), ship.getHeight())){
							lives=0;
						}
					}
				}
				
				//make random aliens shoot at player at random times
				int rnd, tmp;
				int[] randomAlien1 = new int[10];
				int[] randomAlien2 = new int[10];
				
				for(int i=0; i<10; i++){
					randomAlien1[i] = i;
					randomAlien2[i] = i;
				}

				if(AlienSlobTimeout>randomTime && !stopped){
					
					//randomize int array => choose random aliens, making sure none of the bullets are to be shot from the same alien
					//switch each element of both arrays with elements at random indices
					for(int i=0; i<10; i++){
						//first array
						rnd = (int)(Math.random()*10);
						tmp = randomAlien1[i];
						randomAlien1[i] = randomAlien1[rnd];
						randomAlien1[rnd] = tmp;
						//second array
						rnd = (int)(Math.random()*10);
						tmp = randomAlien2[i];
						randomAlien2[i] = randomAlien2[rnd];
						randomAlien2[rnd] = tmp;
					}
					
					//fire the bullets from random aliens
					for(int i=0; i<5; i++){
						alienSlob[i].fire(alien1[randomAlien1[i]].getX()+6, alien1[randomAlien1[i]].getY(), 1);
					}
					for(int i=5; i<10; i++){
						alienSlob[i].fire(alien2[randomAlien1[i]].getX()+6, alien2[randomAlien1[i]].getY(), 1);
					}
					for(int i=10; i<15; i++){
						alienSlob[i].fire(alien3[randomAlien2[i-10]].getX()+6, alien3[randomAlien2[i-10]].getY(), 1);
					}
					for(int i=15; i<20; i++){
						alienSlob[i].fire(alien3[randomAlien2[i-10]].getX()+6, alien4[randomAlien2[i-10]].getY(), 1);
					}

					AlienSlobTimeout=0;
					randomTime = (int)(Math.random()*300+201);				// 2~5 second delay (making sure that the aliens don't fire before the previous bullets have reached the bottom)
				}
				
				//check to see if a bullet has hit the ship
				for(int i=0; i<alienSlob.length; i++){
					if(alienSlob[i].intersects(ship.getX(), ship.getY(), ship.getWidth(), ship.getHeight())){
						alienSlob[i].setPos(0, getHeight());
						alienSlob[i].setDir(0, 0);
						lives--;
					}
				}
				
				//change the ship's colour and speed depending on how many lives left
				switch(lives){
					case 2:
						ship.setSpeed(4);
						ship.setImage(ShipPicYellow);
						break;
					case 1:
						ship.setSpeed(2);
						ship.setImage(ShipPicRed);
						break;
					default:
						ship.setSpeed(6);
						ship.setImage(ShipPicGreen);
						break;
				}
				
				//if any bullet hits the shields, take one life off the shield
				for(int i=0; i<shield.length; i++){
					//alien bullets
					for(int j=0; j<alienSlob.length; j++){
						if(shield[i].intersects(alienSlob[j].getX(), alienSlob[j].getY(), alienSlob[j].getWidth(), alienSlob[j].getHeight())){
							shieldLife[i]--;
							alienSlob[j].setPos(alienSlob[j].getX(), getHeight());
						}
					}
					//player bullets
					if(shield[i].intersects(machineGun.getX(), machineGun.getY(), machineGun.getWidth(), machineGun.getHeight())){
						shieldLife[i]--;
						machineGun.setPos(machineGun.getX(), 0-machineGun.getHeight());
					}
				}
				
				//if the aliens hit the shields, the shield is automatically destroyed
				for(int i=0; i<shield.length; i++){
					for(int j=0; j<alien1.length; j++){
						if((alien1[j].intersects(shield[i].getX(), shield[i].getY(), shield[i].getWidth(), shield[i].getHeight())) ||
								(alien2[j].intersects(shield[i].getX(), shield[i].getY(), shield[i].getWidth(), shield[i].getHeight())) ||
								(alien3[j].intersects(shield[i].getX(), shield[i].getY(), shield[i].getWidth(), shield[i].getHeight())) ||
								(alien4[j].intersects(shield[i].getX(), shield[i].getY(), shield[i].getWidth(), shield[i].getHeight()))){
							shieldLife[i]=0;
						}
					}
				}
				
				//if shield life is 0, shield is destroyed
				for(int i=0; i<shieldLife.length; i++){
					if(shieldLife[i]<=0){
						shield[i].setSize(0, 0);
					}
				}				
				
				//check if player has any lives left
				if(lives<=0){
					ship.setPos(ship.getX(), 0-ship.getHeight());
					ship.setSpeed(0);
				}
				
				//check if all the aliens are dead
				for(int i=0; i<alien1.length; i++){
					if(alien1[i].getX()==0 && alien1[i].getY()>=getHeight()){
						deadAlien++;
					}
					if(alien2[i].getX()==0 && alien2[i].getY()>=getHeight()){
						deadAlien++;
					}
					if(alien3[i].getX()==0 && alien3[i].getY()>=getHeight()){
						deadAlien++;
					}
					if(alien4[i].getX()==0 && alien4[i].getY()>=getHeight()){
						deadAlien++;
					}
				}
				
				if(deadAlien==40){
					//check if the player is still alive
					if(ship.intersects(0, 0, getWidth(), getHeight())){
						won = true;
						future.cancel(false);
					}
				}else{
					deadAlien=0;
				}
				
				if(!ship.intersects(0, 0, getWidth(), getHeight())){
					lost = true;
					future.cancel(false);
				}
				
				timeTaken = (int)(System.currentTimeMillis() - startTime) / 1000;
				
				AlienSlobTimeout++;
				counter++;
				timeCount++;
				
				paint(g);
			}
		};
			
		t = Executors.newSingleThreadScheduledExecutor();
		future = t.scheduleAtFixedRate(gameLoop, 100, 30, TimeUnit.MILLISECONDS);
	}
	
	public void keyPressed(KeyEvent e){

		if (e.getKeyCode() == KeyEvent.VK_RIGHT){
			ship.setDir(1, ship.getYDir());
		}
			
		if (e.getKeyCode() == KeyEvent.VK_LEFT){
			ship.setDir(-1,ship.getYDir());
		}
		
		if (e.getKeyCode() == KeyEvent.VK_UP){
			ship.setDir(ship.getXDir(), -1);
		}
		
		if (e.getKeyCode() == KeyEvent.VK_DOWN){
			ship.setDir(ship.getXDir(), 1);
		}
		
		if (e.getKeyCode() == KeyEvent.VK_SPACE && !reloading && bulletsLeft > 0 && lives > 0){
			machineGun.fire(ship.getX()+26, ship.getY(), -1);
			bulletsLeft--;														//avoid player from just holding space bar down
			reloading = true;													//avoid player from spamming bullets
		}
	} 
  
 	public void keyReleased(KeyEvent e){
		
		if (e.getKeyCode() == KeyEvent.VK_RIGHT){
			ship.setDir(0, ship.getYDir());
		}
		
		if (e.getKeyCode() == KeyEvent.VK_LEFT){
			ship.setDir(0,ship.getYDir());
		}
		
		if (e.getKeyCode() == KeyEvent.VK_UP){
			ship.setDir(ship.getXDir(), 0);
		}
		
		if (e.getKeyCode() == KeyEvent.VK_DOWN){
			ship.setDir(ship.getXDir(), 0);
		}
		
		//cheat codes... :)
		
		// F = infinite lives
		// D = infinite bullets
		// S = restore shield to full health
		// A = frozen aliens
		
		if (e.getKeyCode() == KeyEvent.VK_F){
			GodMode = true;
			lives = Integer.MAX_VALUE;
			cheated = true;
		}
		
		if (e.getKeyCode() == KeyEvent.VK_D){
			UnlimitedBullets = true;
			bulletsLeft = Integer.MAX_VALUE;
			cheated = true;
		}
		
		if (e.getKeyCode() == KeyEvent.VK_S){
			ShieldRegen = true;
			for(int i=0; i<shieldLife.length; i++){
				shieldLife[i] = 10;
				shield[i].setSize(200, 25);
			}
			cheated = true;
		}
		
		if (e.getKeyCode() == KeyEvent.VK_A){
			AlienFreeze = true;
			stopped = true;
			for(int i=0; i<alien1.length; i++){
				alien1[i].setSpeed(0);
				alien2[i].setSpeed(0);
				alien3[i].setSpeed(0);
				alien4[i].setSpeed(0);
				
				//if aliens are off the screen when stopped, it will be impossible to shoot at, so just kill them and send them off the screen
				if(alien1[i].getX() < 0 || alien1[i].getY() + alien1[i].getWidth() > 1600){
					alien1[i].setPos(0, getHeight());
				}
				if(alien2[i].getX() < 0 || alien2[i].getY() + alien2[i].getWidth() > 1600){
					alien2[i].setPos(0, getHeight());
				}
				if(alien3[i].getX() < 0 || alien3[i].getY() + alien3[i].getWidth() > 1600){
					alien3[i].setPos(0, getHeight());
				}
				if(alien4[i].getX() < 0 || alien4[i].getY() + alien4[i].getWidth() > 1600){
					alien4[i].setPos(0, getHeight());
				}
			}
			cheated = true;
		}
	}

	public void keyTyped(KeyEvent e){
		
	}

	public void paint(Graphics g){
		
		Graphics2D g2 = (Graphics2D) g;
		Color transparent = new Color(0, 0, 0, 0);
		
		//paint background
		graphicsBuffer.setColor(Color.black);
		graphicsBuffer.fillRect(0, 0, getWidth(), getHeight());
		
		//paint aliens
		graphicsBuffer.setColor(transparent);
		for(int i=0; i<alien1.length; i++){
			alien1[i].paint(graphicsBuffer, this);
			alien2[i].paint(graphicsBuffer, this);			
			alien3[i].paint(graphicsBuffer, this);
			alien4[i].paint(graphicsBuffer, this);
		}
		
		//paint target line (to shoot)
		//3 lives: red
		//2 lives: dark gray
		//1 life: invisible
		Color red;
		if(lives >= 3){
			red = new Color(1, 0, 0, 0.9f);
			graphicsBuffer.setColor(red);
		}else if(lives == 2){
			red = new Color(1, 0, 0, 0.5f);
			graphicsBuffer.setColor(red);
		}else if(lives == 1){
			red = new Color(1, 0, 0, 0.2f);
			graphicsBuffer.setColor(red);
		}
		laserSight.paint(graphicsBuffer);
				
		//paint machine gun bullets
		graphicsBuffer.setColor(transparent);
		machineGun.paint(graphicsBuffer, this);
		
		//paint alien bullets
		graphicsBuffer.setColor(transparent);
		for(int i=0; i<alienSlob.length; i++){
			alienSlob[i].paint(graphicsBuffer, this);
		}
		
		//show bullets left, and colour it depending on how many left
		if(bulletsLeft>100){
			graphicsBuffer.setColor(Color.lightGray);
			graphicsBuffer.drawString("Bullets left: Infinite!", 5, getHeight() - 10);
		}else if(bulletsLeft>30){
			graphicsBuffer.setColor(Color.green);
			graphicsBuffer.drawString("Bullets left: " + bulletsLeft, 5, getHeight() - 10);
		}else if(bulletsLeft>15){
			graphicsBuffer.setColor(Color.yellow);
			graphicsBuffer.drawString("Bullets left: " + bulletsLeft, 5, getHeight() - 10);
		}else{
			graphicsBuffer.setColor(Color.red);
			graphicsBuffer.drawString("Bullets left: " + bulletsLeft, 5, getHeight() - 10);
		}
		
		//show lives left, and colour it depending on how many left
		if(lives==3){
			graphicsBuffer.setColor(Color.green);
			graphicsBuffer.drawString("Health left: 3", getWidth() - 75, getHeight() - 10);
		}else if(lives==2){			
			graphicsBuffer.setColor(Color.yellow);
			graphicsBuffer.drawString("Health left: 2", getWidth() - 75, getHeight() - 10);
		}else if(lives==1){
			graphicsBuffer.setColor(Color.red);
			graphicsBuffer.drawString("Health left: 1", getWidth() - 75, getHeight() - 10);
		}else if(lives<=0){
			graphicsBuffer.setColor(Color.gray);
			graphicsBuffer.drawString("Health left: 0", getWidth() - 75, getHeight() - 10);
		}else{
			graphicsBuffer.setColor(Color.lightGray);
			graphicsBuffer.drawString("Health: Infinite!", getWidth() - 100, getHeight() - 10);
		}
		
		//paint ship
		graphicsBuffer.setColor(transparent);
		ship.paint(graphicsBuffer, this);
		
		//show controls
		String controls = "Arrow keys to move  |  'Space' key to shoot";
		graphicsBuffer.setColor(Color.MAGENTA);
		graphicsBuffer.drawString(controls, getWidth()/2 - getStringWidth(controls)/2, getHeight() - 10);
		
		//paint shield, and shade it depending on its state (white = full, black = destroyed)
		Color c;
		for(int i=0; i<shield.length; i++){
			if(shieldLife[i]>=10){
				c = new Color(0, 0, 255, 255);
			}else if(shieldLife[i]==9){
				c = new Color(0, 0, 255, 230);
			}else if(shieldLife[i]==8){
				c = new Color(0, 0, 255, 205);
			}else if(shieldLife[i]==7){
				c = new Color(0, 0, 255, 180);
			}else if(shieldLife[i]==6){
				c = new Color(0, 0, 255, 155);
			}else if(shieldLife[i]==5){
				c = new Color(0, 0, 255, 130);
			}else if(shieldLife[i]==4){
				c = new Color(0, 0, 255, 105);
			}else if(shieldLife[i]==3){
				c = new Color(0, 0, 255, 80);
			}else if(shieldLife[i]==2){
				c = new Color(0, 0, 255, 55);
			}else if(shieldLife[i]==1){
				c = new Color(Color.red.getRGB());
			}else{
				c = new Color(Color.white.getRGB());
			}
			graphicsBuffer.setColor(c);		
			shield[i].paint(graphicsBuffer, this);
		}
		
		//if the the player won, paint (and check if the player is a cheater :D )
		if(won){
			graphicsBuffer.setColor(Color.black);
			graphicsBuffer.fillRect(0, 0, getWidth(), getHeight());
			graphicsBuffer.setColor(Color.white);
			Font f = new Font("Comic Sans MS", Font.BOLD, 22);
			graphicsBuffer.setFont(f);
			
			//fair play is rewarded.
			if(!cheated){
				
				//print "Congratulations! You have won the game with x bullet(s) remaining and y life/lives left!"
				if(lives==1){
					if(bulletsLeft==1){
						graphicsBuffer.drawString("Congratulations! You have won the game with 1 bullet remaining and 1 life left!", getWidth()/2-410, getHeight()/2-11);
					}else{
						graphicsBuffer.drawString("Congratulations! You have won the game with " + bulletsLeft + " bullets remaining and 1 life left!", getWidth()/2-410, getHeight()/2-11);
					}
				}else{
					if(bulletsLeft==1){
						graphicsBuffer.drawString("Congratulations! You have won the game with 1 bullet remaining and " + lives + " lives left!", getWidth()/2-410, getHeight()/2-11);
					}else{
						graphicsBuffer.drawString("Congratulations! You have won the game with " + bulletsLeft + " bullets remaining and " + lives + " lives left!", getWidth()/2-410, getHeight()/2-11);
					}
				}
				
				//print "Shot Accuracy: ##%  (x hit(s) out of y shot(s))"
				if(hitAlien==1 && 60-bulletsLeft==1){
					graphicsBuffer.drawString("Shot Accuracy: " + ((hitAlien*100)/(START_BULLETS-bulletsLeft)) + "%  (1 hit out of 1 shot)", getWidth()/2-250, getHeight()/2+250);
				}else if(hitAlien>1 && 60-bulletsLeft==1){
                    graphicsBuffer.drawString("Shot Accuracy: " + ((hitAlien*100)/(START_BULLETS-bulletsLeft)) + "%  (" + hitAlien + " hits out of 1 shot)", getWidth()/2-250, getHeight()/2+250);
				}else if(hitAlien==1 && 60-bulletsLeft>1){
                    graphicsBuffer.drawString("Shot Accuracy: " + ((hitAlien*100)/(START_BULLETS-bulletsLeft)) + "%  (1 hit out of " + (START_BULLETS-bulletsLeft) + " shots)", getWidth()/2-250, getHeight()/2+250);
				}else{
                    graphicsBuffer.drawString("Shot Accuracy: " + ((hitAlien*100)/(START_BULLETS-bulletsLeft)) + "%  (" + hitAlien + " hits out of " + (START_BULLETS-bulletsLeft) + " shots)", getWidth()/2-250, getHeight()/2+250);
				}
			
				//print "Time taken: x seconds"
				//this won't be very accurate at all; it is just a fun counter
				graphicsBuffer.drawString("Time taken: " + timeCount/100 + " seconds", getWidth()/2-140, getHeight()/2+350);
				
			}else{
				
				//print "Good job, you won the game.  Next time, try playing without cheats!"
				graphicsBuffer.drawString("Good job, you won the game.  Next time, try playing without cheats!", getWidth()/2-380, getHeight()/2-11);
				
				//list cheats used (for fun)
				graphicsBuffer.drawString("Cheats used: ", getWidth()/2-100, getHeight()/2+250);
				
				//God Mode
				if(GodMode){
					graphicsBuffer.setColor(Color.white);
				}else{
					graphicsBuffer.setColor(Color.darkGray);
				}
				graphicsBuffer.drawString("God Mode (Unlimited Lives)", getWidth()/2-170, getHeight()/2+290);
				
				//Super Gun
				if(UnlimitedBullets){
					graphicsBuffer.setColor(Color.white);
				}else{
					graphicsBuffer.setColor(Color.darkGray);
				}
				graphicsBuffer.drawString("Super Gun (Unlimited Bullets)", getWidth()/2-180, getHeight()/2+330);
				
				//Shield Regeneration
				if(ShieldRegen){
					graphicsBuffer.setColor(Color.white);
				}else{
					graphicsBuffer.setColor(Color.darkGray);
				}
				graphicsBuffer.drawString("Shield Regeneration", getWidth()/2-130, getHeight()/2+370);
				
				//Alien Freeze
				if(AlienFreeze){
					graphicsBuffer.setColor(Color.white);
				}else{
					graphicsBuffer.setColor(Color.darkGray);
				}
				graphicsBuffer.drawString("Alien Freeze", getWidth()/2-100, getHeight()/2+410);
			}
		}
		
		//if the player lost, paint (and again, check if the player has cheated)
		if(lost){
			graphicsBuffer.setColor(Color.black);
			graphicsBuffer.fillRect(0, 0, getWidth(), getHeight());
			graphicsBuffer.setColor(Color.white);
			Font f = new Font("Comic Sans MS", Font.BOLD, 22);
			graphicsBuffer.setFont(f);
			
			if(!cheated){
				//print "Sorry, it looks like the aliens have gotten to you, and you've lost the game.  Good luck next time!"
				graphicsBuffer.drawString("Oi, it looks like the aliens have gotten to you, and you've lost the game.  Better luck next time!", getWidth()/2-550, getHeight()/2-11);
				
				//	use try-catch statement block here to protect from division by zero
				/*	if no bullets fired, print that (and in doing so, catch division by zero)
				*	if 1 bullet fired, print "You only fired one shot..."
				*	otherwise, print shot accuracy ("Shot Accuracy: ##%  (x hit(s) out of y shot(s))")	*/
				try{
					if(bulletsLeft == 59){
						graphicsBuffer.drawString("You only fired one shot...", getWidth()/2 - 140, getHeight() - 200);
					}else{
						if(hitAlien==1 && 60-bulletsLeft==1){
							graphicsBuffer.drawString("Shot Accuracy: " + ((hitAlien*100)/(START_BULLETS-bulletsLeft)) + "%  (1 hit out of 1 shot)", getWidth()/2-250, getHeight() - 200);
						}else if(hitAlien>1 && 60-bulletsLeft==1){
							graphicsBuffer.drawString("Shot Accuracy: " + ((hitAlien*100)/(START_BULLETS-bulletsLeft)) + "%  (" + hitAlien + " hits out of 1 shot)", getWidth()/2-250, getHeight() - 200);
						}else if(hitAlien==1 && 60-bulletsLeft>1){
							graphicsBuffer.drawString("Shot Accuracy: " + ((hitAlien*100)/(START_BULLETS-bulletsLeft)) + "%  (1 hit out of " + (START_BULLETS-bulletsLeft) + " shots)", getWidth()/2-250, getHeight() - 200);
						}else{
							graphicsBuffer.drawString("Shot Accuracy: " + ((hitAlien*100)/(START_BULLETS-bulletsLeft)) + "%  (" + hitAlien + " hits out of " + (START_BULLETS-bulletsLeft) + " shots)", getWidth()/2-250, getHeight() - 200);
						}
					}
				}catch(Exception e){
					graphicsBuffer.drawString("You didn't fire a single shot!", getWidth()/2-150, getHeight() - 200);
					e.printStackTrace();
				}
				
				//print "Time taken: x seconds"
				//this won't be very accurate at all; it is just a fun counter
				graphicsBuffer.drawString("Time taken: " + timeTaken + " seconds", getWidth()/2-140, getHeight()/2+350);
			}else{
				//not much sympathy for those who cheated and still couldn't win...
				graphicsBuffer.drawString("You cheated, and you still lost.  Shame on you.", getWidth()/2-250, getHeight()/2);
				
				int side = getHeight() / 3;
				int midX = getWidth() / 2 - side / 2;
				//paint a bunch of skull and crossbones around the screen
				graphicsBuffer.drawImage(skull, 0, 0, side, side, this);								// X * * | * * *
				graphicsBuffer.drawImage(skull, midX, 0, side, side, this);								// * X * | * * *
				graphicsBuffer.drawImage(skull, getWidth()-side, 0, side, side, this);					// * * X | * * *
				graphicsBuffer.drawImage(skull, 0, getHeight()-side, side, side, this);					// * * * | X * *
				graphicsBuffer.drawImage(skull, midX, getHeight()-side, side, side, this);				// * * * | * X *
				graphicsBuffer.drawImage(skull, getWidth()-side, getHeight()-side, side, side, this);	// * * * | * * X
			}
		}
		
		//paint everything from buffer
		g2.drawImage(imageBuffer, 0,0, getWidth(), getHeight(), this);
	}
	
	private Image getImage(String name) {
		try {
			return ImageIO.read(getClass().getClassLoader().getResource(name));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Image getIconImage() {
		return alienPic;
	}
	
	private int getStringWidth(String string) {
		return graphicsBuffer.getFontMetrics().stringWidth(string);
	}
}