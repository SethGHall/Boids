/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Seth
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.Random;

public class Boid implements Runnable
{
    
   private boolean stopRequested;
   public static final int THREAD_SLEEP =10; 
   public static float RADIUS = 100f;

   public static final int BOID_LENGTH=20;
   private Random gen; 
   private BoidFlock flock;
    
   private float xPos,yPos;     //x and y position of boid
   private int boundX,boundY;   //boundaries of panel
   private float velX, velY;    //velocity of boid
   public static float SEPARATION_WEIGHT = 1.0f; // separation steering force weighting 1.5
   public static float ALIGNMENT_WEIGHT = 0.05f; // alignment steering force weighting 0.005
   public static float COHESION_WEIGHT = 0.01f;  // cohesion steering force weighting ( 0.01)
   public static float EXTERNAL_WEIGHT = 5.0f;   // cohesion steering force weighting ( 0.01)
   public static int MAX_SPEED = 5;              // upper bound for speed of one boid
   private boolean enableExtForce;
   private float xExt,yExt;

   //constructor takes reference back to the boid flock. and needs the starting
   //postion and velocity of the boid as well as the boundaries for the panel
   public Boid(BoidFlock flock)
   {
      this.boundX = BoidFlockGUI.PANEL_WIDTH-BOID_LENGTH-1;
      this.boundY = BoidFlockGUI.PANEL_HEIGHT-BOID_LENGTH-1;
      gen = new Random();

      this.xPos = gen.nextInt(BoidFlockGUI.PANEL_WIDTH-BOID_LENGTH)+BOID_LENGTH;
      this.yPos = gen.nextInt(BoidFlockGUI.PANEL_HEIGHT-BOID_LENGTH)+BOID_LENGTH;
      this.velX = gen.nextFloat()*4.0f-2.0f;
      this.velY = gen.nextFloat()*4.0f-2.0f;
      this.flock = flock;
   }
   //getters and settings
   public synchronized float getVelX()
   {    return velX;
   }
   public synchronized float getVelY()
   {    return velY;
   }
   public synchronized float getXPosition()
   {    return xPos;
   }
   public synchronized float getYPosition()
   {    return yPos;
   }
   public synchronized void setVelX(float velX)
   {    this.velX = velX;
   }
   public synchronized void setVelY(float velY)
   {    this.velY = velY;
   }
   public synchronized void setxPos(float xPos)
   {   this.xPos = xPos;
   }
   public synchronized void setyPos(float yPos)
   {   this.yPos = yPos;
   }
   //draws a single boid.
   public void drawBoid(Graphics g)
   {  double speed = Math.sqrt((velX*velX)+(velY*velY));
      if (speed < 0.01f)
         speed = 0.01f;
      // calculate direction of boid normalised to length BOID_LENGTH/2
      double vx_dir = BOID_LENGTH*velX/(2.0f*speed);
      double vy_dir = BOID_LENGTH*velY/(2.0f*speed);
      g.setColor(Color.YELLOW);
      g.drawLine((int)(xPos-2.0f*vx_dir),(int)(yPos-2.0f*vy_dir),(int)xPos,(int)yPos);
      g.setColor(Color.RED);
      g.drawLine((int)xPos,(int)yPos,(int)(xPos-vx_dir+vy_dir),(int)(yPos-vx_dir-vy_dir));
      g.setColor(Color.BLUE);
      g.drawLine((int)xPos,(int)yPos,(int)(xPos-vx_dir-vy_dir),(int)(yPos+vx_dir-vy_dir));
   }
   //called so if an external force is applied, then we set the force up
   //at the parameter co-ordinates.
   public void setExternalForceLocation(float xExt, float yExt)
   {
      enableExtForce = true;
      this.xExt = xExt;
      this.yExt = yExt;
   }
   //called to turn off the external force
   public void removeExternalForceLocation()
   {
      enableExtForce = false;
   }
   //update the position of the boid based on its velocity, should be
   //avoid shared used of position and velocity variables
   private synchronized void moveBoid()
   {  xPos += velX;
      yPos += velY;
      boundX = BoidFlockGUI.PANEL_WIDTH-BOID_LENGTH-1;
      boundY = BoidFlockGUI.PANEL_HEIGHT-BOID_LENGTH-1;
              
      // check whether boid off bounds of screen and if so bounce back
      if (xPos < 0)
      {  xPos = 2*BOID_LENGTH-xPos;
         velX = -velX;
      }
      else if (xPos >= boundX)
      {  xPos = 2*boundX-xPos;
         velX = -velX;
      }
      if (yPos < BOID_LENGTH)
      {  yPos = 2*BOID_LENGTH-yPos;
         velY = -velY;
      }
      else if (yPos >= boundY)
      {  yPos = 2*boundY-yPos;
         velY = -velY;
      }
   }
   //run method for single boid to run this thread concurrently
   public void run()
   {
      stopRequested = false;
      List<Boid> neighbours = null;
      //create all posible forces
      Force steering_force = new Force();
      Force separation_force = new Force();
      Force alignment_force = new Force();
      Force cohesion_force = new Force();
      Force external_force = new Force();
      //loop until requested to stop.
      while(!stopRequested)
      {  //neighbouring boids around this boids position.
         neighbours = flock.getNeighbours(this);
         //option to add another cohesive external force
         if(enableExtForce)
         {
            external_Force_behaviour(external_force);
            external_force.x *= EXTERNAL_WEIGHT;
            external_force.y *= EXTERNAL_WEIGHT;
            add_available_force(steering_force,external_force);
         }     
         // find the separation force on boid and apply the weight factor
         separation_behaviour(neighbours,separation_force);
         separation_force.x *= SEPARATION_WEIGHT;
         separation_force.y *= SEPARATION_WEIGHT;
         add_available_force(steering_force,separation_force);
           // find the alignment force and apply the weight factor
         alignment_behaviour(neighbours,alignment_force);
         alignment_force.x *= ALIGNMENT_WEIGHT;
         alignment_force.y *= ALIGNMENT_WEIGHT;
         add_available_force(steering_force,alignment_force);
         // find the cohesion force and apply the weight factor
         cohesion_behaviour(neighbours,cohesion_force);
         cohesion_force.x *= COHESION_WEIGHT;
         cohesion_force.y *= COHESION_WEIGHT;
         add_available_force(steering_force,cohesion_force);
         // use the cumulative steering force to modify velocity of boid
         float new_vx = getVelX() + steering_force.x;
         float new_vy = getVelY() + steering_force.y;
         if (new_vx*new_vx+new_vy*new_vy <= MAX_SPEED*MAX_SPEED)
         {  setVelX(new_vx);
            setVelY(new_vy);
         }
         //update the position of the boid based on its velocity, should be
         //synchronized to avoid shared used of position and velocity variables
         moveBoid();
         //clear the force objects
         steering_force.clear();
         separation_force.clear();
         cohesion_force.clear();
         alignment_force.clear();
         external_force.clear();
         //put this thread to sleep for 50mS
         try
         {  Thread.sleep(50);
         }catch(InterruptedException e){}
      }
   }
   //requests this thread to stop running
   public void requestStop()
   {    stopRequested = true;
   }
   // adds the new_force to the steering force
    private void add_available_force(Force force,Force new_force)
    {  force.x += new_force.x;
       force.y += new_force.y;
    }
    //method that is called to either repel or attract all boids to a force
   private void external_Force_behaviour(Force external_force)
   {
      float x_separation = getXPosition()-xExt;
      float y_separation = getYPosition()-yExt;
      float separation_squared = x_separation*x_separation + y_separation*y_separation;
      if (separation_squared < 1)
         separation_squared = 1.0f;
      external_force.x += x_separation/separation_squared;
      external_force.y += y_separation/separation_squared;

   }
   // calculates the separation component of steering force on a boid
   // based on sum of forces each given by 1/separation of boids
   private void separation_behaviour(List<Boid> neighbours,Force steering_force)
   {  // determine which boids are within rang
      //Force steering_force = new Force();
      for (Boid ship:neighbours)
      {
         float x_separation = getXPosition()-ship.getXPosition();
         float y_separation = getYPosition()-ship.getYPosition();
         float separation_squared = x_separation*x_separation + y_separation*y_separation;
         if (separation_squared < 1)
            separation_squared = 1.0f;
         // scale steering force so linearly decreases with separation
         steering_force.x += x_separation/separation_squared;
         steering_force.y += y_separation/separation_squared;
      }
      //return steering_force;
   }
    //aligns the boids with each of its neighbours by the alignment weight
    private void alignment_behaviour(List<Boid> neighbours,Force steering_force)
    {  // determine which boids are within range
       float total_vx = 0.0f;
       float total_vy = 0.0f;
       int counter = 0;
       for (Boid ship:neighbours)
       {
          total_vx += ship.getVelX();
          total_vy += ship.getVelY();
          counter++;
       }
       if (counter > 0)
       {  steering_force.x = total_vx/counter - getVelX();
          steering_force.y = total_vy/counter - getVelY();
       }
    }
    //calculates the cohesion component of steering force on a boid
    //based on the average position of tagged (nearby) boids
    private void cohesion_behaviour(List<Boid> neighbours,Force steering_force)
    {  // determine which boids are within range
       float total_x = 0.0f;
       float total_y = 0.0f;
       int counter = 0;
       for (Boid ship:neighbours)
       {  total_x += ship.getXPosition();
          total_y += ship.getYPosition();
          counter++;
       }
       if (counter > 0)
       {  steering_force.x = total_x/counter - getXPosition();
          steering_force.y = total_y/counter - getYPosition();
       }
    }
   //Object that represents an applied force on a single boid
   private class Force
   {  public float x,y;

      public Force()
      {  this(0.0f,0.0f);
      }
      public Force(float x,float y)
      {  this.x = x;
         this.y = y;
      }
      public void clear()
      {
         x=0.0f;y=0.0f;
      }
   }
}

