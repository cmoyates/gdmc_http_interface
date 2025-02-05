package com.gdmc.httpinterfacemod.commands;

import com.gdmc.httpinterfacemod.GdmcHttpServer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class GetHttpInterfacePort {

	private static final String COMMAND_NAME = "gethttpport";
	private GetHttpInterfacePort() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands.literal(COMMAND_NAME)
			.executes(context -> perform(context))
		);
	}

	private static int perform(CommandContext<CommandSourceStack> commandSourceContext) {
		int currentPort = GdmcHttpServer.getCurrentHttpPort();
		commandSourceContext.getSource().sendSuccess(Component.nullToEmpty(
			String.valueOf(currentPort)
		), true);
		return currentPort;
	}
}
