package me.toofifty.jmv;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.Texture;

/**
 * Requires:
 * 		jackson-core
 * 		jackson-annotations
 * 		jackson-databind
 * 		jackson-coreutils
 * 		lwjgl
 * 		slick-util
 * 
 * @author Toofifty
 *
 */
public class JSONModelViewer {

	/** Main instance */
	public static JSONModelViewer modelViewer;
	
	/* FPS Info */
	private long lastFrame;
	private long lastFPS;
	protected int fps;
	
	/* Zoom level (aka. z axis) */
	private float zoom = -10;
	
	/* Other classes */
	private CubeRenderer renderer;
	private MouseControl mouse;
	private ControlFrame controlFrame;
	
	private Texture floor;
	
	/** Main model */
	public Model model;
	
	/** New model json string */
	private String jsonModel;
	/** Model update flag */
	private boolean needsUpdate;
	private boolean showFloor = true;

	/**
	 * Main function, init and loop
	 */
	public void start() {
		controlFrame = new ControlFrame();
		getDelta();
		lastFPS = getTime();
		
		initGL();
		
		renderer = new CubeRenderer();
		
		mouse = new MouseControl();
		mouse.update(Mouse.getX(), Mouse.getY());
		
		floor = FileLoader.loadTexture("floor");

		while (!Display.isCloseRequested()) {
			int delta = getDelta();
			
			if (needsUpdate) {
				Model newModel = new Model(FileLoader.loadJson(jsonModel));
				if (newModel != null) {
					model = newModel;
				}
				needsUpdate = false;
			}

			pollInput(delta);
			updateFPS();
			renderGL();
			
			Display.update();
			Display.sync(120);
		}

		Display.destroy();
		controlFrame.dispose();
	}
	
	/**
	 * Init OpenGL context
	 */
	private void initGL() {
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(70, (float) Display.getWidth() / (float) Display.getHeight(), 0.3F, 100);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
    	GL11.glEnable(GL11.GL_BLEND);
    	GL11.glEnable(GL11.GL_CULL_FACE);
    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glClearColor(0.375F, 0.5625F, 0.75F, 1F);
	}

	/**
	 * Main render (looped) 
	 */
	private void renderGL() {
		// Clear screen & depth buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0, 0, zoom);
		
		renderer.doRotate();
		if (renderer.getRX() >= 0 && showFloor) {
			renderer.renderFloor(floor);		
		}
		
		if (this.model != null) {
			renderer.renderModel(model);
		}
	}
	
	/**
	 * Schedule a model update for the next frame
	 * (used by ControlFrame)
	 * 
	 * @param jsonModel
	 */
	public void updateModel(String jsonModel) {
		if (jsonModel != null && jsonModel != "") {
			this.jsonModel = jsonModel;
			this.needsUpdate = true;
		}
	}

	/**
	 * Toggle floor boolean 
	 * (used by ControlFrame)
	 */
	public void toggleFloor() {
		showFloor = !showFloor;
	}

	/**
	 * Set angles to isometric
	 * (used by ControlFrame)
	 */
	public void setIso() {
		renderer.setRY(45F); 
		renderer.setRX(35.264F); 
	}

	/**
	 * Get the time in milliseconds
	 * 
	 * @return time (ms)
	 */
	public long getTime() {
		return System.nanoTime() / 1000000;
	}

	/**
	 * Get delta time since last frame
	 * 
	 * @return delta time (ms)
	 */
	public int getDelta() {
		long time = getTime();
		int delta = (int) (time - lastFrame);
		lastFrame = time;
		return delta;
	}

	/**
	 * Update the FPS on the window title
	 */
	private void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			Display.setTitle("FPS: " + fps);
			fps = 0;
			lastFPS += 1000;
		}
		this.fps++;
	}
	
	/**
	 * Check for and handle input
	 * 
	 * @param delta
	 */
	private void pollInput(float delta) {	
		mouse.update(Mouse.getX(), Mouse.getY());
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W))
			renderer.rotateUp(delta / 20F);
		if (Keyboard.isKeyDown(Keyboard.KEY_S))
			renderer.rotateUp(-delta / 20F);
		if (Keyboard.isKeyDown(Keyboard.KEY_A))
			renderer.rotateLeft(delta / 20F);
		if (Keyboard.isKeyDown(Keyboard.KEY_D))
			renderer.rotateLeft(-delta / 20F);
		
		if (Mouse.isButtonDown(0)) {
			renderer.rotateUp(-mouse.dy() / 2F);
			renderer.rotateLeft(mouse.dx() / 2F);
		}
		
		scroll(-Mouse.getDWheel() / 100F);
	}

	/**
	 * Scroll (zoom) in and out by delta
	 * 
	 * @param delta
	 */
	private void scroll(float delta) {
		this.zoom -= delta;
	}

	/**
	 * Java main
	 * 
	 * @param argv
	 */
	public static void main(String[] argv) {
		modelViewer = new JSONModelViewer();
		modelViewer.start();
	}

}
