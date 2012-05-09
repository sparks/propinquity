package propinquity;

import java.util.Vector;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

public class Liquid {

	/** The strength of the acceleration acting on the particles. */
	public static final float GRAVITY_STRENGTH = 0.01f;

	public Vector<Particle> particlesCreated;
	public Vector<Particle> particlesHeld;

	private Propinquity parent;
	private Color color;

	private PImage particleImage;
	private PGraphics particleGraphic;

	public Liquid(Propinquity parent, Color color) {

		this.parent = parent;
		this.color = color;

		particlesCreated = new Vector<Particle>();
		particlesHeld = new Vector<Particle>();

		particleImage = parent.graphics.loadParticle();

		particleGraphic = new PGraphics();
		particleGraphic = parent.createGraphics(particleImage.width, particleImage.height, PApplet.P2D);
		particleGraphic.background(particleImage);
		particleGraphic.mask(particleImage);
	}

	public void reset() {
		particlesCreated = new Vector<Particle>();
		particlesHeld = new Vector<Particle>();
	}

	public void createParticle() {
		particlesCreated.add(new Particle(parent, new Vec2(parent.width / 2f, parent.height / 2f), particleGraphic,
				color));
	}

	public void transferParticles() {
		for (Particle particle : particlesCreated) {
			particle.getCircleDef().filter.categoryBits = 0;
			particle.getCircleDef().filter.maskBits = 0;
			particlesHeld.add(particle);
		}

		particlesCreated = new Vector<Particle>();
	}

	public void applyGravity() {
		float gravX = Liquid.GRAVITY_STRENGTH * PApplet.cos(-parent.hud.angle + PConstants.HALF_PI);
		float gravY = Liquid.GRAVITY_STRENGTH * PApplet.sin(-parent.hud.angle + PConstants.HALF_PI);
		Vec2 gravity = new Vec2(gravX, gravY);

		for (Particle particle : particlesCreated)
			particle.getBody().applyForce(gravity, particle.getBody().getWorldCenter());

		for (Particle particle : particlesHeld)
			particle.getBody().applyForce(gravity, particle.getBody().getWorldCenter());
	}

	public void applyReverseGravity() {
		float gravX = Liquid.GRAVITY_STRENGTH * PApplet.cos(-parent.hud.angle - PConstants.HALF_PI);
		float gravY = Liquid.GRAVITY_STRENGTH * PApplet.sin(-parent.hud.angle - PConstants.HALF_PI);
		Vec2 antiGravity = new Vec2(gravX, gravY);

		for (Particle particle : particlesCreated)
			particle.getBody().applyForce(antiGravity, particle.getBody().getWorldCenter());

		for (Particle particle : particlesHeld)
			particle.getBody().applyForce(antiGravity, particle.getBody().getWorldCenter());
	}

	public void update() {
		for (Particle particle : particlesCreated)
			particle.update();

		for (Particle particle : particlesHeld)
			particle.update();
	}

	public void draw() {
		for (Particle particle : particlesCreated)
			particle.draw();

		for (Particle particle : particlesHeld)
			particle.draw();
	}
}
