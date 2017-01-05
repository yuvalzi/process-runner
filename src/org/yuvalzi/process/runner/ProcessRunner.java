package org.yuvalzi.process.runner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.yuvalzi.deletedir.FileUtils;

/**
 * Automates an interactive shell process
 */
public class ProcessRunner {

	private String[] command;
//	ThreadGroup threads = new ThreadGroup(this.getClass().getName());
	Process proc;
	private OutputStream outputStream;
	private OutputStream errorStream;
	private Boolean inheritIO = true;
	

	public ProcessRunner() {}
	
	public ProcessRunner(String[] command) {
		this.command = command;
	}

	public void setInheritIO(Boolean inheritIO) {
		if (this.outputStream != null || this.errorStream == null) {
			throw new RuntimeException("disambigous, cannot inherit IO while outputStream and errorStream where defined");
		}
		else {
			this.inheritIO = inheritIO;
		}
	}

	public void start() {
		try {
			Path TempDirectory = Files.createTempDirectory("yuvalzi.processRunner");
			File tmpDir = TempDirectory.toFile();
			ProcessBuilder pb = new ProcessBuilder(this.command);
			if (inheritIO) {
				pb.inheritIO();
			}
			//Set the running directory
			pb.directory(tmpDir);
			//Start the process
			this.proc = pb.start();
			if (inheritIO == false) {
				do {
					readProcessOutput(proc, outputStream);
					readProcessError(proc, errorStream);
				} while (this.proc.isAlive());
			}
			//read all the leftovers
			if (inheritIO == false) {
				readProcessOutput(proc, outputStream);
				readProcessError(proc, errorStream);
			}
			if (tmpDir.exists()) {
				FileUtils.deleteDirectory(tmpDir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String[] getCommand() {
		return command;
	}

	public void setCommand(String[] cmdArgs) {
		this.command = cmdArgs;
	}

//
//	/**
//	 * Kickstart the example
//	 */
	public static void main(String[] args) {
		String os = System.getenv("OS");
		String cmd;
		if (os.toLowerCase().contains("windows")) {
			cmd = "npm.cmd";
		}
		else {
			cmd = "npm";
		}
		
		String[] cmdArgs = {cmd, "install", "yuvalzi", "-g"};
		
		ProcessRunner pr = new ProcessRunner();
		pr.setCommand(cmdArgs);
		pr.setOutputStream(System.out);
		pr.setErrorStream(System.err);
		pr.start();
	}
		
	private void setErrorStream(OutputStream errorStream) {
		this.errorStream = errorStream;
		this.inheritIO = false;
	}

	private void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
		this.inheritIO = false;

	}

	private void readProcessOutput(Process proc, OutputStream destStream)
			throws IOException {
		writeToStream(proc.getInputStream(), destStream);
	}

	private void writeToStream(InputStream input, OutputStream destStream) {
		try {
			for (int i = 0; i < input.available(); i++) {
				byte[] bytes = new byte[input.available()];
				input.read(bytes);
				destStream.write(bytes);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void readProcessError(Process proc, OutputStream destStream) {
		writeToStream(proc.getErrorStream(), destStream);
	}
}