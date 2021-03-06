package com.kz.pipeCutter.BBB.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.XYZSettings;

public class MachinekitRunPostgui {

	public void start() {
		JSch jsch = new JSch();
		Session session;
		ChannelShell channelShell = null;
		try {
			String ip = Settings.getInstance().getSetting("machinekit_ip");
			String user = Settings.getInstance().getSetting("machinekit_user");
			String pass = Settings.getInstance().getSetting("machinekit_password");
			Settings.getInstance().log("MK instance at IP: " + ip);
			// Settings.getInstance().log("MK instance at host: " + host);
			session = jsch.getSession(user, ip, 22);
			session.setPassword(pass);

			session.setConfig("StrictHostKeyChecking", "no");
			session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");

			session.setServerAliveInterval(2000);
			session.setServerAliveCountMax(Integer.MAX_VALUE);

			session.setOutputStream(System.out);
			session.connect(30000); // making a connection with timeout.

			channelShell = (ChannelShell) session.openChannel("shell");
			OutputStream ops = channelShell.getOutputStream();
			PrintStream ps = new PrintStream(ops, true);

			// channelShell.setAgentForwarding(true);
			// channelShell.setXForwarding(true);
			channelShell.connect(5 * 1000);

			// String command = "source ~/git/machinekit/scripts/rip-environment";
			// ps.println(command);
			Settings.getInstance().log("Running postgui hal....");
			//String command = "halcmd -f /home/machinekit/git/machinekit-multicore/myini/3D.postgui.hal || echo 'hal completed'\n";
			String command = "halcmd -f /home/machinekit/git/machinekit/myini/3D.postgui.hal || echo 'hal completed'\n";
			ps.println(command);
			readOutput(channelShell);
			

			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (channelShell != null && channelShell.isConnected())
				channelShell.disconnect();
			Settings.getInstance().log("Running postgui hal....COMPLETED.");
		}
	}

	private void readOutput(ChannelShell channelShell) throws IOException, InterruptedException {
		InputStream in = channelShell.getInputStream();
		BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));

		String line;
		while (true) {
			line = buffReader.readLine();
			if (line != null)
				Logger.getLogger(this.getClass()).info(line);
			if (line.contains("'stepg_maxaccel_4'"))
				break;
			Thread.sleep(100);
		}
	}

}
