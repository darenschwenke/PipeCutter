package com.kz.pipeCutter.ui;

//install bbonjour avahi sevice in windows: https://support.apple.com/kb/DL999?locale=sl_SI
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.glassfish.tyrus.client.ClientManager;
import org.jzy3d.chart.factories.ChartComponentFactory;

import com.jogamp.common.util.InterruptSource.Thread;
import com.kz.pipeCutter.BBB.BBBError;
import com.kz.pipeCutter.BBB.BBBHalCommand;
import com.kz.pipeCutter.BBB.BBBHalRComp;
import com.kz.pipeCutter.BBB.BBBMachineTalkCommand;
import com.kz.pipeCutter.BBB.BBBPreviewStatus;
import com.kz.pipeCutter.BBB.BBBStatus;
import com.kz.pipeCutter.BBB.Discoverer;
import com.kz.pipeCutter.ui.tab.GcodeViewer;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;
import com.kz.pipeCutter.ui.tab.OtherSettings;
import com.kz.pipeCutter.ui.tab.PlasmaSettings;
import com.kz.pipeCutter.ui.tab.RotatorSettings;
import com.kz.pipeCutter.ui.tab.XYZSettings;

public class Settings extends JFrame {

	private JPanel contentPane;
	public static String iniFullFileName = getIniPath();
	public static String iniEdgeProperties;
	private static Settings instance;
	public static Discoverer discoverer;
	public static BBBError error;
	public static BBBStatus status;
	public static BBBHalCommand halCommand;
	public static BBBHalRComp halRComp;
	public static BBBPreviewStatus previewStatus;

	public static HashMap<String, SavableControl> controls = new HashMap<String, SavableControl>();

	public JSplitPane splitPane;
	CommandPanel commandPanel;
	public XYZSettings xyzSettings;
	public PlasmaSettings plasmaSettings;

	boolean repositioned = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		// StdOutErrLog.tieSystemOutAndErrToLog();

		System.setProperty("java.net.preferIPv4Stack", "true");

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Settings frame = Settings.getInstance();
			}
		});

	}

	public void initCommandService() {
		if (BBBMachineTalkCommand.ctx != null) {
			BBBMachineTalkCommand.ctx.destroySocket(BBBMachineTalkCommand.socket);
			BBBMachineTalkCommand.ctx.destroy();
		}

		BBBMachineTalkCommand.ctx = null;
		BBBMachineTalkCommand.socket = null;
		BBBMachineTalkCommand.initSocket();
	}

	public void initErrorService() {
		if (error == null)
			error = new BBBError();
		else
			error.initSocket();
	}

	public void initStatusService() {
		if (status == null)
			status = new BBBStatus();
		else
			status.initSocket();
	}

	public void initHalCmdService() {
		if (halCommand == null)
			halCommand = new BBBHalCommand();
		else
			halCommand.initSocket();
	}

	public void initHalRcompService() {
		if (halRComp == null)
			halRComp = new BBBHalRComp();
		else
			halRComp.initSocket();
	}

	public void initPreviewStatusService() {
		// if (previewStatus == null)
		// previewStatus = new BBBPreviewStatus();
		// else
		// previewStatus.initSocket();
	}

	/**
	 * Create the frame.
	 */
	protected Settings() {
		// this.setAlwaysOnTop(true);
		this.setTitle("PipeCutter settings");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// setBounds(400, 500, 800, 650);
		this.setPreferredSize(new Dimension(900, 700));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		// tabbedPane.setMinimumSize(new Dimension(600, 500));

		xyzSettings = new XYZSettings();
		plasmaSettings = new PlasmaSettings();

		tabbedPane.addTab("MachinekitSettings", new MachinekitSettings());
		tabbedPane.addTab("Rotators", new RotatorSettings());
		tabbedPane.addTab("XYZ", xyzSettings);
		tabbedPane.addTab("Plasma", plasmaSettings);
		tabbedPane.addTab("Other", new OtherSettings());

		tabbedPane.setSelectedIndex(0);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(splitPane, BorderLayout.NORTH);
		splitPane.setDividerLocation(600);

		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				System.out.println("split changed");
				Logger.getLogger(this.getClass()).info("splitter resized");
				Logger.getLogger(this.getClass()).info(pce.getPropertyName() + "=" + pce.getNewValue());
			}
		});

		commandPanel = new CommandPanel();
		splitPane.setTopComponent(tabbedPane);
		GcodeViewer gcodeViewer = new GcodeViewer();
		tabbedPane.addTab("Gcode", gcodeViewer);
		gcodeViewer.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		splitPane.setBottomComponent(commandPanel);

		this.addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent evt) {
				Component c = (Component) evt.getSource();
				// System.out.println(c.getName() + " resized: " +
				// c.getSize().toString());
				if (c.getName().equals("frame0") && repositioned) {
					try {
						FileInputStream in = new FileInputStream(Settings.iniFullFileName);
						SortedProperties props = new SortedProperties();
						props.load(in);
						in.close();

						FileOutputStream out = new FileOutputStream(Settings.iniFullFileName);
						props.setProperty("frame0", c.getSize().getWidth() + "x" + c.getSize().getHeight());
						props.store(out, null);
						out.close();

					} catch (Exception ex) {
						ex.printStackTrace();
					}
					// splitPane.setDividerLocation(1 -
					// (commandPanel.getHeight() /
					// Settings.getInstance().getHeight()));
				}
			}
		});

		this.addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent evt) {
				if (repositioned) {
					Component c = (Component) evt.getSource();
					Point currentLocationOnScreen = c.getLocationOnScreen();
					if (currentLocationOnScreen.getX() > 0 && currentLocationOnScreen.getY() > 0) {
						FileInputStream in;
						try {
							in = new FileInputStream(Settings.iniFullFileName);

							SortedProperties props = new SortedProperties();
							props.load(in);
							in.close();

							FileOutputStream out = new FileOutputStream(Settings.iniFullFileName);
							props.setProperty("frame0_location", String.format("%.0fx%.0f", currentLocationOnScreen.getX(), currentLocationOnScreen.getY()));
							props.store(out, null);
							out.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			public void componentHidden(ComponentEvent e) {
				/* code run when component hidden */
			}

			public void componentShown(ComponentEvent e) {
				FileInputStream in;
				try {
					in = new FileInputStream(Settings.iniFullFileName);
					SortedProperties props = new SortedProperties();
					props.load(in);
					in.close();

					if (props.get("frame0") != null) {
						String folder = Settings.getInstance().getSetting("screenshot_folder");
						if (folder != null) {
							ChartComponentFactory.SCREENSHOT_FOLDER = folder;
						}
						String size = props.get("frame0").toString();
						try {
							String[] splittedSize = size.split("x");
							System.out.println(size);
							Settings.this.setMinimumSize(new Dimension(Double.valueOf(splittedSize[0]).intValue(), Double.valueOf(splittedSize[1]).intValue()));

							// MachinekitSettings.getInstance().machinekitServices.setPreferredSize(new
							// Dimension(200,200));
							Settings.getInstance().pack();
							Settings.getInstance().repaint();
							SavableText c = (SavableText) Settings.getInstance().getParameter("machinekit_commandService_url");
							System.out.println(c.jValue.getSize());

						} catch (Exception ex) {
							ex.printStackTrace();
						}
						System.out.println("size: " + size);

					}

					if (props.get("frame0_location") != null) {
						String location_str = props.get("frame0_location").toString();
						try {
							String[] splittedSize = location_str.split("x");
							System.out.println(location_str);
							Settings.this.setLocation(new Point(Integer.valueOf(splittedSize[0]), Integer.valueOf(splittedSize[1])));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						System.out.println("size: " + location_str);
					}

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Settings.getInstance().pingBBB();

							initErrorService();
							initStatusService();
							initHalCmdService();
							initHalRcompService();
							// initPreviewStatusService();

						}
					});

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					repositioned = true;
				}
			}

		});

		this.setVisible(true);
		this.pack();
		Settings.instance = this;

		setEdgePropertiesFile();
	}

	protected void pingBBB() {
		InetAddress address;
		try {
			String machinekitHost = Settings.getInstance().getSetting("machinekit_host");
			address = InetAddress.getByName(machinekitHost);
			Settings.getInstance().setSetting("machinekit_ip", address.getHostAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static String getIniPath() {
		String ret = null;
		String iniFileName = "pipeCutter.ini";
		iniFullFileName = null;
		try {
			String path = new File(".").getCanonicalPath();
			ret = path + File.separator + iniFileName;
			File f = new File(ret);
			if (!f.exists()) {
				System.out.println(ret + " does not exist. Creating in path:" + path);
				File fout = new File(ret);
				FileOutputStream fos = new FileOutputStream(fout);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
				bw.write("#pipecutter ini file");
				bw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public static String getEdgePropertiesPath() {
		String ret = null;
		Settings settInst = Settings.getInstance();
		String gCodeInputFile = settInst.getParameter("gcode_input_file").getParValue();
		File f1 = new File(gCodeInputFile);

		String iniFileName = f1.getName() + "-edgeProperties.ini";
		try {
			String path = new File(".").getCanonicalPath();
			ret = path + File.separator + iniFileName;
			File f = new File(ret);
			if (!f.exists()) {
				Settings.instance.log(ret + " does not exist. Creating in path:" + path);
				File fout = new File(ret);
				FileOutputStream fos = new FileOutputStream(fout);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
				bw.write("# edge velocities ini file");
				bw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public String getSetting(String parameterId) {
		String ret = "";
		synchronized (this) {
			ret = controls.get(parameterId).getParValue();
			if (ret.equals(""))
				System.out.println("oops");
		}
		return ret;
	}

	public String getNonEmptySetting(String parameterId) {
		String ret = "";
		synchronized (this) {
			ret = controls.get(parameterId).getParValue();
			while (ret.equals(""))
			{
				try {
					Thread.currentThread().sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ret = controls.get(parameterId).getParValue();
			}
		}
		return ret;
	}

	public String getSetting2(String parameterId) {
		String ret = null;
		try {
			FileInputStream in = new FileInputStream(Settings.iniFullFileName);
			Properties props = new Properties();
			props.load(in);
			in.close();
			if (props.getProperty(parameterId) != null) {
				ret = props.getProperty(parameterId);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// TODO Auto-generated method stub
		if (ret == null)
			ret = "";
		return ret;
	}

	public void setSetting(String parameterId, String value) {
		try {
			SavableControl mysavable = null;
			// if (controls.get(parameterId) == null) {
			// List<SavableControl> savableControls =
			// harvestMatches(Settings.getInstance().getContentPane(),
			// SavableControl.class);
			// for (SavableControl savableControl : savableControls) {
			// Logger.getLogger(this.getClass()).info(savableControl.getPin().pinName);
			// if (savableControl.getParId().equals(parameterId)) {
			// mysavable = savableControl;
			// break;
			// }
			// }
			// controls.put(parameterId, mysavable);
			// }
			if(value=="" && parameterId.startsWith("position_"))
				System.out.println("");

			mysavable = controls.get(parameterId);
			// if(value.trim().equals("") && !mysavable.isLoadingValue)
			// System.out.println("");
			mysavable.setParValue(value);
			mysavable.save();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setSetting(String parameterId, Double value) {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
		// otherSymbols.setDecimalSeparator('.');
		// otherSymbols.setGroupingSeparator(',');

		// DecimalFormat df = new DecimalFormat("##,##0.0000", otherSymbols);
		DecimalFormat df = new DecimalFormat("0.0000", otherSymbols);
		df.setDecimalSeparatorAlwaysShown(true);
		String strValue = df.format(value);
		if (parameterId.startsWith("position_") && strValue.equals(""))
			System.out.println("oops");
		setSetting(parameterId, strValue);
	}

	public void setSetting(String parameterId, Float value) {
		setSetting(parameterId, (double) value);
	}

	public void setSetting(String parameterId, Integer value) {
		setSetting(parameterId, String.valueOf(value));
	}

	public IParameter getParameter(String parameterId) {
		IParameter ret = null;
		List<SavableControl> savableControls = harvestMatches(this.getContentPane(), SavableControl.class);
		for (SavableControl savableControl : savableControls) {
			System.out.println("control  id:" + savableControl.getParId());
			if (savableControl.getParId() != null && savableControl.getParId().equals(parameterId)) {
				ret = savableControl;
				break;
			}
		}
		return ret;
	}

	public String getHostOrIp() {
		String host = getSetting("machine_host");
		String ip = getSetting("machine_ip");
		if (host != null)
			return host;
		else if (ip != null)
			return ip;

		return null;
	}

	public static <T extends Component> List<T> harvestMatches(Container root, Class<T> clazz) {
		List<Container> containers = new LinkedList<>();
		List<T> harvested = new ArrayList<>();

		containers.add(root);
		while (!containers.isEmpty()) {
			Container container = containers.remove(0);
			for (Component component : container.getComponents()) {
				if (clazz.isAssignableFrom(component.getClass())) {
					harvested.add((T) component);
				} else if (component instanceof Container) {
					containers.add((Container) component);
				}
			}
		}
		return Collections.unmodifiableList(harvested);
	}

	public static List harvestSupportsInterface(Container root, Class clazz) {
		List<Container> containers = new LinkedList<>();
		List harvested = new ArrayList<>();

		containers.add(root);
		while (!containers.isEmpty()) {
			Container container = containers.remove(0);
			for (Component component : container.getComponents()) {
				if (clazz.isInstance(component)) {
					harvested.add(component);
				} else if (component instanceof Container) {
					containers.add((Container) component);
				}
			}
		}
		return Collections.unmodifiableList(harvested);
	}

	public static synchronized Settings getInstance() {
		if (instance == null)
			instance = new Settings();
		return instance;
	}

	public void log(String txt) {
		Logger.getLogger(this.getClass().getName()).info(txt);
		String txtOut = "";
		String patternString = "type:\\s(.+)\\sreply_ticket:\\s(.+)\\s";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(txt);
		if (matcher.find())
			txtOut = matcher.group(1) + "(" + matcher.group(2) + ")";
		else
			txtOut = txt;

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String logTxt = sdf.format(new Date()) + " " + txtOut;
		commandPanel.log.append(logTxt + "\n");
		commandPanel.log.setCaretPosition(commandPanel.log.getText().length());
	}

	public List<SavableControl> getAllControls() {
		List<SavableControl> savableControls = harvestMatches(Settings.getInstance().getContentPane(), SavableControl.class);
		return savableControls;
	}

	public List<IHasPinDef> getAllPinControls() {
		List<IHasPinDef> savableControls = harvestSupportsInterface(Settings.getInstance().getContentPane(), IHasPinDef.class);
		return savableControls;
	}

	public void updateHalValues() {
		Settings.getInstance().log("Update-ing hal values...");
		for (SavableControl cntrl : controls.values()) {
			Logger.getLogger(this.getClass()).info(cntrl.getParId());
			if (cntrl.requiresHalRCompSet) {
				Settings.getInstance().log("\t" + cntrl.getParId());
				cntrl.updateHal();
			}
		}
		Settings.getInstance().log("Update-ing hal values...DONE.");
	}

	public void setEdgePropertiesFile() {
		iniEdgeProperties = getEdgePropertiesPath();
	}

}
