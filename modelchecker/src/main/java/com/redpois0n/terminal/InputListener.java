package com.redpois0n.terminal;

public abstract class InputListener {

	/**
	 * Called when a command is entered
	 * @param terminal The terminal
	 * @param c The character that was typed
	 */
	public abstract void processCommand(JTerminal terminal, String cmd);

	/**
	 * Called when Ctrl+C is pressed
	 * @param terminal The terminal
	 */
	public void onTerminate(JTerminal terminal) {}
}
