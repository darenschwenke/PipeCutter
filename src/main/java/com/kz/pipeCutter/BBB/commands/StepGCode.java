package com.kz.pipeCutter.BBB.commands;

import pb.Message.Container;

import com.kz.pipeCutter.BBB.BBBMachineTalkCommand;

import pb.Status;
import pb.Status.EmcTaskModeType;
import pb.Types.ContainerType;

public class StepGCode extends BBBMachineTalkCommand {

	public Container prepareContainer() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder  builder = Container.newBuilder();
		builder.setType(ContainerType.MT_EMC_TASK_PLAN_STEP);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);
		Container container = builder.build();

		return container;
	}

}
