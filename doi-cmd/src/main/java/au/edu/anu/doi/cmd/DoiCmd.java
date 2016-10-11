/*******************************************************************************
 * Australian National University Data Commons
 * Copyright (C) 2013  The Australian National University
 * 
 * This file is part of Australian National University Data Commons.
 * 
 * Australian National University Data Commons is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package au.edu.anu.doi.cmd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import au.edu.anu.doi.api.DoiException;
import au.edu.anu.doi.api.DoiService;
import au.edu.anu.doi.api.config.DoiConfig;
import au.edu.anu.doi.api.config.DoiConfigFile;
import au.edu.anu.doi.api.response.DoiResponse;

/**
 * @author Rahul Khanna
 *
 */
public class DoiCmd {
	private String[] args;
	private Options options;
	private CommandLine cmdLine;
	private DoiService doiSvc;

	public DoiCmd(String[] args) {
		this.args = args;
	}

	public void run() throws DoiCmdException {
		options = createOptions();
		cmdLine = parseCommandLine(options, args);

		if (args.length == 0 || cmdLine.hasOption("h")) {
			dispUsageHelp();
		} else {
			// read properties
			if (!cmdLine.hasOption("config")) {
				dispUsageHelp();
				return;
			}
			DoiConfig doiConfig = readConfig();

			// create DOI Service client
			Client client = ClientBuilder.newClient();
			
			client.register(new HttpLoggingFilter());
			doiSvc = new DoiService(client, doiConfig);

			if (args[0].equals("status")) {
				execGetServiceStatus();
			} else if (args[0].equals("metadata")) {
				execGetMetadata();
			} else if (args[0].equals("mint")) {
				execMint();
			} else if (args[0].equals("update")) {
				execUpdate();
			} else if (args[0].equals("activate")) {
				execActivate();
			} else if (args[0].equals("deactivate")) {
				execDeactivate();
			}
		}
	}

	private void execGetServiceStatus() throws DoiCmdException {
		try {
			DoiResponse serviceStatusResp = doiSvc.getServiceStatus();
			System.out.println(serviceStatusResp);
		} catch (DoiException e) {
			throw new DoiCmdException(e);
		}
	}

	private void execGetMetadata() throws DoiCmdException {
		if (!cmdLine.hasOption("doi")) {
			dispUsageHelp();
			return;
		}
		
		try {
			String serviceStatusResp = doiSvc.getMetadata(cmdLine.getOptionValue("doi"));
			
			// if file specified on command line, save xml to that file, else write to stdout
			if (cmdLine.hasOption("file")) {
				Path outputFile = Paths.get(cmdLine.getOptionValue("file"));
				try (BufferedWriter metadataWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					metadataWriter.write(serviceStatusResp);
				}
			} else {
				System.out.println(serviceStatusResp);
			}
		} catch (DoiException | IOException e) {
			throw new DoiCmdException(e);
		}
	}

	private void execMint() throws DoiCmdException {
		// url that doi will resolve to and metadata for that doi are mandatory
		if (!cmdLine.hasOption("url") || !cmdLine.hasOption("file")) {
			dispUsageHelp();
			return;
		}
		
		String doiUrl = cmdLine.getOptionValue("url", null);
		String metadataFile = cmdLine.getOptionValue("file", null);
		StringBuilder resourceDoc = new StringBuilder();

		if (metadataFile != null) {
			BufferedReader metadataReader;
			try {
				if (!metadataFile.equals("-")) {
					// point reader to file
					metadataReader = Files.newBufferedReader(Paths.get(metadataFile), StandardCharsets.UTF_8);
				} else {
					// point reader to stdin
					metadataReader = new BufferedReader(new InputStreamReader(System.in));
				}

				char[] buffer = new char[8192];
				for (int nCharsRead = metadataReader.read(buffer); nCharsRead != -1; nCharsRead = metadataReader
						.read(buffer)) {
					resourceDoc.append(buffer, 0, nCharsRead);
				}

				// close stream if non-file
				if (!metadataFile.equals("-")) {
					IOUtils.closeQuietly(metadataReader);
				}
				
				doiSvc.mint(doiUrl, resourceDoc.toString());
			} catch (IOException | DoiException e) {
				throw new DoiCmdException(e);
			}
		}

	}

	/**
	 * Execute DOI update.
	 * 
	 * @throws DoiCmdException
	 */
	private void execUpdate() throws DoiCmdException {
		if (!cmdLine.hasOption("doi")) {
			dispUsageHelp();
			return;
		}

		// an update requires either a metadata update, a url update or both
		if (!cmdLine.hasOption("file") && cmdLine.hasOption("url")) {
			dispUsageHelp();
			return;
		}

		String doi = cmdLine.getOptionValue("doi");
		String doiUrl = cmdLine.getOptionValue("url", null);
		String metadataFile = cmdLine.getOptionValue("file", null);
		StringBuilder resourceDoc = new StringBuilder();
		if (metadataFile != null) {
			BufferedReader metadataReader;
			try {
				if (!metadataFile.equals("-")) {
					// point reader to file
					metadataReader = Files.newBufferedReader(Paths.get(metadataFile), StandardCharsets.UTF_8);
				} else {
					// point reader to stdin
					metadataReader = new BufferedReader(new InputStreamReader(System.in));
				}

				char[] buffer = new char[8192];
				for (int nCharsRead = metadataReader.read(buffer); nCharsRead != -1; nCharsRead = metadataReader
						.read(buffer)) {
					resourceDoc.append(buffer, 0, nCharsRead);
				}

				// close stream if non-file
				if (!metadataFile.equals("-")) {
					IOUtils.closeQuietly(metadataReader);
				}
				
				doiSvc.update(doi, doiUrl, resourceDoc.toString());
			} catch (IOException | DoiException e) {
				throw new DoiCmdException(e);
			}
		}

	}

	private void execActivate() {
		// TODO Auto-generated method stub
		
	}

	private void execDeactivate() {
		// TODO Auto-generated method stub
		
	}

	private DoiConfigFile readConfig() throws DoiCmdException {
		File configFile = new File(cmdLine.getOptionValue("config"));
		DoiConfigFile doiConfigFile;
		try {
			doiConfigFile = new DoiConfigFile(configFile);
		} catch (IOException e) {
			throw new DoiCmdException(e);
		}
		return doiConfigFile;
	}

	private void dispUsageHelp() {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("doi-cmd [status | metadata | mint | update | activate | deactivate]", options);
	}

	private CommandLine parseCommandLine(Options options, String[] args) {
		CommandLine cmd;
		CommandLineParser parser = new DefaultParser();
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			try {
				cmd = parser.parse(options, new String[] { "-h" });
			} catch (ParseException e1) {
				throw new RuntimeException(e1);
			}
		}
		return cmd;
	}

	private Options createOptions() {
		Options options = new Options();

		Option help = new Option("h", "help", false, "display help");

		Option doi = new Option("d", "doi", true, "doi on which to perform chosen action");
		Option config = new Option("c", "config", true, "location of config file");
		Option url = new Option("u", "url", true, "url the DOI resolves to");
		Option file = new Option("f", "file", true, "file to read/write metadata as XML to (defaults to stdin/stdout)");

		options.addOption(help);
		options.addOption(doi);
		options.addOption(config);
		options.addOption(url);
		options.addOption(file);
		
		return options;
	}
	
	private final class HttpLoggingFilter implements ClientRequestFilter, ClientResponseFilter {

		@Override
		public void filter(ClientRequestContext requestContext) throws IOException {
			// request method and url
			System.out.printf("> %s %s", requestContext.getMethod(), requestContext.getUri().toString());
			System.out.println();
			
			// http headers
			MultivaluedMap<String,String> headers = requestContext.getStringHeaders();
			for (Entry<String, List<String>> header : headers.entrySet()) {
				for (String value : header.getValue()) {
					System.out.printf("> %s: %s", header.getKey(), value);
					System.out.println();
				}
			}
			
		}

		@Override
		public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
				throws IOException {
			// http headers
			MultivaluedMap<String,String> headers = responseContext.getHeaders();
			for (Entry<String, List<String>> header : headers.entrySet()) {
				for (String value : header.getValue()) {
					System.out.println(String.format("< %s: %s", header.getKey(), value));
				}
			}
			
			
		}
		
	}
}
