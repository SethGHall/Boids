/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author sehall
 */
public class BoidFlockGUI extends JPanel implements ActionListener, ChangeListener, MouseMotionListener, MouseListener
{
   public static int PANEL_WIDTH = 600;
   public static int PANEL_HEIGHT = 600;
   public Timer timer;
   private BoidFlock flock;
   private JSlider seperation,alignment,cohesion, radius, mouseForce;
   private DrawPanel drawPanel;
   private JLabel seperationL,alignmentL,cohesionL,radiusL,boidCount,mouseForceL;
   private final JButton addBoid;
   private final JButton removeBoid;
   private static final int NUM_START_BOIDS = 100;

   public BoidFlockGUI()
   {    
      super(new BorderLayout());
      try
      {  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e){}
      drawPanel = new DrawPanel();
      drawPanel.addMouseListener((MouseListener)this);
      drawPanel.addMouseMotionListener((MouseMotionListener)this);
      flock = new BoidFlock();
      timer = new Timer(25,this);
      timer.start();

      
      JPanel sliderPanel = new JPanel();

      addBoid = new JButton("ADD BOID");
      removeBoid = new JButton("REMOVE BOID");

      addBoid.addActionListener((ActionListener)this);
      removeBoid.addActionListener((ActionListener)this);
      sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));

      seperation = new JSlider(0,500,(int)(Boid.SEPARATION_WEIGHT*100));
      alignment = new JSlider(0, 100, (int)(Boid.ALIGNMENT_WEIGHT*100));
      cohesion = new JSlider(0, 25, (int)(Boid.COHESION_WEIGHT*100));
      radius = new JSlider(0, 1000,(int)Boid.RADIUS);

      seperationL = new JLabel("seperation weight: "+Boid.SEPARATION_WEIGHT);
      alignmentL = new JLabel("alignment weight: "+Boid.ALIGNMENT_WEIGHT);
      cohesionL = new JLabel("cohesion weight: "+Boid.COHESION_WEIGHT);
      radiusL = new JLabel("radius detection: "+Boid.RADIUS);
     
      boidCount = new JLabel("Num Boids: "+flock.getSize());
      
      seperation.setBorder(BorderFactory.createTitledBorder("Separation Weight"));
      sliderPanel.add(seperation);

      alignment.setBorder(BorderFactory.createTitledBorder("Alignment Weight"));
      sliderPanel.add(alignment);
      
      cohesion.setBorder(BorderFactory.createTitledBorder("Cohesion Weight"));
      sliderPanel.add(cohesion);
      
      radius.setBorder(BorderFactory.createTitledBorder("Radius Detection"));
      sliderPanel.add(radius);
      
      sliderPanel.add(addBoid);

      sliderPanel.add(removeBoid);

      JPanel northPanel =  new JPanel();
      northPanel.add(boidCount);

      
      seperation.addChangeListener(this);
      alignment.addChangeListener(this);
      cohesion.addChangeListener(this);
      radius.addChangeListener(this);

      super.add(drawPanel,BorderLayout.CENTER);
      super.add(sliderPanel,BorderLayout.SOUTH);
      super.add(northPanel,BorderLayout.NORTH);
      
      for(int i=0;i<NUM_START_BOIDS;i++)
      {   Boid  boid = new Boid(flock);
          Thread t = new Thread(boid);
          t.start();
          flock.addBoid(boid);       
      }
      boidCount.setText("Num Boids: "+flock.getSize());
   }
   @Override
   public void stateChanged(ChangeEvent event)
   {
        Object source = event.getSource();

        if(source == seperation)
        {   synchronized(this)
            {
                float value = (float)seperation.getValue()/100.0f;
                Boid.SEPARATION_WEIGHT = value;
                System.out.println("separation weight: "+value);
            }
        }
        else if(source == cohesion)
        {   synchronized(this)
            {
                float value = (float)cohesion.getValue()/100.0f;
                Boid.COHESION_WEIGHT = value;
                System.out.println("cohesion weight: "+value);
            }
        }
        else if(source == alignment)
        {   synchronized(this)
            {
                float value = (float)alignment.getValue()/100.0f;
                Boid.ALIGNMENT_WEIGHT = value;
                System.out.println("alignment weight: "+value);
            }
        }
        else if(source == radius)
        {   synchronized(this)
            {
                int value = radius.getValue();
                Boid.RADIUS = value;
                System.out.println("RADIUS IS "+Boid.RADIUS);
            }
        }
        
   }

   @Override
   public void mouseDragged(MouseEvent e)
   {  mousePressed(e);
   }

   @Override
   public void mouseMoved(MouseEvent e){}

   @Override
   public void mouseClicked(MouseEvent e){}

   @Override
   public void mousePressed(MouseEvent e){  
      System.out.println("ADDING FORCE TO mouse ");
      //flock.addExternalForce((float)e.getX(), (float)e.getY());
   }

   @Override
   public void mouseReleased(MouseEvent e)
   {  //flock.releaseExternalForce();
   }

   @Override
   public void mouseEntered(MouseEvent e)
   {}

   @Override
   public void mouseExited(MouseEvent e)
   {}
   
   private class DrawPanel extends JPanel
   {
       public DrawPanel()
       {   super();
           super.setPreferredSize(new Dimension(PANEL_WIDTH ,PANEL_HEIGHT));
           super.setBackground(Color.BLACK);
       }
       @Override
       public void paintComponent(Graphics g)
       {  super.paintComponent(g);
          PANEL_HEIGHT = getHeight();
          PANEL_WIDTH = getWidth();
          flock.drawBoids(g);
       }
   }
   public void actionPerformed(ActionEvent event)
   {
      Object source = event.getSource();

      if(source == timer)
      {  drawPanel.repaint();
      }
      else if(source == addBoid)
      {
          Boid  boid = new Boid(flock);
          Thread t = new Thread(boid);
          t.start();
          flock.addBoid(boid);
          boidCount.setText("Num Boids: "+flock.getSize());
      }
      else if(source == removeBoid && flock.getSize() > 0)
      {
          flock.removeBoid();
          boidCount.setText("Num Boids: "+flock.getSize());
      }
   }
   public static void main(String[] args)
   {
      System.out.println("============BOOOOOOOOOOOOIIDS OUT THERE===============");
      JFrame frame = new JFrame("Boids - Flocking Algorithm");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(new BoidFlockGUI());
      //gets the dimensions for screen width and height to calculate center
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Dimension dimension = toolkit.getScreenSize();
      int screenHeight = dimension.height;
      int screenWidth = dimension.width;
      frame.pack();             //resize frame apropriately for its content
      //positions frame in center of screen
      frame.setLocation(new Point((screenWidth/2)-(frame.getWidth()/2),
         (screenHeight/2)-(frame.getHeight()/2)));
      frame.setVisible(true);
   }
}
