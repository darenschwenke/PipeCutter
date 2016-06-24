package com.kz.pipeCutter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.vecmath.Vector3d;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
//import org.jzy3d.plot3d.primitives.Point;
//import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.text.drawable.DrawableTextBitmap;

import com.kz.pipeCutter.ui.Settings;

public class Utils {
	ConcurrentHashMap<Integer, MyPickablePoint> points = null;
	HashMap<Integer, MySurface> surfaces = new HashMap<Integer, MySurface>();
	ConcurrentHashMap<Integer, MyEdge> edges = new ConcurrentHashMap<Integer, MyEdge>();
	ConcurrentHashMap<Integer, MyContinuousEdge> continuousEdges = new ConcurrentHashMap<Integer, MyContinuousEdge>();
	HashMap<String, MinYMaxY> minAndMaxY;
	ConcurrentHashMap<Integer, MyPickablePoint> origPoints = null;

	ArrayList<PickableDrawableTextBitmap> edgeTexts = null;
	ArrayList<DrawableTextBitmap> pointTexts = null;

	public float maxX = 0;
	public float minX = 0;
	public float maxY = 0;
	public float minY = 0;
	public float maxZ = 0;
	public float minZ = 0;

	static double Math_E = 0.0001;
	static double rotationAngleMin = 0.01;

	public MyPickablePoint createOrFindMyPickablePoint(int id, Coord3d coord, int inventorEdge) {
		MyPickablePoint mp = null;
		if (points == null)
			init();

		for (MyPickablePoint p : points.values()) {
			if (p.xyz.distance(coord) < Math_E) {
				mp = p;
				break;
			}
		}
		if (mp == null) {
			mp = new MyPickablePoint(id, coord, Color.BLACK, 4.0f, inventorEdge);
			points.put(id, mp);
		}

		if (maxX < coord.x)
			maxX = coord.x;
		if (maxY < coord.y)
			maxY = coord.y;
		if (maxZ < coord.z)
			maxZ = coord.z;
		if (minX > coord.x)
			minX = coord.x;
		if (minY > coord.y)
			minY = coord.y;
		if (minZ > coord.z)
			minZ = coord.z;

		return mp;
	}

	public void init() {
		points = new ConcurrentHashMap<Integer, MyPickablePoint>();
	}

	public ArrayList<Integer> calculateCutPoints(MyPickablePoint clickedPoint,
			ArrayList<MyPickablePoint> alAlreadyAddedPoints, boolean verticals) {
		// TODO Auto-generated method stub
		MyEdge edge = new MyEdge(-1, -1);

		MyPickablePoint tempPoint = findConnectedPoint(clickedPoint, alAlreadyAddedPoints, true);
		System.out.println("Point id: " + tempPoint.id);
		edge.addPoint(tempPoint.id);
		alAlreadyAddedPoints.add(tempPoint);
		while (tempPoint != clickedPoint && tempPoint != null) {
			tempPoint = findConnectedPoint(tempPoint, alAlreadyAddedPoints, true);
			if (tempPoint != null) {
				edge.addPoint(tempPoint.id);
				alAlreadyAddedPoints.add(tempPoint);
			}
		}
		edge.addPoint(clickedPoint.id);
		alAlreadyAddedPoints.add(clickedPoint);

		return edge.points;

	}

	public void establishNeighbourPoints() {
		for (MyPickablePoint p : points.values()) {
			p.neighbourPoints = findConnectedPoints(p, new ArrayList<MyPickablePoint>());
		}
	}

	public MyPickablePoint findConnectedPoint(MyPickablePoint point, ArrayList<MyPickablePoint> alreadyAdded,
			boolean order) {
		MyPickablePoint ret = null;
		for (MyEdge edge : edges.values()) {
			int startInd, endInd;
			if (order) {
				startInd = 0;
				endInd = 1;
			} else {
				startInd = 1;
				endInd = 0;
			}

			if (edge.getPointByIndex(startInd).equals(point)) {
				if (!alreadyAdded.contains(edge.getPointByIndex(endInd))) {
					ret = edge.getPointByIndex(endInd);
					break;
				}
			} else if (edge.getPointByIndex(endInd).equals(point)) {
				if (!alreadyAdded.contains(edge.getPointByIndex(startInd))) {
					ret = edge.getPointByIndex(startInd);
					break;
				}
			}

		}
		return ret;
	}

	public ArrayList<MyPickablePoint> findConnectedPoints(MyPickablePoint point,
			ArrayList<MyPickablePoint> alreadyAdded) {
		ArrayList<MyPickablePoint> ret = new ArrayList<MyPickablePoint>();
		for (MyEdge edge : edges.values()) {
			if (edge.getPointByIndex(0).distance(point) < Math_E) {
				if (!alreadyAdded.contains(edge.points.get(1))) {
					ret.add(edge.getPointByIndex(1));
				}
			} else if (edge.getPointByIndex(1).distance(point) < Math_E) {
				if (!alreadyAdded.contains(edge.points.get(0))) {
					ret.add(edge.getPointByIndex(0));
				}
			}

		}
		return ret;
	}

	public void addEdge(MyEdge edge) {
		if (edges == null)
			edges = new ConcurrentHashMap<Integer, MyEdge>();
		edges.put(edge.edgeNo, edge);
	}

	public static ArrayList<Coord3d> CalculateLineLineIntersection(Coord3d line1Point1, Coord3d line1Point2,
			Coord3d line2Point1, Coord3d line2Point2) {
		// Algorithm is ported from the C algorithm of
		// Paul Bourke at
		// http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline3d/
		ArrayList<Coord3d> alRet = new ArrayList<Coord3d>();

		Coord3d resultSegmentPoint1 = new Coord3d();
		Coord3d resultSegmentPoint2 = new Coord3d();

		Coord3d p1 = line1Point1;
		Coord3d p2 = line1Point2;
		Coord3d p3 = line2Point1;
		Coord3d p4 = line2Point2;
		Coord3d p13 = p1.sub(p3);
		Coord3d p43 = p4.sub(p3);

		if (p43.magSquared() < Math_E) {
			return null;
		}
		Coord3d p21 = p2.sub(p1);
		if (p21.magSquared() < Math_E) {
			return null;
		}

		double d1343 = p13.x * (double) p43.x + (double) p13.y * p43.y + (double) p13.z * p43.z;
		double d4321 = p43.x * (double) p21.x + (double) p43.y * p21.y + (double) p43.z * p21.z;
		double d1321 = p13.x * (double) p21.x + (double) p13.y * p21.y + (double) p13.z * p21.z;
		double d4343 = p43.x * (double) p43.x + (double) p43.y * p43.y + (double) p43.z * p43.z;
		double d2121 = p21.x * (double) p21.x + (double) p21.y * p21.y + (double) p21.z * p21.z;

		double denom = d2121 * d4343 - d4321 * d4321;
		if (Math.abs(denom) < Math_E) {
			return null;
		}
		double numer = d1343 * d4321 - d1321 * d4343;

		double mua = numer / denom;
		double mub = (d1343 + d4321 * (mua)) / d4343;

		resultSegmentPoint1.x = (float) (p1.x + mua * p21.x);
		resultSegmentPoint1.y = (float) (p1.y + mua * p21.y);
		resultSegmentPoint1.z = (float) (p1.z + mua * p21.z);
		resultSegmentPoint2.x = (float) (p3.x + mub * p43.x);
		resultSegmentPoint2.y = (float) (p3.y + mub * p43.y);
		resultSegmentPoint2.z = (float) (p3.z + mub * p43.z);

		alRet.add(resultSegmentPoint1);
		alRet.add(resultSegmentPoint2);

		return alRet;
	}

	static boolean isBetween(Coord3d a, Coord3d b, Coord3d c) {
		if (a.distance(c) + c.distance(b) == a.distance(b))
			return true;

		return false;

	}

	static boolean isBetween2(Coord3d a, Coord3d b, Coord3d c) {
		// if c is between a and b ?
		// http://stackoverflow.com/questions/328107/how-can-you-determine-a-point-is-between-two-other-points-on-a-line-segment
		// double crossproduct = (c.y - a.y) * (b.x - a.x) - (c.x - a.x) * (b.y
		// -
		// a.y);
		double ac_d[] = { c.x - a.x, c.y - a.y, c.z - a.z };
		Vector3d ac = new Vector3d(ac_d);
		double ab_d[] = { b.x - a.x, b.y - a.y, b.z - a.z };
		Vector3d ab = new Vector3d(ab_d);

		Vector3d product = new Vector3d();
		product.cross(ac, ab);

		if (product.length() > Math_E)
			return false;

		double kac = ab.dot(ac);
		double kab = ab.dot(ab);

		if (kac < 0)
			// KAC<0 the point is not between A and B.
			return false;
		else if (kac > kab)
			// KAC>KAB the point is not between A and B.
			return false;
		else if (kac < Math_E && kac > 0)
			// KAC=0 : the points C and A coincide.
			return true;
		else if (kac == kab)
			// KAC=KAB : the points C and B coincide.
			return true;

		// 0<KAC<KAB : the point C belongs on the line segment S.
		return true;
	}

	public void markOuterPoints() {
		// TODO Auto-generated method stub

		float[] result2 = { 0, 0, 0 };

		Coord3d a2 = new Coord3d(1, 2, 0);
		Coord3d b2 = new Coord3d(1, 1, 0);
		Coord3d c2 = new Coord3d(0, 0, 0);
		Coord3d d2 = new Coord3d(0.5, 0, 0);

		ArrayList<Coord3d> alCrossPoints2 = Utils.CalculateLineLineIntersection(a2, b2, c2, d2);

		// calculate edge surfaces
		List<MySurface> surfacesSortedByCenterY = new ArrayList<MySurface>(SurfaceDemo.instance.utils.surfaces.values());
		Collections.sort(surfacesSortedByCenterY, new MySurfaceYComparator());
		MySurface rightMostSurf = surfacesSortedByCenterY.get(0);
		MySurface leftMostSurf = surfacesSortedByCenterY.get(surfacesSortedByCenterY.size() - 1);

		ArrayList<MyPickablePoint> outerPoints = new ArrayList<MyPickablePoint>();
		float sumAngle = 0;
		for (int i = 0; i < 4; i++) {
			List<MyPickablePoint> pointsSortedByZ = new ArrayList<MyPickablePoint>(
					SurfaceDemo.instance.utils.points.values());
			Collections.sort(pointsSortedByZ, new MyPickablePointZComparator());
			float topZ = pointsSortedByZ.get(pointsSortedByZ.size() - 1).xyz.z;
			if (topZ < 0)
				topZ = pointsSortedByZ.get(0).xyz.z;
			System.out.println("sumAngle: " + sumAngle + "\n top z: " + topZ);
			float delta = (float) Math_E;
			for (MyPickablePoint p : SurfaceDemo.instance.utils.points.values()) {
				if (Math.abs(p.xyz.z - topZ) < delta) {
					// if it's point from vertical edge - don't add it.
					//
					outerPoints.add(p);
					// p.setColor(Color.BLACK);
					p.setWidth(5);
				}
			}
			double angle = 90.0d;
			SurfaceDemo.instance.utils.rotatePoints(angle, true);
			SurfaceDemo.instance.getChart().render();
			sumAngle = (float) (sumAngle + angle);
		}
	}

	public boolean isAlreadyAdded(MyPickablePoint myPoint, ArrayList<MyPickablePoint> alreadyAddedPoints) {
		for (MyPickablePoint p1 : alreadyAddedPoints) {
			if (myPoint.xyz.distance(p1.xyz) == 0) {
				return true;
			}
		}
		return false;
	}

	public String getMinYMaxYKeyFor(MyPickablePoint p) {
		// TODO Auto-generated method stub
		return "x=" + p.xyz.x + " z=" + p.xyz.z;
	}

	public class MinYMaxY {
		public double minY;
		public double maxY;

		public MinYMaxY() {
			minY = Double.MAX_VALUE;
			maxY = Double.MIN_VALUE;
		}

		public String toString() {
			return "minY=" + this.minY + " maxY=" + maxY;
		}

	}

	public MyPickablePoint getPointByCoordinate(Coord3d coord) {
		for (MyPickablePoint point : this.points.values()) {
			if (point.xyz.distance(coord) == 0) {
				return point;
			}
		}
		return null;
	}

	public ArrayList<MyPickablePoint> findAllConnectedPoints(MyPickablePoint p, ArrayList<MyPickablePoint> points) {
		for (MyPickablePoint p1 : p.neighbourPoints) {
			if (!points.contains(p1)) {
				points.add(p1);
				findAllConnectedPoints(p1, points);
			}
		}
		return points;
	}

	public void writeGcodeToFile(String gcode) {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("prog.gcode", true)))) {
			out.println(gcode);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String coordinateToGcode(MyPickablePoint point) {
		float offset = Float.valueOf(Settings.instance.getSetting("plasma_pierce_offset_mm"));
		boolean moveRelatively = false;
		Float x, y, z;
		if (moveRelatively) {
			x = Cylinder.offsetX - point.xyz.x;
			y = point.xyz.y + Cylinder.offsetY;
			z = point.xyz.z + Cylinder.offsetZ + offset;
		} else {
			x = point.xyz.x;
			y = point.xyz.y;
			z = point.xyz.z;
		}
		return String.format(java.util.Locale.US, "X%.3f Y%.3f Z%.3f", x, y, z);
	}

	public String coordinateToGcode(Coord3d coord, float offset) {
		boolean moveRelatively = false;
		Float x, y, z;
		if (moveRelatively) {
			x = Cylinder.offsetX - coord.x;
			y = coord.y + Cylinder.offsetY;
			z = coord.z + Cylinder.offsetZ + offset;
		} else {
			x = coord.x;
			y = coord.y;
			z = coord.z;
		}
		return String.format(java.util.Locale.US, "X%.3f Y%.3f Z%.3f", x, y, z);
	}

	public Coord3d rotate(double x, double y, double z, double angle, double axisX, double axisY, double axisZ) {
		Rotation rot = new Rotation(new Vector3D(axisX, axisY, axisZ), angle);
		Vector3D rotated = rot.applyTo(new Vector3D(x, y, z));
		return new Coord3d(rotated.getX(), rotated.getY(), rotated.getZ());
	}

	public void rotatePoints(double angle, boolean slow) {
		rotatePoints(angle, slow, true);
	}

	public void rotatePoints(double angle, boolean slow, boolean angleInDelta) {
		double value;
		if (!slow) {
			if (angleInDelta)
				value = Double.valueOf(SurfaceDemo.instance.angleTxt) + angle;
			else
				value = angle;

			double[] zAxisDouble = { 0.0d, 1.0d, 0.0d };
			Vector3D zAxis = new Vector3D(zAxisDouble);
			Rotation rotZ = new Rotation(zAxis, Math.toRadians(value));
			for (MyPickablePoint point : SurfaceDemo.instance.utils.origPoints.values()) {
				double[] myPointDouble = { point.getX(), point.getY(), point.getZ() };
				Vector3D myPoint = new Vector3D(myPointDouble);
				Vector3D result = rotZ.applyTo(myPoint);
				points.get(point.id).xyz.set(Double.valueOf(result.getX()).floatValue(),
						Double.valueOf(result.getY()).floatValue(), Double.valueOf(result.getZ()).floatValue());
			}
			for (MyEdge edge : continuousEdges.values()) {
				edge.calculateCenter();
			}
		} else {

			int noSteps = 10;
			for (int i = 0; i <= noSteps; i++) {
				if (angleInDelta)
					value = Double.valueOf(SurfaceDemo.instance.angleTxt) + angle * i / noSteps;
				else
					value = angle * i / noSteps;

				double[] zAxisDouble = { 0.0d, 1.0d, 0.0d };
				Vector3D zAxis = new Vector3D(zAxisDouble);
				Rotation rotZ = new Rotation(zAxis, Math.toRadians(value));
				for (MyPickablePoint point : SurfaceDemo.instance.utils.origPoints.values()) {
					double[] myPointDouble = { point.getX(), point.getY(), point.getZ() };
					Vector3D myPoint = new Vector3D(myPointDouble);
					Vector3D result = rotZ.applyTo(myPoint);
					points.get(point.id).xyz.set(Double.valueOf(result.getX()).floatValue(),
							Double.valueOf(result.getY()).floatValue(), Double.valueOf(result.getZ()).floatValue());
				}
				for (MyEdge edge : continuousEdges.values()) {
					edge.calculateCenter();
				}
				float val = (float) (value);
				SurfaceDemo.instance.calculateRotationPoint(val);
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (SurfaceDemo.instance.NUMBER_EDGES)
			for (MyEdge edge : SurfaceDemo.instance.utils.edges.values()) {
				edge.calculateCenter();
			}

		// if((float)newValue == 360.0f)
		// {
		// newValue = 0.0f;
		// }
		if (angleInDelta) {
			value = Double.valueOf(SurfaceDemo.instance.angleTxt);
			double newValue = value + angle;
			SurfaceDemo.instance.angleTxt = Double.valueOf(newValue).toString();
		} else
			SurfaceDemo.instance.angleTxt = Double.valueOf(angle).toString();

		float val = Float.valueOf(SurfaceDemo.instance.angleTxt);
		SurfaceDemo.instance.calculateRotationPoint(val);
	}

	public void calculateContinuousEdges() {
		int edgeNo = 1;
		continuousEdges = new ConcurrentHashMap<Integer, MyContinuousEdge>();
		for (MyPickablePoint point : points.values()) {
			if (point.continuousEdgeNo == -1) {
				ArrayList<MyPickablePoint> pointsOfEdgeAl = findAllConnectedPoints(point, new ArrayList<MyPickablePoint>());
				MyContinuousEdge edge = new MyContinuousEdge(edgeNo, -1);
				for (MyPickablePoint edgePoint : pointsOfEdgeAl) {
					edge.addPoint(edgePoint.id);
					points.get(edgePoint.id).continuousEdgeNo = edgeNo;
				}
				continuousEdges.put(edgeNo, edge);
				edgeNo++;
			}
		}

		// calculate type of Edge START MIDDLE or END
		ArrayList<MyContinuousEdge> sortedContinuousEdgeList = new ArrayList(continuousEdges.values());
		Collections.sort(sortedContinuousEdgeList, new MyEdgeYComparator());

		sortedContinuousEdgeList.get(0).edgeType = MyContinuousEdge.EdgeType.START;
		sortedContinuousEdgeList.get(sortedContinuousEdgeList.size() - 1).edgeType = MyContinuousEdge.EdgeType.END;

	}

	public org.jzy3d.plot3d.primitives.Point calculateOffsetPoint(MyPickablePoint point) {
		org.jzy3d.plot3d.primitives.Point ret = new org.jzy3d.plot3d.primitives.Point();

		MyContinuousEdge continuousEdge = continuousEdges.get(point.continuousEdgeNo);

		System.out.println(continuousEdge.edgeType);
		Vector3D result = null;
		if (continuousEdge.edgeType == MyContinuousEdge.EdgeType.ONPIPE) {
			int index = continuousEdge.points.indexOf(point.id);
			int prevIndex = -1;
			int nextIndex = -1;

			if (index == 0)
				prevIndex = continuousEdge.points.size() - 1;
			else
				prevIndex = index - 1;

			if (index == continuousEdge.points.size() - 1)
				nextIndex = 0;
			else
				nextIndex = index + 1;

			MyPickablePoint prevPoint = continuousEdge.getPointByIndex(prevIndex);
			MyPickablePoint nextPoint = continuousEdge.getPointByIndex(nextIndex);
			System.out.println(prevPoint.id + " " + point.id + " " + nextPoint.id);

			Vector3D centerVec = new Vector3D(continuousEdge.center.x, continuousEdge.center.y, continuousEdge.center.z);
			Vector3D vecPrevPoint = new Vector3D(prevPoint.xyz.x, prevPoint.xyz.y, prevPoint.xyz.z);
			Vector3D vecNextPoint = new Vector3D(nextPoint.xyz.x, nextPoint.xyz.y, nextPoint.xyz.z);
			Vector3D vecPoint = new Vector3D(point.xyz.x, point.xyz.y, point.xyz.z);
			try {
				Plane plane = getPlaneForPoint(point);
				double vectorCrossLength = plane.getNormal().crossProduct(new Vector3D(0f, 1.0f, 0.0f)).getNorm();
				System.out.println(vectorCrossLength);
				Vector3D pointOnPlane = (Vector3D) plane.project(centerVec);

				if (Math.abs(vectorCrossLength) < Math_E || Math.abs(1 - vectorCrossLength) < Math_E) {
					System.out.println("is 0 or is 1");
					Vector3D vecA = vecPrevPoint.subtract(vecPoint);
					Vector3D vecB = vecNextPoint.subtract(vecPoint);
					Rotation rotationP = new Rotation(plane.getNormal(), Math.PI / 2);
					Rotation rotationN = new Rotation(plane.getNormal(), -Math.PI / 2);
					Vector3D rotatedA = rotationP.applyTo(vecA).normalize();
					Vector3D rotatedB = rotationN.applyTo(vecB).normalize();
					Vector3D vecAoffset = vecA.add(rotatedA.scalarMultiply(SurfaceDemo.instance.plasmaKerfOffset));
					Vector3D vecBoffset = vecB.add(rotatedB.scalarMultiply(SurfaceDemo.instance.plasmaKerfOffset));
					Line lineA = new Line(vecPrevPoint.add(vecAoffset), vecPoint.add(vecAoffset), 0.0001);
					Line lineB = new Line(vecNextPoint.add(vecBoffset), vecPoint.add(vecBoffset), 0.0001);
					Vector3D intersect = lineA.intersection(lineB);
					ret.xyz.x = (float) intersect.getX();
					ret.xyz.y = (float) intersect.getY();
					ret.xyz.z = (float) intersect.getZ();
				} else {
					System.out.println("is between 0 and 1");
					// subtract y from center of edge to find signum of y to be added
					float deltaY = point.getY() - continuousEdge.center.y;
					ret.xyz.x = (float) point.getX();
					ret.xyz.y = (float) point.getY() - Math.signum(deltaY) * SurfaceDemo.instance.plasmaKerfOffset;
					ret.xyz.z = (float) point.getZ();
				}
			} catch (org.apache.commons.math3.exception.MathArithmeticException ex) {
				System.out.println("Middle point");
				// we will get plane from previous point.
				int prevPrevIndex = -1;
				if (prevIndex == 0)
					prevPrevIndex = continuousEdge.points.size() - 1;
				else
					prevPrevIndex = prevIndex - 1;
				MyPickablePoint prevPrevPoint = continuousEdge.getPointByIndex(prevPrevIndex);
				System.out.println("prevprevpoint=" + prevPrevPoint.id);
				Plane plane = getPlaneForPoint(prevPoint);

				Vector3D vecPrevPrevPoint = new Vector3D(prevPrevPoint.xyz.x, prevPrevPoint.xyz.y, prevPrevPoint.xyz.z);
				Vector3D vecA = vecPoint.subtract(vecPrevPoint);

				Rotation rotationP = new Rotation(plane.getNormal(), -Math.PI / 2);
				Vector3D rotatedA = rotationP.applyTo(vecA).normalize();
				result = vecPoint.add(rotatedA.scalarMultiply(SurfaceDemo.instance.plasmaKerfOffset));
				ret.xyz.x = (float) result.getX();
				ret.xyz.y = (float) result.getY();
				ret.xyz.z = (float) result.getZ();
			}
		} else {
			Vector3D vecPoint = new Vector3D(point.xyz.x, point.xyz.y, point.xyz.z);
			Vector3D vecOffset = new Vector3D(0, Math.signum(point.xyz.y) * SurfaceDemo.instance.plasmaKerfOffset, 0);
			result = vecPoint.add(vecOffset);
			ret.xyz.x = (float) result.getX();
			ret.xyz.y = (float) result.getY();
			ret.xyz.z = (float) result.getZ();
		}

		return ret;
	}

	public Plane getPlaneForPoint(MyPickablePoint point)
			throws org.apache.commons.math3.exception.MathArithmeticException {
		MyEdge continuousEdge = continuousEdges.get(point.continuousEdgeNo);

		int index = continuousEdge.points.indexOf(point.id);
		int prevIndex = -1;
		int nextIndex = -1;

		if (index == 0)
			prevIndex = continuousEdge.points.size() - 1;
		else
			prevIndex = index - 1;

		if (index == continuousEdge.points.size() - 1)
			nextIndex = 0;
		else
			nextIndex = index + 1;

		MyPickablePoint prevPoint = continuousEdge.getPointByIndex(prevIndex);
		MyPickablePoint nextPoint = continuousEdge.getPointByIndex(nextIndex);
		System.out.println(prevPoint.id + " " + point.id + " " + nextPoint.id);

		Vector3D centerVec = new Vector3D(continuousEdge.center.x, continuousEdge.center.y, continuousEdge.center.z);
		Vector3D vecPrevPoint = new Vector3D(prevPoint.xyz.x, prevPoint.xyz.y, prevPoint.xyz.z);
		Vector3D vecNextPoint = new Vector3D(nextPoint.xyz.x, nextPoint.xyz.y, nextPoint.xyz.z);
		Vector3D vecPoint = new Vector3D(point.xyz.x, point.xyz.y, point.xyz.z);
		Plane plane = new Plane(vecPoint, vecPrevPoint, vecNextPoint, 0.0001);
		return plane;
	}
}
