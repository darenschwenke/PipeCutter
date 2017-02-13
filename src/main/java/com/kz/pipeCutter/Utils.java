package com.kz.pipeCutter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.vecmath.Point3d;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.transform.Rotate;
import org.jzy3d.plot3d.transform.Transform;

import com.kz.pipeCutter.ui.Settings;

public class Utils {
	ConcurrentHashMap<Integer, MyPickablePoint> points = null;
	HashMap<Integer, MySurface> surfaces = new HashMap<Integer, MySurface>();
	public ConcurrentHashMap<Integer, MyEdge> edges = new ConcurrentHashMap<Integer, MyEdge>();
	public ConcurrentHashMap<Integer, MyContinuousEdge> continuousEdges = new ConcurrentHashMap<Integer, MyContinuousEdge>();
	HashMap<String, MinYMaxY> minAndMaxY;
	ConcurrentHashMap<Integer, MyPickablePoint> origPoints = null;

	// ArrayList<PickableDrawableTextBitmap> edgeTexts = null;
	// ArrayList<DrawableTextBitmap> pointTexts = null;
	MyComposite edgeTexts = null;
	MyComposite pointTexts = null;

	public double maxX = 0;
	public double minX = 0;
	public double maxY = 0;
	public double minY = 0;
	public double maxZ = 0;
	public double minZ = 0;

	public double maxEdge = 0;
	public Coord3d previousPoint;
	float previousAngle;
	// public MyEdge previousEdge;
	private int previousPointId;

	static double Math_E = 0.0001;
	static double rotationAngleMin = 0.0001;

	public MyPickablePoint createOrFindMyPickablePoint(int id, Point3d p, int inventorEdge) {
		MyPickablePoint mp = null;
		if (points == null)
			init();

		for (MyPickablePoint p1 : points.values()) {
			if (p1.distance(p) < Math_E) {
				mp = p1;
				break;
			}
		}
		if (mp == null) {
			mp = new MyPickablePoint(id, p, Color.BLACK, 4.0f, inventorEdge);
			points.put(id, mp);
		}

		if (maxX < p.x)
			maxX = p.x;
		if (maxY < p.y)
			maxY = p.y;
		if (maxZ < p.z)
			maxZ = p.z;
		if (minX > p.x)
			minX = p.x;
		if (minY > p.y)
			minY = p.y;
		if (minZ > p.z)
			minZ = p.z;

		return mp;
	}

	public void init() {
		points = new ConcurrentHashMap<Integer, MyPickablePoint>();
	}

	public ArrayList<Integer> calculateCutPoints(MyPickablePoint clickedPoint, ArrayList<Integer> alAlreadyAddedPoints, boolean verticals) {
		// TODO Auto-generated method stub
		MyEdge edge = new MyEdge(-1, -1);

		MyPickablePoint tempPoint = findConnectedPoint(clickedPoint, alAlreadyAddedPoints, true);
		System.out.println("Point id: " + tempPoint.id);
		edge.addPoint(tempPoint.id);
		alAlreadyAddedPoints.add(tempPoint.id);
		while (tempPoint != clickedPoint && tempPoint != null) {
			tempPoint = findConnectedPoint(tempPoint, alAlreadyAddedPoints, true);
			if (tempPoint != null) {
				edge.addPoint(tempPoint.id);
				alAlreadyAddedPoints.add(tempPoint.id);
			}
		}
		edge.addPoint(clickedPoint.id);
		alAlreadyAddedPoints.add(clickedPoint.id);

		return edge.points;

	}

	public void establishNeighbourPoints() {
		for (MyPickablePoint p : points.values()) {
			p.neighbourPoints = findConnectedPoints(p, new ArrayList<MyPickablePoint>());
		}

	}

	public void establishRighMostAndLeftMostPoints() {
		ArrayList<MyPickablePoint> sortedList = new ArrayList<MyPickablePoint>(points.values());
		Collections.sort(sortedList, new MyPickablePointYComparator());

		// SurfaceDemo.getInstance().utils.establishNeighbourPoints();
		MyPickablePoint firstOuterPoint = sortedList.get(0);
		MyPickablePoint lastOuterPoint = sortedList.get(sortedList.size() - 1);

		ArrayList<Integer> firstPoints = SurfaceDemo.getInstance().utils.findAllConnectedPoints(firstOuterPoint, new ArrayList<Integer>());
		Iterator<Integer> it = firstPoints.iterator();
		while (it.hasNext()) {
			Integer pointId = it.next();
			MyPickablePoint point = points.get(pointId);
			point.setFirstOrLast(MyPickablePoint.FirstOrLast.FIRST);
		}
		ArrayList<Integer> lastPoints = SurfaceDemo.getInstance().utils.findAllConnectedPoints(lastOuterPoint, new ArrayList<Integer>());
		it = lastPoints.iterator();
		while (it.hasNext()) {
			Integer pointId = it.next();
			MyPickablePoint point = points.get(pointId);
			point.setFirstOrLast(MyPickablePoint.FirstOrLast.LAST);
		}

	}

	public MyPickablePoint findConnectedPoint(MyPickablePoint point, ArrayList<Integer> alreadyAdded, boolean direction) {

		MyContinuousEdge contEdge = continuousEdges.get(point.continuousEdgeNo);
		int index = contEdge.points.indexOf(point.id);

		int increment = 0;
		if (direction)
			increment = 1;
		else
			increment = -1;

		int i = index;
		i = i + increment;
		if (i == contEdge.points.size())
			i = 0;
		if (i == -1)
			i = contEdge.points.size() - 1;

		if (!alreadyAdded.contains(contEdge.points.get(i)))
			return points.get(contEdge.points.get(i));

		return null;

		/*
		 * while (i != index) { if (!alreadyAdded.contains(contEdge.points.get(i)))
		 * { // go through edges and see if any of them is connecting those 2 points
		 * for (MyEdge edge : contEdge.connectedEdges) { if (direction) if
		 * (edge.points.get(0) == point.id && edge.points.get(1) ==
		 * (contEdge.points.get(i))) { // yes there is an edge connecting those 2
		 * points return points.get(contEdge.points.get(i)); } } } i = i +
		 * increment; if (i == contEdge.points.size()) i = 0; if (i == -1) i =
		 * contEdge.points.size() - 1; } return null;
		 */
	}

	public ArrayList<MyPickablePoint> findConnectedPoints(MyPickablePoint point, ArrayList<MyPickablePoint> alreadyAdded) {
		ArrayList<MyPickablePoint> ret = new ArrayList<MyPickablePoint>();
		for (MyEdge edge : edges.values()) {
			if (edge.getPointByIndex(0).distance(point.point) < Math_E) {
				if (!alreadyAdded.contains(edge.points.get(1))) {
					ret.add(edge.getPointByIndex(1));
				}
			} else if (edge.getPointByIndex(1).distance(point.point) < Math_E) {
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

	public static ArrayList<Coord3d> CalculateLineLineIntersection(Coord3d line1Point1, Coord3d line1Point2, Coord3d line2Point1, Coord3d line2Point2) {
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
			List<MyPickablePoint> pointsSortedByZ = new ArrayList<MyPickablePoint>(SurfaceDemo.instance.utils.points.values());
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

	public ArrayList<Integer> findAllConnectedPoints(MyPickablePoint p, ArrayList<Integer> points) {
		for (MyPickablePoint p1 : p.neighbourPoints) {
			if (!points.contains(p1.id)) {
				points.add(p1.id);
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

	public String coordinateToGcode(MyPickablePoint p, Vector3D kerfOffset) {
		return coordinateToGcode(p, 0, false, kerfOffset);
	}

	public String coordinateToGcode(MyPickablePoint p, float zOffset, boolean slow, Vector3D kerfOffset) {
		// G93.1
		// http://www.eng-tips.com/viewthread.cfm?qid=200454

		Double x, y, z;
		String ret;

		if (kerfOffset == null) {
			kerfOffset = new Vector3D(0, 0, 0);
		}

		x = p.getX() - kerfOffset.getX() / 2;
		y = p.getY() - kerfOffset.getY() / 2;
		z = p.getZ() - kerfOffset.getZ() / 2 + zOffset;

		float angle = Float.valueOf(SurfaceDemo.instance.angleTxt);

		MyEdge edge = null;
		float calcSpeed = 0;
		if (slow) {
			calcSpeed = SurfaceDemo.instance.g1Speed;
		} else
			calcSpeed = SurfaceDemo.instance.g0Speed;

		double length = 0;
		if (this.previousPointId > 0 && (p.id != this.previousPointId)) {
			edge = getEdgeFromTwoPoints(p, SurfaceDemo.instance.utils.points.get(this.previousPointId));
			if (edge != null) {
				length = edge.length;
				if (edge.edgeType == MyEdge.EdgeType.ONRADIUS) {
					float radius_of_edge = Float.valueOf(Settings.instance.getSetting("pipe_radius"));
					float maxRadius = (float) Math.sqrt(this.maxX * this.maxX + this.maxZ * this.maxZ);
					float s = (float) (maxRadius * Math.PI) * 1f;
					float arc_length = (float) (radius_of_edge * Math.PI / 2);
					float v = SurfaceDemo.instance.g1Speed * s / arc_length * 1f;
					float dv = v - SurfaceDemo.instance.g1Speed;
					float t = s / SurfaceDemo.instance.g1Speed;
					float a = 2 * dv / t;
					double currAngle = Math.atan2(p.getCoord().z, p.getCoord().x) * 180.0 / Math.PI;
					double maxAngle = Math.atan2(this.maxZ, (this.maxX - radius_of_edge)) * 180.0 / Math.PI;
					CutThread.instance.filletSpeed = Double.valueOf(v).floatValue();
					calcSpeed = CutThread.instance.filletSpeed;
				}
				if (edge.cutVelocity != 0)
					calcSpeed = edge.cutVelocity;
			}
		}
		double feed = 1;
		Coord3d p1 = new Coord3d(x, y, z);

		if (length == 0) {
			length = p1.distance(this.previousPoint);
		}

		if (length != 0) {
			feed = (calcSpeed) / length;
		} else
			feed = 10000;

		String edgeDescription = "";
		if (edge != null)
			edgeDescription = edge.edgeType + " no:" + edge.edgeNo; // + " length=" +
		// edge.length ;
		if (slow) {
			// Point point = calculateOffsetPoint(p);
			// float x1=point.xyz.x;
			// float y1=point.xyz.y;
			// float z1=point.xyz.z;

			ret = String.format(java.util.Locale.US, "%s X%.2f Y%.2f Z%.1f A%.4f B%.4f F%.1f (move length: %.1f speed:%.1f p:%d, e:%s)",
					(slow == true ? "G01" : "G01"), x, y, z, angle, angle, feed, length, calcSpeed, p.id, edgeDescription);

		} else
			ret = String.format(java.util.Locale.US, "%s X%.2f Y%.2f Z%.1f A%.4f B%.4f F%.1f (move length: %.1f speed:%.1f, e:%s)",
					(slow == true ? "G01" : "G01"), x, y, z, angle, angle, feed, length, calcSpeed, edgeDescription);

		this.previousPoint = p1;
		this.previousPointId = p.id;
		this.previousAngle = angle;

		return ret;
	}

	public Coord3d rotate(double x, double y, double z, double angle, double axisX, double axisY, double axisZ) {
		Rotation rot = new Rotation(new Vector3D(axisX, axisY, axisZ), angle);
		Vector3D rotated = rot.applyTo(new Vector3D(x, y, z));
		return new Coord3d(rotated.getX(), rotated.getY(), rotated.getZ());
	}

	public void rotatePoints(double angleDeg, boolean slow) {
		rotatePoints(angleDeg, slow, true);
	}

	public void rotatePoints(double angleDeg, boolean slow, boolean angleInDelta) {
		double value;
		if (SurfaceDemo.instance.utils.origPoints == null)
			return;
		if (!slow) {
			if (angleInDelta)
				value = Double.valueOf(SurfaceDemo.instance.angleTxt) + angleDeg;
			else
				value = angleDeg;

			double[] zAxisDouble = { 0.0d, 1.0d, 0.0d };
			Vector3D zAxis = new Vector3D(zAxisDouble);
			Rotation rotZ = new Rotation(zAxis, Math.toRadians(value));
			// Rotation rotZ1 = new Rotation(zAxis, Math.toRadians(angleDeg));
			for (MyPickablePoint point : SurfaceDemo.instance.utils.origPoints.values()) {
				double[] myPointDouble = { point.getX(), point.getY(), point.getZ() };
				Vector3D myPoint = new Vector3D(myPointDouble);
				Vector3D result = rotZ.applyTo(myPoint);
				points.get(point.id).setCoord(result.getX(), result.getY(), result.getZ());
			}

			// for (int j = 0; j < SurfaceDemo.instance.myTrail.size(); j++) {
			// Point p = (Point) SurfaceDemo.instance.myTrail.get(j);
			// Vector3D myPoint = new Vector3D(p.getCoord().x, p.getCoord().y,
			// p.getCoord().z);
			// Vector3D result = rotZ1.applyTo(myPoint);
			// p.setCoord(new Coord3d(result.getX(), result.getY(), result.getZ()));
			// }
			Transform myRot = new Transform(new Rotate(angleDeg, new Coord3d(0f, 1f, 0f)));
			SurfaceDemo.instance.myTrail.applyGeometryTransform(myRot);
			if (SurfaceDemo.instance.NUMBER_EDGES) {
				edgeTexts.applyGeometryTransform(myRot);
			}
			if (SurfaceDemo.instance.NUMBER_POINTS) {
				pointTexts.applyGeometryTransform(myRot);
			}
			for (MyEdge edge : continuousEdges.values()) {
				edge.calculateCenter();
			}
		} else {
			int noSteps = 10;
			Transform transf = new Transform(new Rotate(angleDeg / noSteps, new Coord3d(0, 1, 0)));
			for (int i = 0; i <= noSteps; i++) {
				if (angleInDelta)
					value = Double.valueOf(SurfaceDemo.instance.angleTxt) + angleDeg * i / noSteps;
				else
					value = angleDeg * i / noSteps;

				SurfaceDemo.instance.myTrail.applyGeometryTransform(transf);

				double[] zAxisDouble = { 0.0d, 1.0d, 0.0d };
				Vector3D zAxis = new Vector3D(zAxisDouble);
				Rotation rotZ = new Rotation(zAxis, Math.toRadians(value));
				Rotation rotZ1 = new Rotation(zAxis, Math.toRadians(angleDeg / noSteps));

				for (int j = 0; j < SurfaceDemo.instance.myTrail.size(); j++) {
					Point p = (Point) SurfaceDemo.instance.myTrail.get(j);
					Vector3D myPoint = new Vector3D(p.getCoord().x, p.getCoord().y, p.getCoord().z);
					Vector3D result = rotZ1.applyTo(myPoint);
					p.setCoord(new Coord3d(result.getX(), result.getY(), result.getZ()));
				}

				for (MyPickablePoint point : SurfaceDemo.instance.utils.origPoints.values()) {
					double[] myPointDouble = { point.getX(), point.getY(), point.getZ() };
					Vector3D myPoint = new Vector3D(myPointDouble);
					Vector3D result = rotZ.applyTo(myPoint);
					points.get(point.id).setCoord(result.getX(), result.getY(), result.getZ());
				}

				Transform myRot = new Transform(new Rotate(angleDeg, new Coord3d(0f, 1f, 0f)));
				SurfaceDemo.instance.myTrail.applyGeometryTransform(myRot);
				if (SurfaceDemo.instance.NUMBER_EDGES) {
					edgeTexts.applyGeometryTransform(myRot);
				}
				if (SurfaceDemo.instance.NUMBER_POINTS) {
					pointTexts.applyGeometryTransform(myRot);
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
		for (MyEdge edge : SurfaceDemo.instance.utils.edges.values()) {
			edge.calculateCenter();
		}

		// if((float)newValue == 360.0f)
		// {
		// newValue = 0.0f;
		// }
		if (angleInDelta) {
			value = Double.valueOf(SurfaceDemo.instance.angleTxt);
			double newValue = value + angleDeg;
			SurfaceDemo.instance.angleTxt = Double.valueOf(newValue).toString();
		} else
			SurfaceDemo.instance.angleTxt = Double.valueOf(angleDeg).toString();

		float val = Float.valueOf(SurfaceDemo.instance.angleTxt);
		SurfaceDemo.instance.calculateRotationPoint(val);
	}

	public void calculateContinuousEdges() {
		int edgeNo = 1;
		continuousEdges = new ConcurrentHashMap<Integer, MyContinuousEdge>();
		for (MyPickablePoint point : points.values()) {
			if (point.continuousEdgeNo == -1) {
				MyContinuousEdge contEdge = new MyContinuousEdge(edgeNo, -1);
				ArrayList<Integer> pointsOfEdgeAl = findAllConnectedPoints(point, new ArrayList<Integer>());

				int i = 0;
				int j = 0;
				while (i < pointsOfEdgeAl.size()) {

					MyPickablePoint edgePoint1 = points.get(pointsOfEdgeAl.get(i));

					j = i + 1;
					if (i == pointsOfEdgeAl.size() - 1)
						j = 0;
					MyPickablePoint edgePoint2 = points.get(pointsOfEdgeAl.get(j));
					points.get(edgePoint1.id).continuousEdgeNo = edgeNo;
					points.get(edgePoint2.id).continuousEdgeNo = edgeNo;
					if (!contEdge.points.contains(edgePoint1.id))
						contEdge.addPoint(edgePoint1.id);
					if (!contEdge.points.contains(edgePoint2.id))
						contEdge.addPoint(edgePoint2.id);
					MyEdge edgeFromPoints = getEdgeFromTwoPoints(edgePoint1, edgePoint2);
					if (edgeFromPoints != null)
						contEdge.connectedEdges.add(edgeFromPoints);
					i++;
				}
				continuousEdges.put(edgeNo, contEdge);
				edgeNo++;
			}
		}

		// calculate type of Edge START MIDDLE or END
		ArrayList<MyContinuousEdge> sortedContinuousEdgeList = new ArrayList(continuousEdges.values());
		Collections.sort(sortedContinuousEdgeList, new MyEdgeYComparator());

		sortedContinuousEdgeList.get(0).edgeType = MyContinuousEdge.EdgeType.END;
		sortedContinuousEdgeList.get(sortedContinuousEdgeList.size() - 1).edgeType = MyContinuousEdge.EdgeType.START;

		// correctly orient points of continuous edge
		Iterator<Integer> it = continuousEdges.keySet().iterator();
		while (it.hasNext()) {
			edgeNo = it.next();
			MyContinuousEdge edge = continuousEdges.get(edgeNo);

			if (edge.edgeType != MyContinuousEdge.EdgeType.END && edge.edgeType != MyContinuousEdge.EdgeType.START) {
				MyPickablePoint p1 = edge.getPointByIndex(0);
				MyPickablePoint p2 = edge.getPointByIndex(1);
				Vector3D vec1 = new Vector3D(p1.getX(), p1.getY(), p1.getZ());
				Vector3D vec2 = new Vector3D(p2.getX(), p2.getY(), p2.getZ());
				Vector3D vecC = new Vector3D(edge.center.x, edge.center.y, edge.center.z);

				Vector3D vecC1 = vec1.subtract(vecC);
				Vector3D vecC2 = vec2.subtract(vecC);
				Vector3D vecN = vecC1.crossProduct(vecC2);

				Vector3D vecCenOrigin = new Vector3D(0, vecC.getY(), 0);
				Vector3D vecCen = vecC.subtract(vecCenOrigin);

				try {
					double angle = Vector3D.angle(vecCen, vecN);
					System.out.println(angle);
					if (angle > Math.PI) {
						// points of continuous edge is not ordered in positive direction
						Collections.reverse(edge.points);
					}
				} catch (ArithmeticException ex) {
					System.out.println(ex.getMessage() + " for edge: " + edge.edgeNo);
					
					if(edge.edgeNo>=13 && edge.edgeNo<=16)
					{
						System.out.println("14");
					}
					Collections.sort(edge.points, new RoundPointComparator());
					if(edge.edgeNo>=13 && edge.edgeNo<=16)
					{
						System.out.println(edge.points);
						System.out.println("");
					}
					
					
					

				}
			}

		}

	}

	public Plane getPlaneForPoint(MyPickablePoint point) throws org.apache.commons.math3.exception.MathArithmeticException {
		Plane plane = null;
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
		plane = new Plane(vecPoint, vecPrevPoint, vecNextPoint, 0.0001);
		return plane;
	}

	static boolean isBetween(Coord3d a, Coord3d b, Coord3d c) {
		if (a.distance(c) + c.distance(b) == a.distance(b))
			return true;

		return false;

	}

	public Plane getPlaneForMiddlePoint(MyPickablePoint point) throws org.apache.commons.math3.exception.MathArithmeticException {
		Plane plane = null;
		MyPickablePoint prevPoint = null;
		MyPickablePoint nextPoint = null;
		MyEdge continuousEdge = continuousEdges.get(point.continuousEdgeNo);

		while (plane == null) {
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

			prevPoint = continuousEdge.getPointByIndex(prevIndex);
			nextPoint = continuousEdge.getPointByIndex(nextIndex);
			try {
				System.out.println(prevPoint.id + " " + point.id + " " + nextPoint.id);

				Vector3D centerVec = new Vector3D(continuousEdge.center.x, continuousEdge.center.y, continuousEdge.center.z);
				Vector3D vecPrevPoint = new Vector3D(prevPoint.xyz.x, prevPoint.xyz.y, prevPoint.xyz.z);
				Vector3D vecNextPoint = new Vector3D(nextPoint.xyz.x, nextPoint.xyz.y, nextPoint.xyz.z);
				Vector3D vecPoint = new Vector3D(point.xyz.x, point.xyz.y, point.xyz.z);
				plane = new Plane(vecPoint, vecPrevPoint, vecNextPoint, 0.0001);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				point = prevPoint;
			}
		}

		return plane;
	}

	public void calculateMaxAndMins() {
		minX = Double.MAX_VALUE;
		minY = Double.MAX_VALUE;
		minZ = Double.MAX_VALUE;

		maxX = Double.MIN_VALUE;
		maxY = Double.MIN_VALUE;
		maxZ = Double.MIN_VALUE;

		for (MyPickablePoint point : this.points.values()) {
			if (point.getX() < this.minX)
				minX = point.getX();
			if (point.getY() < this.minY)
				minY = point.getY();
			if (point.getZ() < this.minZ)
				minZ = point.getZ();

			if (point.getX() > this.maxX)
				maxX = point.getX();
			if (point.getY() > this.maxY)
				maxY = point.getY();
			if (point.getZ() > this.maxZ)
				maxZ = point.getZ();
		}

		if (maxX - minX > maxEdge)
			maxEdge = maxX - minX;
		if (maxZ - minZ > maxEdge)
			maxEdge = maxZ - minZ;

	}

	public void showLengthDistrib() {
		for (Float length1 : MyEdge.hmLengthDistrib.keySet()) {
			System.out.println(length1 + " count: " + MyEdge.hmLengthDistrib.get(length1));
		}
	}

	public void markRadiusEdges() {
		double radius = Double.valueOf(Settings.instance.getSetting("pipe_radius"));

		double rx_min = -this.maxX + radius;
		double rx_max = this.maxX - radius;
		double rz_min = -this.maxZ + radius;
		double rz_max = this.maxZ - radius;

		for (MyEdge edg : this.edges.values()) {
			if (edg.center.x < rx_min && edg.center.z > rz_max) {
				edg.edgeType = MyEdge.EdgeType.ONRADIUS;
			} else if (edg.center.x > rx_max && edg.center.z > rz_max) {
				edg.edgeType = MyEdge.EdgeType.ONRADIUS;
			} else if (edg.center.x < rx_min && edg.center.z < rz_min) {
				edg.edgeType = MyEdge.EdgeType.ONRADIUS;
			} else if (edg.center.x > rx_max && edg.center.z < rz_min) {
				edg.edgeType = MyEdge.EdgeType.ONRADIUS;
			} else {
				edg.edgeType = MyEdge.EdgeType.NORMAL;
			}
		}

	}

	public MyEdge getEdgeFromPoint(MyPickablePoint point, boolean direction) {

		MyEdge ret = null;
		for (MyEdge edge : edges.values()) {
			// System.out.println("EdgeNo: " + edge.edgeNo);
			int startInd, endInd;
			if (direction) {
				startInd = 0;
				endInd = 1;
			} else {
				startInd = 1;
				endInd = 0;
			}

			if (edge.getPointByIndex(startInd).equals(point)) {
				ret = edge;
				break;
			} else if (edge.getPointByIndex(endInd).equals(point)) {
				ret = edge;
				break;
			}
		}
		return ret;
	}

	public MyEdge getEdgeFromTwoPoints(MyPickablePoint point1, MyPickablePoint point2) {

		MyEdge ret = null;
		for (MyEdge edge : edges.values()) {
			if (edge.getPointByIndex(0).equals(point1) && edge.getPointByIndex(1).equals(point2)
					|| edge.getPointByIndex(0).equals(point2) && edge.getPointByIndex(1).equals(point1)) {
				ret = edge;
				break;
			}
		}
		return ret;
	}

	public MyPickablePoint getPointbyId(Integer id) {
		return points.get(id);
	}

	public org.jzy3d.plot3d.primitives.Point calculateOffsetPoint(MyPickablePoint point) {
		return calculateOffsetPointAndPlane(point).point;
	}

	public PointAndPlane calculateOffsetPointAndPlane(MyPickablePoint point) {
		boolean plotPlanes = false;
		PointAndPlane ret = new PointAndPlane();

		MyContinuousEdge continuousEdge = continuousEdges.get(point.continuousEdgeNo);
		int index = continuousEdge.points.indexOf(point.id);
		int prevIndex = -1;
		int nextIndex = -1;

		if (index == 0)
			// check if there is edge that connect 0 and continuousEdge.points.size()
			// - 1 point
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
		ret.prevPoint = prevPoint;
		ret.nextPoint = nextPoint;

		Vector3D vecPrevPoint = new Vector3D(prevPoint.xyz.x, prevPoint.xyz.y, prevPoint.xyz.z);
		Vector3D vecNextPoint = new Vector3D(nextPoint.xyz.x, nextPoint.xyz.y, nextPoint.xyz.z);
		Vector3D vecPoint = new Vector3D(point.xyz.x, point.xyz.y, point.xyz.z);

		System.out.println(continuousEdge.edgeType);
		Vector3D result = null;

		Vector3D p1 = new Vector3D(-this.maxX, this.maxY, this.maxZ);
		Vector3D p2 = new Vector3D(this.maxX, this.maxY, this.maxZ);
		Vector3D p3 = new Vector3D(this.maxX, this.maxY, -this.maxZ);
		Vector3D p4 = new Vector3D(-this.maxX, this.maxY, -this.maxZ);

		Vector3D p1_ = new Vector3D(-this.maxX, -this.maxY, this.maxZ);
		Vector3D p2_ = new Vector3D(this.maxX, -this.maxY, this.maxZ);
		Vector3D p3_ = new Vector3D(this.maxX, -this.maxY, -this.maxZ);
		// Vector3D p4_ = new Vector3D(-this.maxX, -this.maxY, -this.maxZ);

		// rotate planes like anglTxt is rotated
		double angle = Double.valueOf(SurfaceDemo.instance.angleTxt);
		// angle = Math.round(angle);

		Rotation rotPlane = new Rotation(new Vector3D(0, 1.0d, 0), Math.toRadians(angle));
		p1 = rotPlane.applyTo(p1);
		p2 = rotPlane.applyTo(p2);
		p3 = rotPlane.applyTo(p3);
		p4 = rotPlane.applyTo(p4);
		p1_ = rotPlane.applyTo(p1_);
		p2_ = rotPlane.applyTo(p2_);
		p3_ = rotPlane.applyTo(p3_);

		Plane pl1 = new Plane(p2, p3, p3_, 0.1);
		Plane pl2 = new Plane(p1, p2, p2_, 0.1);
		Plane pl3 = new Plane(p4, p1, p1_, 0.1);
		Plane pl4 = new Plane(p4, p3_, p3, 0.1);

		Plane[] planes = new Plane[] { pl1, pl2, pl3, pl4 };

		Vector3D contEdgCenter = new Vector3D(continuousEdge.center.x, continuousEdge.center.y, continuousEdge.center.z);
		Plane plane = null;
		int i = -1;
		for (final Plane pl : planes) {
			i++;
			// Polygon polygon = null;
			// if (plotPlanes) {
			// polygon = new Polygon();
			// if (i == 0) {
			// polygon.add(new Point(new Coord3d(p2.getX(), p2.getY(), p2.getZ())));
			// polygon.add(new Point(new Coord3d(p3.getX(), p3.getY(), p3.getZ())));
			// polygon.add(new Point(new Coord3d(p3_.getX(), p3_.getY(),
			// p3_.getZ())));
			// } else if (i == 1) {
			// polygon.add(new Point(new Coord3d(p1.getX(), p1.getY(), p1.getZ())));
			// polygon.add(new Point(new Coord3d(p2.getX(), p2.getY(), p2.getZ())));
			// polygon.add(new Point(new Coord3d(p2_.getX(), p2_.getY(),
			// p2_.getZ())));
			// } else if (i == 2) {
			// polygon.add(new Point(new Coord3d(p4.getX(), p4.getY(), p4.getZ())));
			// polygon.add(new Point(new Coord3d(p1.getX(), p1.getY(), p1.getZ())));
			// polygon.add(new Point(new Coord3d(p1_.getX(), p1_.getY(),
			// p1_.getZ())));
			// } else if (i == 3) {
			// polygon.add(new Point(new Coord3d(p4.getX(), p4.getY(), p4.getZ())));
			// polygon.add(new Point(new Coord3d(p3.getX(), p3.getY(), p3.getZ())));
			// polygon.add(new Point(new Coord3d(p3_.getX(), p3_.getY(),
			// p3_.getZ())));
			// }
			// polygon.setPolygonMode(PolygonMode.FRONT);
			// polygon.setColor(Color.GREEN);
			// SurfaceDemo.instance.myComposite.add(polygon);
			// }

			double distance = pl.getOffset(vecPoint);
			System.out.println(distance);
			if (pl.contains(vecPoint)) {
				plane = pl;
				ret.plane = pl;
				break;
			}
			// SurfaceDemo.instance.getChart().render();
			// if (polygon != null)
			// SurfaceDemo.instance.myComposite.remove(polygon);
		}

		try {
			Vector3D vecA = vecPrevPoint.subtract(vecPoint);
			Vector3D vecB = vecNextPoint.subtract(vecPoint);

			Line l = new Line(vecPrevPoint, vecNextPoint, 0.0001);
			if (l.contains(vecPoint)) {
				// all 3 points are collinear
				// point is NOT on radius edge
				// try first with 90degree and with -90 degree and take the angle that
				// produces point nearest to center of edge

				Rotation rotation1 = new Rotation(plane.getNormal(), Math.PI / 2);
				Vector3D rotatedA = rotation1.applyTo(vecA).normalize();
				Vector3D newPoint1 = vecPoint.add(rotatedA.scalarMultiply(SurfaceDemo.getInstance().getKerfOffset()));
				Rotation rotation2 = new Rotation(plane.getNormal(), -Math.PI / 2);
				Vector3D rotatedB = rotation2.applyTo(vecA).normalize();
				Vector3D newPoint2 = vecPoint.add(rotatedB.scalarMultiply(SurfaceDemo.getInstance().getKerfOffset()));

				if (newPoint1.distance(contEdgCenter) < newPoint2.distance(contEdgCenter))
					ret.point.xyz.set((float) newPoint1.getX(), (float) newPoint1.getY(), (float) newPoint1.getZ());
				else
					ret.point.xyz.set((float) newPoint2.getX(), (float) newPoint2.getY(), (float) newPoint2.getZ());

			} else {
				// points are not colinear
				Rotation rotationP = new Rotation(ret.plane.getNormal(), Math.PI / 2);
				Rotation rotationN = new Rotation(ret.plane.getNormal(), -Math.PI / 2);
				Vector3D rotatedA = rotationP.applyTo(vecA).normalize();
				Vector3D rotatedB = rotationN.applyTo(vecB).normalize();
				Vector3D vecAoffset = vecA.add(rotatedA.scalarMultiply(SurfaceDemo.getInstance().getKerfOffset()));
				Vector3D vecBoffset = vecB.add(rotatedB.scalarMultiply(SurfaceDemo.getInstance().getKerfOffset()));
				Line lineA = new Line(vecPrevPoint.add(vecAoffset), vecPoint.add(vecAoffset), Math_E);
				Line lineB = new Line(vecNextPoint.add(vecBoffset), vecPoint.add(vecBoffset), Math_E);
				Vector3D intersect = lineA.intersection(lineB);
				ret.point.xyz.x = (float) intersect.getX();
				ret.point.xyz.y = (float) intersect.getY();
				ret.point.xyz.z = (float) intersect.getZ();

				// ret.plane = new Plane(plane.getNormal(), vecNextPoint, 0.001);
			}
		} catch (Exception ex) {
			// if we are not at surface of four planes then we are on edge lets move
			// kerf toward center of edge
			Vector3D vecOffset = new Vector3D(0, Math.signum(continuousEdge.center.y - point.xyz.y) * SurfaceDemo.instance.getKerfOffset(), 0);
			result = vecPoint.add(vecOffset);
			ret.point.xyz.x = (float) result.getX();
			ret.point.xyz.y = (float) result.getY();
			ret.point.xyz.z = (float) result.getZ();

			Vector3D vec1 = new Vector3D(point.getX(), point.getY(), point.getZ());
			Vector3D vec2 = new Vector3D(ret.nextPoint.getX(), ret.nextPoint.getY(), ret.nextPoint.getZ());

			ret.plane = new Plane(vec1, vec2, vecOffset, Math_E);
		}
		// } else {
		// Vector3D vecOffset = new Vector3D(0, Math.signum(point.xyz.y) *
		// SurfaceDemo.instance.getKerfOffset(), 0);
		// result = vecPoint.add(vecOffset);
		// ret.point.xyz.x = (float) result.getX();
		// ret.point.xyz.y = (float) result.getY();
		// ret.point.xyz.z = (float) result.getZ();
		// Plane p = new Plane(vecPoint, vecOffset, vecPrevPoint, Math_E);
		// ret.plane = p;
		// }

		if (continuousEdge.edgeType == MyContinuousEdge.EdgeType.END || continuousEdge.edgeType == MyContinuousEdge.EdgeType.START) {
			Vector3D vecOffset = new Vector3D(0, Math.signum(point.xyz.y) * SurfaceDemo.instance.getKerfOffset(), 0);
			result = vecPoint.add(vecOffset);
			ret.point.xyz.x = (float) result.getX();
			ret.point.xyz.y = (float) result.getY();
		}

		MyEdge edg1 = getEdgeFromTwoPoints(point, nextPoint);
		MyEdge edg2 = getEdgeFromTwoPoints(point, prevPoint);
		if (edg1 == null)
			ret.nextPoint = null;
		if (edg2 == null)
			ret.prevPoint = null;

		// define follow the path direction clockwise/anticlockwise depending on
		// plane pointing up or down
		double angle1 = Vector3D.angle(ret.plane.getNormal(), new Vector3D(0.0d, 0.0d, 1.0d));
		if (angle1 < Math.PI)
			ret.direction = false;
		else
			ret.direction = true;

		return ret;
	}

	void removeNotUsedPoints() {
		ArrayList<Integer> usedPoints = new ArrayList<Integer>();
		for (MyEdge e : edges.values()) {
			usedPoints.addAll(e.points);
		}
		Enumeration<Integer> it = points.keys();
		while (it.hasMoreElements()) {
			Integer pointId = it.nextElement();
			if (!usedPoints.contains(pointId)) {
				points.remove(pointId);
			}
		}
	}

}
