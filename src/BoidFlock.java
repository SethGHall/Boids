
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Seth
 */
public class BoidFlock {
 
    private List<Boid> flock;
      
    public BoidFlock()
    {
        flock = Collections.synchronizedList(new ArrayList<>());
    }
    
    public synchronized void addBoid(Boid boid)
    {
        flock.add(boid);
    }
    
    public synchronized void removeBoid()
    {
        if(!flock.isEmpty())
        {   Boid boid = flock.remove(flock.size()-1);
            boid.requestStop();
        }
    }
    
    public void drawBoids(Graphics g)
    {
        for(Boid boid:flock)
            boid.drawBoid(g);
    }
   
    
    public int getSize()
    {
        return flock.size();
    }

    public synchronized List<Boid> getNeighbours(Boid boid) {
        
      List<Boid> neighbours = new ArrayList<>();
      float xPos = boid.getXPosition();
      float yPos = boid.getYPosition();

      for(Boid neighbour:flock)
      {  float x = neighbour.getXPosition();
         float y = neighbour.getYPosition();
         if(Math.sqrt(Math.pow((x-xPos),2)+Math.pow((y-yPos),2)) < Boid.RADIUS)
         {  if(neighbour != boid)
            {  neighbours.add(neighbour);
            }
         }
      }
      return neighbours;
    }
    
}
