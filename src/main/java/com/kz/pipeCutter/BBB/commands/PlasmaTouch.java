package com.kz.pipeCutter.BBB.commands;

import java.util.HashMap;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.kz.pipeCutter.ui.Settings;

public class PlasmaTouch implements Runnable {

	@Override
	public void run() {
		float plasmaHeight = Float.valueOf(Settings.instance.getSetting("plasma_length_mm"));
		float z = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));

		float zDiff = plasmaHeight-z;
		float zSpeed = Float.valueOf(Settings.instance.getSetting("myini.maxvel_2")) * 1000;

		new ExecuteMdi(String.format("G91\nG01 Z%5.3f F%5.3f\nG90", zDiff, zSpeed)).start();
	}

}
