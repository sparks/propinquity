package propinquity;

import org.jbox2d.common.Vec2;

import processing.core.*;
import propinquity.hardware.*;

/**
 * The level select draws a circular GUI which lets the user select the level they would like to play. All the levels are displayed as dots around a ring. By using the arrow keys, they players can inspect the level names and select the level they wish to play.
 *
 */
public class LevelSelect implements UIElement {

	Propinquity parent;

	Hud hud;

	int selected;
	Level[] levels;

	Particle[] particles;

	Player[] players;
	boolean patchToggle;

	boolean isVisible;

	public LevelSelect(Propinquity parent, Hud hud, Level[] levels, Player[] players) {
		this.parent = parent;
		this.hud = hud;
		this.levels = levels;
		this.players = players;

		isVisible = false;
	}

	public Level getSelectedLevel() {
		return levels[selected];
	}

	public void reset() {
		stopAllPreviews();
		selected = 0;
		createParticles(levels.length, PlayerConstants.NEUTRAL_COLOR);
	}

	public void stopAllPreviews() {
		for(int i = 0;i < levels.length;i++) {
			levels[i].stopPreview();
		}
	}

	void createParticles(int num, Color color) {
		int radius = parent.height/2 - Hud.WIDTH * 2;

		particles = new Particle[num];

		for(int i = 0; i < num; i++) {
			Particle p = new Particle(parent, parent.getOffscreen(), new Vec2(parent.width/2+PApplet.cos(PApplet.TWO_PI/particles.length * i) * radius,
					parent.height/2+PApplet.sin(PApplet.TWO_PI/particles.length * i) * radius), color, Particle.LARGE_SIZE, true);
			particles[i] = p;
		}
	}

	void killParticles() {
		if(particles == null) return;
		for(Particle particle : particles) particle.kill();
	}

	void drawParticles() {
		if(particles == null) return;
		
		for(int i = 0; i < particles.length; i++) {
			particles[i].draw();
		}
	}

	public void draw() {
		if(!isVisible) return;

		hud.drawInnerBoundary();
		hud.drawOuterBoundary();
		
		drawParticles();

		// for(int i = 0;i < levels.length;i++) {
		// 	hud.drawBannerCenter(levels[i].getName(), new Color(255, 255, 255, 0), i*PApplet.TWO_PI/levels.length);
		// }

		// hud.drawCenterText("Select Level", hud.getAngle());
		hud.drawCenterText("Come and play!\nVenez jouer!", hud.getAngle());
		Color c = Color.violet();
		c.a = 200;

		hud.drawBannerCenter(levels[selected].getName(), c, PApplet.TWO_PI/levels.length*selected);

		// parent.fill(255);
		// parent.textFont(hud.font, 30);
		// parent.text(levels[selected].getName(), 30, 30);
		// if(levels[selected].getBPM() != -1) parent.text(levels[selected].getBPM() + " BPM", 30, 60);
	}

	public void show() {
		levels[selected].startPreview();
		isVisible = true;
	}

	public void hide() {
		stopAllPreviews();
		isVisible = false;
	}

	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Receive a keyPressed event.
	 * 
	 * @param key the char of the keyPressed event.
	 * @param keycode the keycode of the keyPressed event.
	 */
	public void keyPressed(char key, int keycode) {
		switch(keycode) {
			case BACKSPACE: {
				killParticles();
				parent.changeGameState(GameState.PlayerSelect);
				break;
			}
			case LEFT: {
				left();
				break;
			}
			case RIGHT: {
				right();
				break;
			}
			case ENTER:
			case ' ': {
				confirm();
				break;
			}
		}

		if(key == 't') {
			patchToggle = !patchToggle;

			if(patchToggle) {
				for(Player player : players) {
					for(Patch patch : player.getPatches()) {
						patch.setActive(true);
						patch.setColor(player.getColor());						
					}

					Glove glove = player.getGlove();
					glove.setActive(true);
					glove.setColor(player.getColor());
				}
			} else {
				for(Player player : players) {
					for(Patch patch : player.getPatches()) {
						patch.setActive(false);
					}
					
					Glove glove = player.getGlove();
					glove.setActive(false);
				}
			}
		}
	}

	public void left() {
		select((selected + levels.length - 1) % levels.length);
	}

	public void right() {
		select((selected + 1) % levels.length);
	}

	public void select(int s) {
		levels[selected].stopPreview();
		selected = s;
		levels[selected].startPreview();
	}

	public void confirm() {
		stopAllPreviews();
		killParticles();
		parent.changeGameState(GameState.Play);
	}
}
