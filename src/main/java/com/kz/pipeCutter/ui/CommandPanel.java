package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.kz.pipeCutter.BBB.Discoverer;
import com.kz.pipeCutter.BBB.commands.ChangeMode;
import com.kz.pipeCutter.BBB.commands.EstopReset;
import com.kz.pipeCutter.BBB.commands.ExecuteMdi;
import com.kz.pipeCutter.BBB.commands.HomeAxis;
import com.kz.pipeCutter.BBB.commands.MachinekitListProcesses;
import com.kz.pipeCutter.BBB.commands.MachinekitStart;
import com.kz.pipeCutter.BBB.commands.MachinekitStop;
import com.kz.pipeCutter.BBB.commands.PowerOff;
import com.kz.pipeCutter.BBB.commands.PowerOn;

import pb.Status.EmcTaskModeType;

public class CommandPanel extends JPanel {
	public CommandPanel() {
		super();

		this.setPreferredSize(new Dimension(420, 332));
		FlowLayout flowLayout = (FlowLayout) this.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);

		// ----------machineKitPanel---------------------------

		JPanel machineKitPanel = new JPanel();
		machineKitPanel.setPreferredSize(new Dimension(200, 350));
		this.add(machineKitPanel);
		
		JButton startMachineKit = new JButton("Start MachineKit");
		startMachineKit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new MachinekitStart().start();
			}
		});
		machineKitPanel.add(startMachineKit);

		JButton discoverMachineKit = new JButton("Discover MachineKit");
		discoverMachineKit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Discoverer.getInstance().discover();
			}
		});
		machineKitPanel.add(discoverMachineKit);

		JButton listMachineKit = new JButton("List MachineKit");
		listMachineKit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new MachinekitListProcesses().start();
			}
		});
		machineKitPanel.add(listMachineKit);

		JButton MachineKitStop = new JButton("Kill MachineKit");
		MachineKitStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new MachinekitStop().start();
			}
		});

		machineKitPanel.add(MachineKitStop);
		


		// ----------machineTalkPanel---------------------------
		JPanel machineTalkPanel = new JPanel();
		machineTalkPanel.setPreferredSize(new Dimension(300, 350));
		
		this.add(machineTalkPanel);

		JButton estopReset = new JButton("EStop reset");
		estopReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new EstopReset().start();
			}
		});
		machineTalkPanel.add(estopReset);

		JButton powerOn = new JButton("Power ON");
		powerOn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PowerOn().start();
			}
		});
		machineTalkPanel.add(powerOn);

		JButton PowerOff = new JButton("Power OFF");
		PowerOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PowerOff().start();
			}
		});
		machineTalkPanel.add(PowerOff);

		JButton modeManual = new JButton("Mode: MANUAL");
		modeManual.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Settings.parMode = EmcTaskModeType.EMC_TASK_MODE_MANUAL;
				new ChangeMode().start();
			}
		});
		machineTalkPanel.add(modeManual);		

		JButton modeAutomatic = new JButton("Mode: AUTOMATIC");
		modeAutomatic.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Settings.parMode = EmcTaskModeType.EMC_TASK_MODE_AUTO;
				new ChangeMode().start();
			}
		});
		machineTalkPanel.add(modeAutomatic);			
		
		JButton homeAll = new JButton("Home ALL");
		homeAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Settings.parMode = EmcTaskModeType.EMC_TASK_MODE_MANUAL;
					new ChangeMode().start();
					Thread.sleep(1000);

					for (int i = 0; i < 4; i++) {
						Settings.parAxisNo = i;
						new HomeAxis().start();
						Thread.sleep(1000);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		machineTalkPanel.add(homeAll);

		SavableText mdiCommand1 = new SavableText();
		mdiCommand1.setLabelTxt("MDI1:");
		mdiCommand1.setParId("machinekit_mdi1");
		machineTalkPanel.add(mdiCommand1);
		mdiCommand1.jValue.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == 10) {
					try {
						Settings.parMode = EmcTaskModeType.EMC_TASK_MODE_MDI;
						new ChangeMode().start();
						Thread.sleep(1000);
						
						Settings.parMdiCommand = mdiCommand1.getParValue();
						new ExecuteMdi().start();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					//System.out.println(e.getKeyCode());
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		
		SavableText mdiCommand2 = new SavableText();
		mdiCommand2.setLabelTxt("MDI2:");
		mdiCommand2.setParId("machinekit_mdi2");
		machineTalkPanel.add(mdiCommand2);
		mdiCommand2.jValue.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == 10) {
					try {
						Settings.parMode = EmcTaskModeType.EMC_TASK_MODE_MDI;
						new ChangeMode().start();
						Thread.sleep(1000);
						
						Settings.parMdiCommand = mdiCommand2.getParValue();
						new ExecuteMdi().start();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					//System.out.println(e.getKeyCode());
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});		
		
		
	}
}