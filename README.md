# Boids
A GUI which shows multithreaded boids which can "flock" using adjustable separation, cohesion and alignment weights. Each Boid acts as its own thread moving within the world coordinates. It periodically asks for "neighbours" and applies seperation, cohesion and alignment forces to its direction vector depending on its neighbour values. These weights can be adjusted with sliders in the GUI.

You can also apply repelling force using the mouse by pressing on and holding the mouse button. The boids will steer away from this.
