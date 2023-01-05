
/**
 * Graphical GUI for drawing Boids and stating them as threads - can apply mouse force
 * to separate the flock.
 *
 * @author sehall
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BoidFlockGUI extends JPanel implements ActionListener, ChangeListener, MouseMotionListener, MouseListener {

    public static int PANEL_WIDTH = 800;
    public static int PANEL_HEIGHT = 600;
    public Timer timer;
    private final BoidFlock flock;
    private final JSlider seperation, alignment, cohesion, radius;
    private final DrawPanel drawPanel;
    private final JLabel boidCount;
    private final JButton addBoid, add100Boid;
    private final JButton removeBoid;

    //setup the GUI panel
    public BoidFlockGUI() {
        super(new BorderLayout());
        flock = new BoidFlock();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
        }
        drawPanel = new DrawPanel();
        drawPanel.addMouseListener((MouseListener) this);
        drawPanel.addMouseMotionListener((MouseMotionListener) this);

        timer = new Timer(25, this);
        timer.start();
        JPanel sliderPanel = new JPanel();

        addBoid = new JButton("ADD BOID");
        add100Boid = new JButton("ADD 100");
        removeBoid = new JButton("REMOVE BOID");

        addBoid.addActionListener((ActionListener) this);
        add100Boid.addActionListener((ActionListener) this);
        removeBoid.addActionListener((ActionListener) this);
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));

        seperation = new JSlider(0, 500, (int) (Boid.SEPARATION_WEIGHT * 100));
        alignment = new JSlider(0, 100, (int) (Boid.ALIGNMENT_WEIGHT * 100));
        cohesion = new JSlider(0, 25, (int) (Boid.COHESION_WEIGHT * 100));
        radius = new JSlider(0, 1000, (int) Boid.RADIUS);

        boidCount = new JLabel("Num Boids: " + flock.getSize());

        seperation.setBorder(BorderFactory.createTitledBorder("Separation Weight"));
        sliderPanel.add(seperation);

        alignment.setBorder(BorderFactory.createTitledBorder("Alignment Weight"));
        sliderPanel.add(alignment);

        cohesion.setBorder(BorderFactory.createTitledBorder("Cohesion Weight"));
        sliderPanel.add(cohesion);

        radius.setBorder(BorderFactory.createTitledBorder("Radius Detection"));
        sliderPanel.add(radius);

        sliderPanel.add(addBoid);
        sliderPanel.add(add100Boid);
        sliderPanel.add(removeBoid);

        JPanel northPanel = new JPanel();
        northPanel.add(boidCount);

        seperation.addChangeListener(this);
        alignment.addChangeListener(this);
        cohesion.addChangeListener(this);
        radius.addChangeListener(this);

        super.add(drawPanel, BorderLayout.CENTER);
        super.add(sliderPanel, BorderLayout.SOUTH);
        super.add(northPanel, BorderLayout.NORTH);
    }

    //Handle sliders for weight adjustments
    @Override
    public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();

        if (source == seperation) {
            synchronized (this) {
                float value = (float) seperation.getValue() / 100.0f;
                Boid.SEPARATION_WEIGHT = value;
                System.out.println("separation weight: " + value);
            }
        } else if (source == cohesion) {
            synchronized (this) {
                float value = (float) cohesion.getValue() / 100.0f;
                Boid.COHESION_WEIGHT = value;
                System.out.println("cohesion weight: " + value);
            }
        } else if (source == alignment) {
            synchronized (this) {
                float value = (float) alignment.getValue() / 100.0f;
                Boid.ALIGNMENT_WEIGHT = value;
                System.out.println("alignment weight: " + value);
            }
        } else if (source == radius) {
            synchronized (this) {
                int value = radius.getValue();
                Boid.RADIUS = value;
                System.out.println("RADIUS IS " + Boid.RADIUS);
            }
        }

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mousePressed(e);
    }
    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {
        flock.addExternalForce((float) e.getX(), (float) e.getY());
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        flock.releaseExternalForce();
    }
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    //Custom JPanel to draw all boids
    private class DrawPanel extends JPanel {

        public DrawPanel() {
            super();
            super.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
            super.setBackground(Color.BLACK);
        }
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            PANEL_HEIGHT = getHeight();
            PANEL_WIDTH = getWidth();
            flock.drawBoids(g);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        //handel repaint refresh of drawing panel and buttons
        if (source == timer) {
            drawPanel.repaint();
        } else if (source == addBoid) {
            addBoidAndThread();
            boidCount.setText("Num Boids: " + flock.getSize());
        } else if (source == add100Boid) {
            for (int i = 0; i < 100; i++) {
                addBoidAndThread();
            }
            boidCount.setText("Num Boids: " + flock.getSize());
        } else if (source == removeBoid && flock.getSize() > 0) {
            flock.removeBoid();
            boidCount.setText("Num Boids: " + flock.getSize());
        }
    }

    private void addBoidAndThread() {
        Boid boid = new Boid(flock);
        Thread t = new Thread(boid);
        t.start();
        flock.addBoid(boid);
    }

    public static void main(String[] args) {
        System.out.println("============BOID FLOCKING ALGORITHM===============");
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
        frame.setLocation(new Point((screenWidth / 2) - (frame.getWidth() / 2),
                (screenHeight / 2) - (frame.getHeight() / 2)));
        frame.setVisible(true);
    }
}
