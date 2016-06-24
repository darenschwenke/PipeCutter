package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.MyPickablePoint;
import com.kz.pipeCutter.SurfaceDemo;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;

import pb.Message;
import pb.Message.Container;
import pb.Status.EmcStatusMotionAxis;
import pb.Types.ContainerType;

public class BBBStatus implements Runnable {
	private static BBBStatus instance;
	private org.zeromq.ZMQ.Socket socket = null;
	ByteArrayInputStream is;
	public ChannelExec channelExec = null;
	ZContext ctx;
	private String uri;
	private Thread readThread;
	double x = 0, y = 0, z = 0, a = 0, b=0, c=0;


	public BBBStatus() {
		initSocket();
		instance = this;
	}

	public static BBBStatus getInstance() {
		if (instance == null)
			instance = new BBBStatus();
		return instance;
	}

	public static void main(String[] args) {
		Settings sett = new Settings();
		sett.setVisible(true);
		BBBStatus status = new BBBStatus();
	}

	public Socket getSocket() {
		return this.socket;
	}

	@Override
	public void run() {
		if (!Settings.instance.isVisible())
			return;

		Container contReturned;
		while (true) {
			try {
				ZMsg receivedMessage = ZMsg.recvMsg(socket, ZMQ.DONTWAIT);
				// System.out.println("loop: " + i);
				if (receivedMessage != null) {
					while (!receivedMessage.isEmpty()) {

						ZFrame frame = receivedMessage.poll();
						byte[] returnedBytes = frame.getData();
						String messageType = new String(returnedBytes);
						// System.out.println("type: " + messageType);
						if (!messageType.equals("motion") && !messageType.equals("task") && !messageType.equals("io")
								&& !messageType.equals("interp")) {

							contReturned = Message.Container.parseFrom(returnedBytes);
							if (contReturned.getType().equals(ContainerType.MT_EMCSTAT_FULL_UPDATE)
									|| contReturned.getType().equals(ContainerType.MT_EMCSTAT_INCREMENTAL_UPDATE)) {

								Iterator<EmcStatusMotionAxis> itAxis = contReturned.getEmcStatusMotion().getAxisList().iterator();
								while (itAxis.hasNext()) {
									EmcStatusMotionAxis axis = itAxis.next();
									int index = axis.getIndex();
									switch (index) {
									case 0:
										x = contReturned.getEmcStatusMotion().getActualPosition().getX();
										Settings.instance.setSetting("position_x", x);
										break;
									case 1:
										y = contReturned.getEmcStatusMotion().getActualPosition().getY();
										Settings.instance.setSetting("position_y", y);
										break;
									case 2:
										z = contReturned.getEmcStatusMotion().getActualPosition().getZ();
										Settings.instance.setSetting("position_z", z);
										break;
									case 3:
										a = contReturned.getEmcStatusMotion().getActualPosition().getA();
										Settings.instance.setSetting("position_a", a);
										break;
									case 4:
										b = contReturned.getEmcStatusMotion().getActualPosition().getB();
										Settings.instance.setSetting("position_b", b);
										break;
									case 5:
										c = contReturned.getEmcStatusMotion().getActualPosition().getC();
										Settings.instance.setSetting("position_c", c);
										break;
									default:
										break;
									}
								}

								if (SurfaceDemo.getInstance() != null) {
									if (SurfaceDemo.instance.getChart() != null) {
										//System.out.println(String.format("%1$,.2f, %2$,.2f, %3$,.2f",x,y,z));
										Coord3d coord = new Coord3d(x, y, z);
										MyPickablePoint mp = new MyPickablePoint(-2, coord, Color.MAGENTA, 1, -1);
										SurfaceDemo.instance.move(mp, false, 0, false);
										SurfaceDemo.instance.utils.rotatePoints(a, false, false);
									}
								}
							} else if (contReturned.getType().equals(ContainerType.MT_PING)) {
								MachinekitSettings.instance.pingStatus();
							} else {
								System.out.println(contReturned.getType());
							}
						}
					}
					receivedMessage.destroy();
					receivedMessage = null;
				}
			} catch (Exception e) {
					e.printStackTrace();
			}
			// try {
			// TimeUnit.MILLISECONDS.sleep(200);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

	}

	public void initSocket() {
		if (ctx != null && socket != null) {
			socket.close();
			ctx.close();
		}
		if (readThread != null && readThread.isAlive())
			readThread.interrupt();

		uri = Settings.getInstance().getSetting("machinekit_statusService_url");

		ctx = new ZContext(2);
		// Set random identity to make tracing easier
		socket = ctx.createSocket(ZMQ.SUB);

		Random rand = new Random(23424234);
		String identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());

		socket.setIdentity(identity.getBytes(ZMQ.CHARSET));
		socket.subscribe("motion".getBytes(ZMQ.CHARSET));
		socket.subscribe("task".getBytes(ZMQ.CHARSET));
		// socket.subscribe("io".getBytes(ZMQ.CHARSET));
		// socket.subscribe("interp".getBytes(ZMQ.CHARSET));
		socket.setReceiveTimeOut(100);
		socket.connect(this.uri);

		readThread = new Thread(this);
		readThread.setName("BBBStatus");
		readThread.start();
	}

}
