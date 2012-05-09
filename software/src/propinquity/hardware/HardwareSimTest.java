package propinquity.hardware;

import processing.core.*;
import controlP5.*;

public class HardwareSimTest extends PApplet implements ProxEventListener {

	// Unique serialization ID
	private static final long serialVersionUID = 6340508174717159418L;

	static final int[] PATCH_ADDR = new int[] { 1, 6 };
	static final int NUM_PATCHES = PATCH_ADDR.length;

	HardwareSimulator simulator;

	Patch[] patches;

	ControlP5 controlP5;
	boolean show_controls = true;

	Slider prox_sliders[];

	public void setup() {
		size(1024, 800);

		controlP5 = new ControlP5(this);

		prox_sliders = new Slider[NUM_PATCHES];

		for(int i = 0;i < NUM_PATCHES;i++) {
			int x_offset = (width-100)/NUM_PATCHES*i+50;
			int y_offset = 60;
			int local_width = round((width-100)/NUM_PATCHES*0.95f);

			int obj_width = 15;
			int slider_height = 200;

			int level_0 = -45;
			int level_1 = 10;
			int level_2 = 240;
			int level_3 = 480;

			int num = 3;

			int incr_offset = 0;
			int incr_width = (local_width-incr_offset*2)/num;
			int obj_offset = incr_offset+(incr_width-obj_width)/2;

			ControlGroup group = controlP5.addGroup("Patch "+i, x_offset, y_offset, local_width);

			Toggle toggle = controlP5.addToggle("Active "+i, incr_width*0+obj_offset, level_0, obj_width, obj_width);
			toggle.setGroup(group);

			Slider r_slider = controlP5.addSlider("Red "+i, 0, 255, 0, incr_width*0+obj_offset, level_1, obj_width, slider_height);
			r_slider.setGroup(group);
			Slider g_slider = controlP5.addSlider("Green "+i, 0, 255, 0, incr_width*1+obj_offset, level_1, obj_width, slider_height);
			g_slider.setGroup(group);
			Slider b_slider = controlP5.addSlider("Blue "+i, 0, 255, 0, incr_width*2+obj_offset, level_1, obj_width, slider_height);
			b_slider.setGroup(group);

			Slider duty_slider = controlP5.addSlider("Color Duty "+i, 0, 255, 0, incr_width*0+obj_offset, level_2, obj_width, slider_height);
			duty_slider.setGroup(group);
			Slider period_slider = controlP5.addSlider("Color Period "+i, 0, 255, 0, incr_width*1+obj_offset, level_2, obj_width, slider_height);
			period_slider.setGroup(group);

			prox_sliders[i] = controlP5.addSlider("Prox "+i, 0, 1024, 0, incr_width*2+obj_offset, level_2, obj_width, slider_height);
			prox_sliders[i].lock();
			prox_sliders[i].setGroup(group);
			
			Slider vibe_slider = controlP5.addSlider("Vibe Level "+i, 0, 255, 0, incr_width*0+obj_offset, level_3, obj_width, slider_height);
			vibe_slider.setGroup(group);
			Slider vibe_duties_slider = controlP5.addSlider("Vibe Duty "+i, 0, 255, 0, incr_width*1+obj_offset, level_3, obj_width, slider_height);
			vibe_duties_slider.setGroup(group);
			Slider vibe_periods_slider = controlP5.addSlider("Vibe Period "+i, 0, 255, 0, incr_width*2+obj_offset, level_3, obj_width, slider_height);
			vibe_periods_slider.setGroup(group);
		}

		if(!show_controls) controlP5.hide();

		simulator = new HardwareSimulator(this);
		simulator.addProxEventListener(this);

		patches = new Patch[NUM_PATCHES];
		for(int i = 0;i < NUM_PATCHES;i++) {
			patches[i] = new Patch(PATCH_ADDR[i], simulator);
			simulator.addPatch(patches[i]);
		}
	}

	public void controlEvent(ControlEvent theEvent) {
		String name = theEvent.controller().name();
		int value = (int)theEvent.controller().value();
		if(name.indexOf("Active") == -1 && value < 10) value = 0; //Snap to zero
		for(int i = 0;i < NUM_PATCHES;i++) {
			if(name.equals("Active "+i)) {
				if(value != 0) patches[i].setActive(true);
				else patches[i].setActive(false);
				return;
			} else if(name.equals("Red "+i)) {
				int[] current_color = patches[i].getColor();
				patches[i].setColor(value, current_color[1], current_color[2]);
				return;
			} else if(name.equals("Green "+i)) {
				int[] current_color = patches[i].getColor();
				patches[i].setColor(current_color[0], value, current_color[2]);
				return;
			} else if(name.equals("Blue "+i)) {
				int[] current_color = patches[i].getColor();
				patches[i].setColor(current_color[0], current_color[1], value);
				return;
			} else if(name.equals("Color Duty "+i)) {
				patches[i].setColorDuty(value);
				return;
			} else if(name.equals("Color Period "+i)) {
				patches[i].setColorPeriod(value);
				return;
			} else if(name.equals("Vibe Level "+i)) {
				patches[i].setVibeLevel(value);
				return;
			} else if(name.equals("Vibe Duty "+i)) {
				patches[i].setVibeDuty(value);
				return;
			} else if(name.equals("Vibe Period "+i)) {
				patches[i].setVibePeriod(value);
				return;
			}
		}
	}

	public void draw() {
		background(0);

		pushMatrix();
		translate(width/2-250, height/2-50);
		simulator.draw();
		popMatrix();
	}
	
	public void keyPressed() {
		if(key == 'h') {
			show_controls = !show_controls;
			if(!show_controls) controlP5.hide();
			else controlP5.show();
		} else if(key == 't') {
			if(simulator.isVisible()) simulator.hide();
			else simulator.show();
		}
	}

	public void proxEvent(Patch patch) {
		for(int i = 0;i < NUM_PATCHES;i++) {
			if(patch == patches[i]) {
				prox_sliders[i].setValue(patch.getProx());
			}
		}
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "propinquity.hardware.HardwareSimTest" });
	}

}