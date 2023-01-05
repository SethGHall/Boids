/**
 * A multi threaded boid which flocks depending on separation, alignment and cohesion weights
 * @author Seth
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

public class Boid implements Runnable {

    private final Color[] colour;
    private boolean stopRequested;
    public static final int THREAD_SLEEP = 20;
    public static float RADIUS = 200f;

    public static final int BOID_LENGTH = 20;
    private Random gen;
    private BoidFlock flock;

    private Point position;     //x and y position of boid
    private Vector movement;    //velocity of boid
    public static float SEPARATION_WEIGHT = 1.0f; // separation steering force weighting 1.5
    public static float ALIGNMENT_WEIGHT = 0.05f; // alignment steering force weighting 0.005
    public static float COHESION_WEIGHT = 0.01f;  // cohesion steering force weighting ( 0.01)
    public static float EXTERNAL_WEIGHT = 10.0f;   // cohesion steering force weighting ( 0.01)
    public static int MAX_SPEED = 5;              // upper bound for speed of one boid
    private static boolean enableExtForce;        //Enable external force location
    private static int xExt = 0;           //set external force
    private static int yExt = 0;
    
    //constructor takes reference back to the boid flock. and needs the starting
    //postion and velocity of the boid as well as the boundaries for the panel
    public Boid(BoidFlock flock) {
        position = new Point();
        movement = new Vector();
        gen = new Random();
        colour = new Color[3];
        for (int i = 0; i < colour.length; i++) {
            colour[i] = new Color(gen.nextFloat(), gen.nextFloat(), gen.nextFloat());
        }
        position.x = gen.nextInt(BoidFlockGUI.PANEL_WIDTH - BOID_LENGTH) + BOID_LENGTH;
        position.y = gen.nextInt(BoidFlockGUI.PANEL_HEIGHT - BOID_LENGTH) + BOID_LENGTH;
        movement.dx = gen.nextFloat() * MAX_SPEED*2 - MAX_SPEED;
        movement.dy = gen.nextFloat() * MAX_SPEED*2 - MAX_SPEED;
        this.flock = flock;
    }
    //getters and settings

    public synchronized float getVelX() {
        return movement.dx;
    }

    public synchronized float getVelY() {
        return movement.dy;
    }

    public synchronized float getXPosition() {
        return position.x;
    }

    public synchronized float getYPosition() {
        return position.y;
    }

    public synchronized void setVelX(float velX) {
         movement.dx = velX;
    }

    public synchronized void setVelY(float velY) {
         movement.dy = velY;
    }

    public synchronized void setXPosition(float xPos) {
        this.position.x = xPos;
    }

    public synchronized void setYPosition(float yPos) {
        this.position.y = yPos;
    }
    //draws a single boid.

    public void drawBoid(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));
        double speed = Math.sqrt((movement.dx * movement.dx) + (movement.dy * movement.dy));
        if (speed < 0.01f) {
            speed = 0.01f;
        }
        // calculate direction of boid normalised to length BOID_LENGTH/2
        double vx_dir = BOID_LENGTH * movement.dx / (2.0f * speed);
        double vy_dir = BOID_LENGTH * movement.dy / (2.0f * speed);
        g.setColor(colour[0]);
        g.drawLine((int) (position.x - 2.0f * vx_dir), (int) (position.y - 2.0f * vy_dir), (int) position.x, (int) position.y);
        g.setColor(colour[1]);
        g.drawLine((int) position.x, (int) position.y, (int) (position.x - vx_dir + vy_dir), (int) (position.y - vx_dir - vy_dir));
        g.setColor(colour[2]);
        g.drawLine((int) position.x, (int) position.y, (int) (position.x - vx_dir - vy_dir), (int) (position.y + vx_dir - vy_dir));
    }
    //called so if an external force is applied, then we set the force up
    //at the parameter co-ordinates.

    public static void setExternalForceLocation(float xExt, float yExt) {
        enableExtForce = true;
        Boid.xExt = (int)xExt;
        Boid.yExt = (int)yExt;
    }
    //called to turn off the external force

    public static void removeExternalForceLocation() {
        enableExtForce = false;
    }
    
    //update the position of the boid based on its velocity, should be
    //avoid shared used of position and velocity variables
    private synchronized void moveBoid() {
        position.x += movement.dx;
        position.y += movement.dy;
        int boundX = BoidFlockGUI.PANEL_WIDTH - BOID_LENGTH - 1;
        int boundY = BoidFlockGUI.PANEL_HEIGHT - BOID_LENGTH - 1;

        // check whether boid off bounds of screen and if so bounce back
        if (position.x < 0) {
            position.x = 2 * BOID_LENGTH - position.x;
            movement.dx = -movement.dx;
        } else if (position.x >= boundX) {
            position.x = 2 * boundX - position.x;
            movement.dx = -movement.dx;
        }
        if (position.y < BOID_LENGTH) {
            position.y = 2 * BOID_LENGTH - position.y;
            movement.dy = -movement.dy;
        } else if (position.y >= boundY) {
            position.y = 2 * boundY - position.y;
            movement.dy = -movement.dy;
        }
    }
    //run method for single boid to run this thread concurrently

    public void run() {
        stopRequested = false;
        List<Boid> neighbours = null;
        //create all posible forces
        Vector steering_force = new Vector();
        Vector separation_force = new Vector();
        Vector alignment_force = new Vector();
        Vector cohesion_force = new Vector();
        Vector external_force = new Vector();
        //loop until requested to stop.
        
        int count = 0;
        int threshold = gen.nextInt(300)+20;
        
        while (!stopRequested) {  //neighbouring boids around this boids position.
            neighbours = flock.getNeighbours(this);
            //option to add another cohesive external force
            Vector steering = new Vector();
            count++;
            if(count >=threshold)
            {    count= 0;
                 threshold = gen.nextInt(300)+20;
                 Vector boidOwn = new Vector(gen.nextFloat()*4.0f - 2.0f,gen.nextFloat()*4.0f - 2.0f);
                 steering_force.dx += boidOwn.dx;
                 steering_force.dy += boidOwn.dy;
            } 
            
            if (enableExtForce) {
                external_Force_behaviour(external_force);
                external_force.dx *= EXTERNAL_WEIGHT;
                external_force.dy *= EXTERNAL_WEIGHT;
                add_available_force(steering_force, external_force);
            }
            // find the separation force on boid and apply the weight factor
            separation_behaviour(neighbours, separation_force);
            separation_force.dx *= SEPARATION_WEIGHT;
            separation_force.dy *= SEPARATION_WEIGHT;
            add_available_force(steering_force, separation_force);
            // find the alignment force and apply the weight factor
            alignment_behaviour(neighbours, alignment_force);
            alignment_force.dx *= ALIGNMENT_WEIGHT;
            alignment_force.dy *= ALIGNMENT_WEIGHT;
            add_available_force(steering_force, alignment_force);
            // find the cohesion force and apply the weight factor
            cohesion_behaviour(neighbours, cohesion_force);
            cohesion_force.dx *= COHESION_WEIGHT;
            cohesion_force.dy *= COHESION_WEIGHT;
            add_available_force(steering_force, cohesion_force);
            // use the cumulative steering force to modify velocity of boid
            float new_vx = getVelX() + steering_force.dx;
            float new_vy = getVelY() + steering_force.dy;
            if (new_vx * new_vx + new_vy * new_vy <= MAX_SPEED * MAX_SPEED) {
                setVelX(new_vx);
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
            try {
                Thread.sleep(THREAD_SLEEP);
            } catch (InterruptedException e) {
            }
        }
    }
    //requests this thread to stop running
    public void requestStop() {
        stopRequested = true;
    }
    // adds the new_force to the steering force
    private void add_available_force(Vector force, Vector new_force) {
        force.dx += new_force.dx;
        force.dy += new_force.dy;
    }

    //method that is called to either repel or attract all boids to a force
    private void external_Force_behaviour(Vector external_force) {
        float x_separation = getXPosition() - xExt;
        float y_separation = getYPosition() - yExt;
        float separation_squared = x_separation * x_separation + y_separation * y_separation;
        if (separation_squared < 1) {
            separation_squared = 1.0f;
        }
        external_force.dx += x_separation / separation_squared;
        external_force.dy += y_separation / separation_squared;

    }
    // calculates the separation component of steering force on a boid
    // based on sum of forces each given by 1/separation of boids

    private void separation_behaviour(List<Boid> neighbours, Vector steering_force) {  // determine which boids are within rang
        //Force steering_force = new Force();
        for (Boid ship : neighbours) {
            float x_separation = getXPosition() - ship.getXPosition();
            float y_separation = getYPosition() - ship.getYPosition();
            float separation_squared = x_separation * x_separation + y_separation * y_separation;
            if (separation_squared < 1) {
                separation_squared = 1.0f;
            }
            // scale steering force so linearly decreases with separation
            steering_force.dx += x_separation / separation_squared;
            steering_force.dy += y_separation / separation_squared;
        }
        //return steering_force;
    }

    //aligns the boids with each of its neighbours by the alignment weight
    private void alignment_behaviour(List<Boid> neighbours, Vector steering_force) {  // determine which boids are within range
        float total_vx = 0.0f;
        float total_vy = 0.0f;
        int counter = 0;
        for (Boid ship : neighbours) {
            total_vx += ship.getVelX();
            total_vy += ship.getVelY();
            counter++;
        }
        if (counter > 0) {
            steering_force.dx = total_vx / counter - getVelX();
            steering_force.dy = total_vy / counter - getVelY();
        }
    }

    //calculates the cohesion component of steering force on a boid
    //based on the average position of tagged (nearby) boids
    private void cohesion_behaviour(List<Boid> neighbours, Vector steering_force) {  // determine which boids are within range
        float total_x = 0.0f;
        float total_y = 0.0f;
        int counter = 0;
        for (Boid ship : neighbours) {
            total_x += ship.getXPosition();
            total_y += ship.getYPosition();
            counter++;
        }
        if (counter > 0) {
            steering_force.dx = total_x / counter - getXPosition();
            steering_force.dy = total_y / counter - getYPosition();
        }
    }
    //Object that represents an applied force and movement direction on a single boid
    private class Vector {
        public float dx, dy;

        public Vector() {
            this(0.0f, 0.0f);
        }

        public Vector(float x, float y) {
            this.dx = x;
            this.dy = y;
        }
        public void clear() {
            dx = 0.0f;
            dy = 0.0f;
        }
    }
    //class that represents the position of the boid
    private class Point
    {   public float x;
        public float y;
        public Point()
        {
            this(0.0f,0.0f);
        }
        public Point(float x, float y)
        {
            this.x = x;
            this.y = y;
        } 
    }
}
