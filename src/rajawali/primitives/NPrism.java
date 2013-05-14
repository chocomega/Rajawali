package rajawali.primitives;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import rajawali.BaseObject3D;
import rajawali.math.Number3D;

/**
 * Basic primitive allowing for the creation of an n-sided regular
 * polygonal cone, as a frustum or to a point with a specified slant
 * angle or aspect ratio. The cone is created about the positive
 * y axis with the vanishing point at (0, height, 0).
 * 
 * NOTE: This still needs a lot of work. Normals and texture coordinates are not correct.
 * 
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class NPrism extends BaseObject3D {

	protected int mSideCount;
	protected double mRadiusBase;
	protected double mRadiusTop;
	protected double mMinorBase;
	protected double mMinorTop;
	protected double mHeight;
	protected double mEccentricity;
	
	private static final Number3D UP = new Number3D(0, 1, 0);
	private static final Number3D DOWN = new Number3D(0, -1, 0);

	/**
	 * Creates a terminated prism.
	 * 
	 * @param sides Integer number of sides to the prism.
	 * @param radius Double the radius of the base.
	 * @param height Double the height of the prism.
	 */
	public NPrism(int sides, double radius, double height) {
		this(sides, 0, radius, height);
	}
	
	/**
	 * Creates a frustum like prism.
	 * 
	 * @param sides Integer number of sides to the prism.
	 * @param radiusTop Double the radius of the top.
	 * @param radiusBase Double the radius of the base.
	 * @param height Double the height of the prism.
	 */
	public NPrism(int sides, double radiusTop, double radiusBase, double height) {
		this(sides, radiusTop, radiusBase, 0.0, height);
	}
	
	/**
	 * Creates a frustum like prism with elliptical base rather than circular. 
	 * The major axis is equivalent to the radius specified and the minor axis
	 * is computed from the eccentricity.
	 * 
	 * @param sides Integer number of sides to the prism.
	 * @param radiusTop Double the radius of the top.
	 * @param radiusBase Double the radius of the base.
	 * @param eccentricity Double the eccentricity of the ellipse.
	 * @param height Double the height of the prism.
	 */
	public NPrism(int sides, double radiusTop, double radiusBase, double eccentricity, double height) {
		if (sides < 3) throw new IllegalArgumentException("Prisms must have at least 3 sides!");
		if ((eccentricity < 0) || (eccentricity >= 1)) throw new IllegalArgumentException("Eccentricity must be in the range [0,1)");
		mSideCount = sides;
		mEccentricity = eccentricity;
		mRadiusTop = radiusTop;
		mMinorTop = calculateMinorAxis(mRadiusTop);
		mRadiusBase = radiusBase;
		mMinorBase = calculateMinorAxis(mRadiusBase);
		mHeight = height;
		init(false);
	}
	
	protected NPrism() {
		
	}
	
	protected double calculateMinorAxis(double major) {
		return Math.sqrt(Math.pow(major, 2.0)*(1 - Math.pow(mEccentricity, 2.0)));
	}

	protected void init(boolean update) {
		int vertex_count = 6*mSideCount + 2;
		int tri_count = 4*mSideCount;
		int top_center_index = 3*vertex_count - 6;
		int bottom_center_index = 3*vertex_count - 3;

		int offset = 0;
		int triangle = 0;
		int vertex = 0;
		float[] vertices = new float[3*vertex_count];
		float[] normals = new float[3*vertex_count];
		float[] texture = new float[2*vertex_count];
		float[] colors = new float[4*vertex_count];
		int[] indices = new int[3*tri_count];
		
		double angle_delta = 2*Math.PI/mSideCount;
		double angle = 0;
		double x = 1.0f, y = 1.0f, z = 1.0f;
		double u = 0, v = 1;
		double u_delta = 1.0/mSideCount;
		double MAG = Math.sqrt(Math.pow((mRadiusTop - mRadiusBase), 2.0) + Math.pow(mHeight, 2.0));
		Number3D temp_normal = new Number3D();
		if (mSideCount % 2 == 0) angle = angle_delta/2.0;

		//Populate the vertices
		int base_index;
		angle = (mSideCount % 2 == 0) ? angle = angle_delta/2.0 : 0;
		
		x = mRadiusTop*Math.cos(angle);
		z = mMinorTop*Math.sin(angle);
		for (int side = 0; side < mSideCount; ++side) {
			base_index = 3*triangle;
			//Handle the top
			y = mHeight/2;
			v = 0;
			temp_normal.x = (float) (mMinorTop*MAG*Math.cos(angle + angle_delta/2));
			temp_normal.y = (float) MAG;
			temp_normal.z = (float) (mRadiusTop*MAG*Math.sin(angle + angle_delta/2));
			temp_normal.normalize();
			temp_normal.z = -temp_normal.z;

			vertices[offset] = (float) x;
			texture[2*vertex] = (float) u;
			texture[2*vertex+1] = (float) v;
			normals[offset++] = temp_normal.x;
			vertices[offset] = (float) y;
			normals[offset++] = temp_normal.y;
			vertices[offset] = (float) z;
			normals[offset++] = temp_normal.z;
			indices[base_index+2] = vertex++;

			y = -mHeight/2;
			v = 1;
			x = mRadiusBase*Math.cos(angle);
			z = mMinorBase*Math.sin(angle);
			vertices[offset] = (float) x;
			texture[2*vertex] = (float) u;
			texture[2*vertex+1] = (float) v;
			normals[offset++] = temp_normal.x;
			vertices[offset] = (float) y;
			normals[offset++] = temp_normal.y;
			vertices[offset] = (float) z;
			normals[offset++] = temp_normal.z;
			indices[base_index+1] = vertex++;

			angle += angle_delta;
			u += u_delta;
			x = mRadiusBase*Math.cos(angle);
			z = mMinorBase*Math.sin(angle);
			vertices[offset] = (float) x;
			texture[2*vertex] = (float) u;
			texture[2*vertex+1] = (float) v;
			normals[offset++] = temp_normal.x;
			vertices[offset] = (float) y;
			normals[offset++] = temp_normal.y;
			vertices[offset] = (float) z;
			normals[offset++] = temp_normal.z;
			indices[base_index] = vertex++;
			++triangle;
			base_index = 3*triangle;
			y = mHeight/2;
			v = 0;
			x = mRadiusTop*Math.cos(angle);
			z = mMinorTop*Math.sin(angle);
			vertices[offset] = (float) x;
			texture[2*vertex] = (float) u;
			texture[2*vertex+1] = (float) v;
			normals[offset++] = temp_normal.x;
			vertices[offset] = (float) y;
			normals[offset++] = temp_normal.y;
			vertices[offset] = (float) z;
			normals[offset++] = temp_normal.z;
			indices[base_index+2] = vertex - 3;
			indices[base_index+1] = vertex - 1;
			indices[base_index] = vertex++;
			++triangle;
		}

		int offset_holder = offset;
		//Add in the top center
		offset = top_center_index;
		vertices[offset] = 0.0f;
		normals[offset++] = UP.x;
		vertices[offset] = (float) mHeight/2;
		normals[offset++] = UP.y;
		vertices[offset] = 0.0f;
		normals[offset++] = UP.z;
		texture[12*mSideCount] = 0.5f;
		texture[12*mSideCount+1] = 0.5f;
		//Add in the base center
		offset = bottom_center_index;
		vertices[offset] = 0.0f;
		normals[offset++] = DOWN.x;
		vertices[offset] = (float) -mHeight/2;
		normals[offset++] = DOWN.y;
		vertices[offset] = 0.0f;
		normals[offset++] = DOWN.z;
		texture[12*mSideCount+2] = 0.5f;
		texture[12*mSideCount+3] = 0.5f;

		offset = offset_holder;
		angle = (mSideCount % 2 == 0) ? angle = angle_delta/2.0 : 0;
		double minorTexture = calculateMinorAxis(1.0);
		y = mHeight/2;
		for (int side = 0; side < mSideCount; ++side) {
			base_index = 3*triangle;
			x = mRadiusTop*Math.cos(angle);
			z = mMinorTop*Math.sin(angle);
			u = Math.cos(angle);
			v = minorTexture*Math.sin(angle);
			//Handle the top
			vertices[offset] = (float) x;
			texture[2*vertex] = (float) u;
			texture[2*vertex+1] = (float) v;
			normals[offset++] = UP.x;
			vertices[offset] = (float) y;
			normals[offset++] = UP.y;
			vertices[offset] = (float) z;
			normals[offset++] = UP.z;

			indices[base_index+2] = vertex_count - 2;
			indices[base_index+1] = vertex;
			if (side == (mSideCount-1)) {
				indices[base_index] = 4*mSideCount;
			} else {
				indices[base_index] = ++vertex; //Moving to the next vertex
			}
			++triangle;
			angle += angle_delta;
		}

		angle = (mSideCount % 2 == 0) ? angle = angle_delta/2.0 : 0;
		y = -mHeight/2;
		for (int side = 0; side < mSideCount; ++side) {
			base_index = 3*triangle;
			x = mRadiusBase*Math.cos(angle);
			z = mMinorBase*Math.sin(angle);
			u = Math.cos(angle);
			v = -minorTexture*Math.sin(angle);
			//Handle the bottom
			vertices[offset] = (float) x;
			texture[2*vertex] = (float) u;
			texture[2*vertex+1] = (float) v;
			normals[offset++] = DOWN.x;
			vertices[offset] = (float) y;
			normals[offset++] = DOWN.y;
			vertices[offset] = (float) z;
			normals[offset++] = DOWN.z;

			indices[base_index+2] = ++vertex;
			indices[base_index+1] = vertex_count - 1;
			if (side == (mSideCount-1)) {
				indices[base_index] = 5*mSideCount;
			} else {
				indices[base_index] = indices[base_index+2] + 1;
			}
			angle += angle_delta;
			++triangle;
		}

		//Populate the colors
		for (int i = 0, j = 4*vertex_count; i < j; ++i) {
			colors[i] = 1.0f;
		}

		if (update) {
			updateBufferData(vertices, normals, texture, colors, indices);			
		} else {
			mGeometry.setData(vertices, normals, texture, colors, indices);
		}
	}
	
	protected void updateBufferData(float[] vertices, float[] normals, float[] texture, 
			float[] colors, int[] indices) {
		mGeometry.setVertices(vertices, true);
		mGeometry.setNormals(normals);
		
		/*FloatBuffer buffer = mGeometry.getVertices();
		buffer.clear(); buffer.put(vertices);
		mGeometry.changeBufferData(mGeometry.getVertexBufferInfo(), buffer, 0);
		buffer = mGeometry.getNormals();
		buffer.clear(); buffer.put(normals);
		buffer.rewind();
		mGeometry.changeBufferData(mGeometry.getVertexBufferInfo(), buffer, 0);
		buffer = mGeometry.getTextureCoords();
		buffer.clear(); buffer.put(texture);
		buffer.rewind();
		mGeometry.changeBufferData(mGeometry.getVertexBufferInfo(), buffer, 0);
		buffer = mGeometry.getColors();
		buffer.clear(); buffer.put(colors);
		buffer.rewind();
		mGeometry.changeBufferData(mGeometry.getVertexBufferInfo(), buffer, 0);
		Buffer indices_buffer = mGeometry.getIndices();
		indices_buffer.clear();
		if (mGeometry.areOnlyShortBuffersSupported()) {
			ShortBuffer shortBuffer = (ShortBuffer) indices_buffer;
			int length = indices.length;
			short[] shortIndices = new short[length];
			for (int i = 0; i < length; ++i) {
				shortIndices[i] = (short) indices[i];
			}
			shortBuffer.put(shortIndices);
			shortBuffer.rewind();
			mGeometry.changeBufferData(mGeometry.getIndexBufferInfo(), shortBuffer, 0);
		} else {
			IntBuffer intBuffer = (IntBuffer) indices_buffer;
			intBuffer.put(indices);
			intBuffer.rewind();
			mGeometry.changeBufferData(mGeometry.getIndexBufferInfo(), intBuffer, 0);
		}*/
	}
}