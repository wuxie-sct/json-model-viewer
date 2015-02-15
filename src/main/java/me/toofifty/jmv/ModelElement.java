package me.toofifty.jmv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNodeReader;

/**
 * Holds all the information for one piece of a model.
 * Piece must be rendered through the CubeRenderer class.
 * 
 * @author Toofifty
 *
 */
public class ModelElement {

	// Main bounds
	private Vector3f from;
	private Vector3f to;

	// Rotations
	private Vector3f origin = new Vector3f(8F, 8F, 8F);
	private Axis axis;
	private float angle = 0;
	
	// Faces
	private HashMap<Dir, Face> faces = new HashMap<Dir, Face>();
	
	// Object name
	private String name;

	/**
	 * Create a new ModelElement with float values.
	 * 
	 * @param from x
	 * @param from y
	 * @param from z
	 * @param to x
	 * @param to y
	 * @param to z
	 */
	public ModelElement(float fx, float fy, float fz, float tx, float ty, float tz) {
		this.from = new Vector3f(fx, fy, fz);
		this.to = new Vector3f(tx, ty, tz);
	}

	/**
	 * Create a new ModelElement with Vectors.
	 * 
	 * @param from
	 * @param to
	 */
	public ModelElement(Vector3f from, Vector3f to) {
		this.from = from;
		this.to = to;
	}
	
	public ModelElement(JsonNode from, JsonNode to) {
		this.from = new Vector3f(from.get(0).floatValue(), 
				from.get(1).floatValue(), 
				from.get(2).floatValue());
		this.from = new Vector3f(to.get(0).floatValue(), 
				to.get(1).floatValue(), 
				to.get(2).floatValue());
	}

	/**
	 * Create a ModelElement from a JSON node.
	 * 
	 * @param rootNode
	 */
	public ModelElement(Model model, JsonNode rootNode) {
		final JsonNode fromNode = rootNode.path("from");
		final JsonNode toNode = rootNode.path("to");
		if (fromNode == null || toNode == null || fromNode.size() != 3 || toNode.size() != 3) {
			return;
		}
		this.from = v3(
			fromNode.get(0).floatValue(), 
			fromNode.get(1).floatValue(), 
			fromNode.get(2).floatValue()
		);
		this.to = v3(
			toNode.get(0).floatValue(), 
			toNode.get(1).floatValue(), 
			toNode.get(2).floatValue()
		);
		
		final JsonNode rotationNode = rootNode.path("rotation");
		if (rotationNode != null) {
			final JsonNode originNode = rotationNode.path("origin");
			if (originNode != null && originNode.size() == 3) {
				this.origin = v3(
					originNode.get(0).floatValue(),
					originNode.get(1).floatValue(),
					originNode.get(2).floatValue()
				);
			}
			
			final JsonNode axisNode = rotationNode.path("axis");
			if (axisNode != null && axisNode.toString() != "") {
				this.axis = getAxis(axisNode.textValue());
			}
			
			final JsonNode angleNode = rotationNode.path("angle");
			if (angleNode != null && angleNode.asInt() != 0) {
				this.angle = angleNode.floatValue();
			}
		}
		
		final JsonNode facesNode = rootNode.path("faces");
		if (facesNode != null) {
			Iterator<Entry<String, JsonNode>> facesIter = facesNode.fields();
			while (facesIter.hasNext()) {
				final Entry faceInfo = facesIter.next();
				final Dir faceDir = getDir(faceInfo.getKey().toString());
				final Face face = new Face(model, (JsonNode)faceInfo.getValue());
				this.faces.put(faceDir, face);
			}
		}
		
		final JsonNode commentNode = rootNode.path("__comment");
		if (commentNode != null && commentNode.toString() != "") {
			this.name = commentNode.toString();
		}
	}

	/**
	 * Rotate the piece with a specific origin.
	 * 
	 * @param origin
	 * @param axis
	 * @param angle
	 */
	public void rotate(Vector3f origin, Axis axis, float angle) {
		this.origin = origin;
		this.axis = axis;
		this.angle = angle;
	}

	/**
	 * Rotate the piece around the default origin [8, 8, 8]
	 * 
	 * @param axis
	 * @param angle
	 */
	public void rotate(Axis axis, float angle) {
		this.axis = axis;
		this.angle = angle;
	}
	
	/**
	 * Set (or overwrite) a face in the faces map.
	 * 
	 * @param dir
	 * @param face
	 */
	public void face(Dir dir, Face face) {
		this.faces.put(dir, face);
	}
	
	/**
	 * Set object name for use in GUI
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * From getter
	 * 
	 * @return from
	 */
	public Vector3f getFrom() {
		return from;
	}
	
	/**
	 * To getter
	 * 
	 * @return to
	 */
	public Vector3f getTo() {
		return to;
	}
	
	/**
	 * Origin getter
	 * 
	 * @return origin
	 */
	public Vector3f getOrigin() {
		return origin;
	}
	
	/**
	 * Axis getter
	 * 
	 * @return axis
	 */
	public Axis getAxis() {
		return axis;
	}
	
	/**
	 * Angle getter
	 * 
	 * @return angle
	 */
	public float getAngle() {
		return angle;
	}
	
	/**
	 * Face getter
	 * 
	 * @param dir
	 * @return face
	 */
	public Face getFace(Dir dir) {
		return faces.get(dir);
	}
	
	/**
	 * Get object name for GUI
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Shortcut Vector3f constructor
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static Vector3f v3(float x, float y, float z) {
		return new Vector3f(x, y, z);
	}

	/**
	 * Shortcut Vector2f constructor
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static Vector2f v2(float x, float y) {
		return new Vector2f(x, y);
	}

	protected class Face {

		// Main
		private Vector2f uv_from;
		private Vector2f uv_to;
		private String texture;
		private Dir cullface;
		private int rotation;
		
		private Model model;

		/**
		 * Create a new face with float values.
		 * 
		 * @param fu
		 * @param fv
		 * @param tu
		 * @param tv
		 * @param tex
		 */
		public Face(float fu, float fv, float tu, float tv, String tex) {
			this.uv_from = new Vector2f(fu, fv);
			this.uv_to = new Vector2f(tu, tv);
			this.texture = tex;
		}

		/**
		 * Create a new face with Vectors.
		 * 
		 * @param uv_from
		 * @param uv_to
		 * @param tex
		 */
		public Face(Vector2f uv_from, Vector2f uv_to, String tex) {
			this.uv_from = uv_from;
			this.uv_to = uv_to;
			this.texture = tex;
		}
		
		/**
		 * Create a face from a JSON node.
		 * 
		 * @param rootNode
		 */
		public Face(Model model, JsonNode rootNode) {
			final JsonNode uvNode = rootNode.path("uv");
			this.model = model;
			if (uvNode != null && uvNode.size() == 4) {
				this.uv_from = v2(uvNode.get(0).floatValue(), uvNode.get(1).floatValue());
				this.uv_to = v2(uvNode.get(2).floatValue(), uvNode.get(3).floatValue());
			} else {
				this.uv_from = v2(0, 0);
				this.uv_to = v2(16F, 16F);
			}
			
			final JsonNode textureNode = rootNode.path("texture");
			if (textureNode != null) {
				texture = textureNode.textValue();
				texture = texture.substring(1, texture.length());
			}
			
			final JsonNode cullfaceNode = rootNode.path("cullface");
			if (cullfaceNode != null && cullfaceNode.toString() != "") {
				cullface = getDir(cullfaceNode.toString());
			}
			
			final JsonNode rotationNode = rootNode.path("rotation");
			if (rotationNode != null) {
				rotation = rotationNode.asInt();
			}			
		}

		/**
		 * UV-From getter
		 * 
		 * @return uv_from + textureCoords
		 */
		public Vector2f getUVFrom(Vector2f textureCoords) {
			return v2(textureCoords.x + uv_from.x / model.getAtlas().width,
					textureCoords.y + uv_from.y / model.getAtlas().height);
		}
		
		/**
		 * UV-To getter
		 * 
		 * @return uv_to + textureCoords
		 */
		public Vector2f getUVTo(Vector2f textureCoords) {
			return v2(textureCoords.x + uv_to.x / model.getAtlas().width,
					textureCoords.y + uv_to.y / model.getAtlas().height);
		}
		
		/**
		 * Texture getter
		 * 
		 * @return texture name as string
		 */
		public String getTexture() {
			return texture;
		}

	}

	/**
	 * Rotation axis enum
	 * 
	 * @author Toofifty
	 *
	 */
	public enum Axis {
		X, Y, Z;
	}

	/**
	 * Face direction enum
	 * 
	 * @author Toofifty
	 *
	 */
	public enum Dir {
		UP, DOWN, NORTH, SOUTH, EAST, WEST;
	}


	/**
	 * Convert to enum Axis from string.
	 * 
	 * @param axis
	 * @return enum axis
	 * @throws Exception
	 */
	public Axis getAxis(String axis) {
		switch (axis.toLowerCase()) {
		case "x":
			return Axis.X;
		case "y":
			return Axis.Y;
		case "z":
			return Axis.Z;
		default:
			return null;
		}
	}
	
	/**
	 * Convert to enum Dir from string.
	 * 
	 * @param dir
	 * @return enum dir
	 * @throws Exception
	 */
	public Dir getDir(String dir) {
		switch (dir.toLowerCase()) {
		case "up":
			return Dir.UP;
		case "down":
			return Dir.DOWN;
		case "north":
			return Dir.NORTH;
		case "south":
			return Dir.SOUTH;
		case "east":
			return Dir.EAST;
		case "west":
			return Dir.WEST;
		default:
			return null;
		}
	}

}
