package com.kz.pipeCutter;

import java.util.Comparator;

public class MyPickablePointMidXComparator implements Comparator<MyPickablePoint> {

	public MyPickablePointMidXComparator() {
	}

	@Override
	public int compare(MyPickablePoint p1, MyPickablePoint p2) {
		// TODO Auto-generated method stub
		if (Math.abs(p1.xyz.x) < Math.abs(p2.xyz.x))
			return -1;
		else if (Math.abs(p1.xyz.x) == Math.abs(p2.xyz.x))
			if (p1.xyz.y > p2.xyz.y)
				return -1;
			else if (p1.xyz.y < p2.xyz.y)
				return 1;
			else
				return 0;
		else
			return 1;
	}
}
