package test;

import bence.sipka.cmd.api.Command;
import bence.sipka.cmd.api.ParameterContext;

@Command
public class MainCommand {
	@ParameterContext
	public SubParams p;
}
