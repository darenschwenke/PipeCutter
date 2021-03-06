package com.kz.pipeCutter.ui;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class MyWebsocketClient {
	public Positioner positioner;
	private URI uri;

	public MyWebsocketClient(Positioner positioner) {
		// TODO Auto-generated constructor stub
		this.positioner = positioner;
	}

	@OnOpen
	public void onOpen(Session session) {
		this.uri = session.getRequestURI();
		if(Settings.getInstance()!=null)
			Settings.getInstance().log("\tConnected to: " + uri);
		this.positioner.isConnected = true;
		
		String reassignString = Settings.getInstance().getSetting("rotator_" + this.positioner.id + "_reassign");
		try {
			if(reassignString!=null && !reassignString.equals(""))
			session.getBasicRemote().sendText("reassign " + reassignString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@OnMessage
	public void onMessage(String message) {
		String res[] = message.split(" ");
		if (message.substring(0, 1).equals("X") && res.length == 5) {
			positioner.x = Long.valueOf(res[0].substring(1, res[0].length()));
			positioner.y = Long.valueOf(res[1].substring(1, res[1].length()));
			positioner.z = Long.valueOf(res[2].substring(1, res[2].length()));
			positioner.e = Long.valueOf(res[3].substring(1, res[3].length()));
			if (res[4].endsWith("1"))
			{
				positioner.m = true;
				positioner.linkedJogEnableCheckBox.setParValue("True");
			}
			else
			{
				positioner.m = false;
				positioner.linkedJogEnableCheckBox.setParValue("False");
			}
			positioner.initToolTips();
		}
	}

	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	@OnClose
	public void onClose() {
		if(Settings.getInstance()!=null)
			Settings.getInstance().log("\tDisconnected from: " + uri);
		positioner.isConnected = false;
	}

}