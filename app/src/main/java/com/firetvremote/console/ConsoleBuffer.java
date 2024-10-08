package com.firetvremote.console;


public class ConsoleBuffer {
	private char[] buffer;
	private int amountPopulated;
	
	public ConsoleBuffer(int bufferSize)
	{
		buffer = new char[bufferSize];
		amountPopulated = 0;
	}
	
	public synchronized void append(byte[] asciiData, int offset, int length)
	{
		if (amountPopulated + length > buffer.length)
		{
			/* Move the old data backwards */
			System.arraycopy(buffer,
					length,
					buffer,
					0,
					amountPopulated - length);

			amountPopulated -= length;
		}
		
		for (int i = 0; i < length; i++)
		{
			buffer[amountPopulated++] = (char)asciiData[offset+i];
		}
	}
}
