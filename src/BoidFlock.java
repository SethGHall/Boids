
/**
 * Boid flock data structure to hold Boids - the class if thread safe. It is also used
 * by boids to obtain a sublist of "neighbours" - other boids within some radius for flocking
 *
 * @author Seth
 */
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoidFlock {

    private List<Boid> flock;

    public BoidFlock() {   //syncrhonize access to the flock for thread safe adding and removing of boids 
        flock = Collections.synchronizedList(new ArrayList<>());
    }

    //add a boid to the flock - assumes the boid thread is running
    public synchronized void addBoid(Boid boid) {
        flock.add(boid);
    }

    //remove the last boid from the flock
    public synchronized void removeBoid() {
        if (!flock.isEmpty()) {
            Boid boid = flock.remove(flock.size() - 1);
            boid.requestStop();
        }
    }

    //draw each of the boids using the Graphics object
    public void drawBoids(Graphics g) {
        for (Boid boid : flock) {
            boid.drawBoid(g);
        }
    }

    public int getSize() {
        return flock.size();
    }
    //return a sublist of neighbouring boids - boids who are within pixel Radius
    public synchronized List<Boid> getNeighbours(Boid boid) {
        List<Boid> neighbours = new ArrayList<>();
        float xPos = boid.getXPosition();
        float yPos = boid.getYPosition();

        for (Boid neighbour : flock) {
            float x = neighbour.getXPosition();
            float y = neighbour.getYPosition();
            if (Math.sqrt(Math.pow((x - xPos), 2) + Math.pow((y - yPos), 2)) < Boid.RADIUS) {
                if (neighbour != boid) {
                    neighbours.add(neighbour);
                }
            }
        }
        return neighbours;
    }
    //release the external force
    public void releaseExternalForce() {
        Boid.removeExternalForceLocation();
    }
    //add external force which repels boids
    public void addExternalForce(float x, float y) {
        Boid.setExternalForceLocation(x, y);
    }
}
