package com.kz.pipeCutter.ui.tab;

import java.awt.Dimension;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.kz.pipeCutter.ui.MyVerticalFlowLayout;
import com.kz.pipeCutter.ui.PinDef;
import com.kz.pipeCutter.ui.SavableCheckBox;
import com.kz.pipeCutter.ui.SavableText;

import pb.Types.HalPinDirection;
import pb.Types.ValueType;

public class PlasmaSettings extends JPanel {

	public XYSeries seriesVoltTime = new XYSeries("XYGraph");
	public XYSeries seriesVoltConstTime = new XYSeries("ConstLine");
	JFreeChart chart;

	public PlasmaSettings() {
		super();
		this.setPreferredSize(new Dimension(300, 450));
		MyVerticalFlowLayout flowLayout = new MyVerticalFlowLayout();
		this.setLayout(flowLayout);

		SavableText plasmaLengthMm = new SavableText();
		plasmaLengthMm.setLabelTxt("Plasma length [mm]:");
		plasmaLengthMm.setParId("plasma_length_mm");
		this.add(plasmaLengthMm);

		SavableText plasmaPierceOffsetMm = new SavableText();
		plasmaPierceOffsetMm.setLabelTxt("Plasma pierce offset [mm]:");
		plasmaPierceOffsetMm.setParId("plasma_pierce_offset_mm");
		this.add(plasmaPierceOffsetMm);

		SavableText plasmaPierceTimeS = new SavableText();
		plasmaPierceTimeS.setLabelTxt("Plasma pierce time [s]:");
		plasmaPierceTimeS.setParId("plasma_pierce_time_s");
		this.add(plasmaPierceTimeS);

		SavableText plasmaKerfOffsetMm = new SavableText();
		plasmaKerfOffsetMm.setLabelTxt("Plasma kerf offset [mm]:");
		plasmaKerfOffsetMm.setParId("plasma_kerf_offset_mm");
		this.add(plasmaKerfOffsetMm);

		SavableText plasmaLeadInRadius = new SavableText();
		plasmaLeadInRadius.setLabelTxt("Plasma lead in radius [mm]:");
		plasmaLeadInRadius.setParId("plasma_leadin_radius");
		this.add(plasmaLeadInRadius);

		SavableText plasmaCutOffsetMm = new SavableText();
		plasmaCutOffsetMm.setLabelTxt("Plasma above surface offset [mm]:");
		plasmaCutOffsetMm.setParId("plasma_cut_offset_mm");
		this.add(plasmaCutOffsetMm);

		SavableCheckBox thcEnablel = new SavableCheckBox();
		thcEnablel.setPin(new PinDef("myini.thc-enable", HalPinDirection.HAL_OUT, ValueType.HAL_BIT));
		thcEnablel.setNeedsSave(true);
		thcEnablel.requiresHalRCompSet = true;
		thcEnablel.setParId("myini.thc-enable");
		thcEnablel.setLabelTxt("Thc enable:");
		this.add(thcEnablel);

		SavableText actualVolts = new SavableText();
		actualVolts.setPin(new PinDef("myini.actual-volts", HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		actualVolts.requiresHalRCompSet = false;
		actualVolts.setNeedsSave(false);
		actualVolts.setParId("myini.actual-volts");
		actualVolts.setLabelTxt("Actual volts [V]:");
		this.add(actualVolts);

		SavableText voltsRequested = new SavableText();
		voltsRequested.setPin(new PinDef("myini.volts-requested", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		voltsRequested.requiresHalRCompSet = true;
		voltsRequested.setNeedsSave(true);
		voltsRequested.setParId("myini.volts-requested");
		voltsRequested.setLabelTxt("VoltsRequested [V]:");
		voltsRequested.setToolTipText("Tip Volts current_vel >= min_velocity requested");
		this.add(voltsRequested);

		SavableCheckBox velStatus = new SavableCheckBox();
		velStatus.setPin(new PinDef("myini.vel-status", HalPinDirection.HAL_IN, ValueType.HAL_BIT));
		velStatus.requiresHalRCompSet = false;
		velStatus.setNeedsSave(false);
		velStatus.setParId("myini.vel-status");
		velStatus.setLabelTxt("Velocity status");
		velStatus.setToolTipText("When the THC thinks we are at requested speed");
		this.add(velStatus);

		SavableText velTol = new SavableText();
		velTol.setPin(new PinDef("myini.vel-tol", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		velTol.requiresHalRCompSet = true;
		velTol.setNeedsSave(true);
		velTol.setParId("myini.vel-tol");
		velTol.setLabelTxt("Velocity Tolerance [%] (Corner Lock):");
		velTol.setToolTipText("Velocity Tolerance [%] (Corner Lock)s");
		this.add(velTol);

		SavableText scaleOff = new SavableText();
		scaleOff.setPin(new PinDef("myini.scale-offset", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		scaleOff.requiresHalRCompSet = true;
		scaleOff.setNeedsSave(true);
		scaleOff.setParId("myini.scale-offset");
		scaleOff.setLabelTxt("Scale offset:");
		scaleOff.setToolTipText("Scale offset");
		this.add(scaleOff);

		SavableText velScale = new SavableText();
		velScale.setPin(new PinDef("myini.vel-scale", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		velScale.requiresHalRCompSet = true;
		velScale.setNeedsSave(true);
		velScale.setParId("myini.vel-scale");
		velScale.setLabelTxt("Velocit scale:");
		velScale.setToolTipText("Velocity scale");
		this.add(velScale);

		SavableText volTolerance = new SavableText();
		volTolerance.setPin(new PinDef("myini.voltage-tol", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		volTolerance.requiresHalRCompSet = true;
		volTolerance.setNeedsSave(true);
		volTolerance.setParId("myini.voltage-tol");
		volTolerance.setLabelTxt("Voltage tolerance:");
		volTolerance.setToolTipText("Voltage tolerance");
		this.add(volTolerance);

		SavableText corrVel = new SavableText();
		corrVel.setPin(new PinDef("myini.correction-vel", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		corrVel.requiresHalRCompSet = true;
		corrVel.setNeedsSave(true);
		corrVel.setParId("myini.correction-vel");
		corrVel.setLabelTxt("Correction velocity:");
		corrVel.setToolTipText("Correction velocity");
		this.add(corrVel);

		SavableCheckBox thcTorchProbe = new SavableCheckBox();
		thcTorchProbe.setPin(new PinDef("myini.torch-probe", HalPinDirection.HAL_IN, ValueType.HAL_BIT));
		thcTorchProbe.setNeedsSave(false);
		thcTorchProbe.requiresHalRCompSet = false;
		thcTorchProbe.setParId("myini.torch-probe");
		thcTorchProbe.setLabelTxt("Torch probe");
		this.add(thcTorchProbe);

		SavableText thcZPos = new SavableText();
		thcZPos.setPin(new PinDef("myini.thc-z-pos", HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		thcZPos.requiresHalRCompSet = false;
		thcZPos.setNeedsSave(false);
		thcZPos.setParId("myini.thc-z-pos");
		thcZPos.setLabelTxt("thc z-pos");
		this.add(thcZPos);

		SavableText offsetValue = new SavableText();
		offsetValue.setPin(new PinDef("myini.offset-value", HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		offsetValue.requiresHalRCompSet = false;
		offsetValue.setNeedsSave(false);
		offsetValue.setParId("myini.offset-value");
		offsetValue.setLabelTxt("thc offset value");
		this.add(offsetValue);

		// Add the series to your data set
		seriesVoltTime.add(1, 1);
		seriesVoltTime.add(2, 2);
		seriesVoltTime.add(3, 3);
		seriesVoltTime.add(4, 3);
		seriesVoltTime.add(5, 10); // Generate the graph

		seriesVoltConstTime.add(1, 4);
		seriesVoltConstTime.add(2, 4);
		seriesVoltConstTime.add(3, 4);
		seriesVoltConstTime.add(4, 4);
		seriesVoltConstTime.add(5, 4);

		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(seriesVoltTime);
		dataset.addSeries(seriesVoltConstTime);

		chart = ChartFactory.createXYLineChart("", // Title
				"time[ms]", // x-axis Label
				"voltage[V]", // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				false, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
		);
		updateChartRange();

		ChartPanel chartVoltagePanel = new ChartPanel(chart);
		chartVoltagePanel.setPreferredSize(new Dimension(450, 350));
		this.add(chartVoltagePanel);
	}

	public void updateChartRange() {
		ValueAxis domainAxis = chart.getXYPlot().getDomainAxis();
		ValueAxis rangeAxis = chart.getXYPlot().getRangeAxis();
		// set max time window as 1 sec
		domainAxis.setRange(seriesVoltConstTime.getMaxX() - 1, seriesVoltConstTime.getMaxX());
		// domainAxis.setTickUnit(new NumberTickUnit(0.1));
		// set max range window as 20V up and down from latest
		double minY = (double) seriesVoltTime.getY(seriesVoltTime.getItemCount() - 1) - 20;
		double maxY = (double) seriesVoltTime.getY(seriesVoltTime.getItemCount() - 1) + 20;
		rangeAxis.setRange(minY, maxY);
		// rangeAxis.setTickUnit(new NumberTickUnit(0.05));
	}
}
