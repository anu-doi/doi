package au.edu.anu.doi.cmd;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

public class HttpLoggingFilter implements ClientRequestFilter, ClientResponseFilter {

	private static final String[] HEADERS_TO_SKIP = { "Set-Cookie" };
	
	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		// request method and url
		System.out.printf("> %s %s", requestContext.getMethod(), requestContext.getUri().toString());
		System.out.println();
		
		// http headers
		MultivaluedMap<String,String> headers = requestContext.getStringHeaders();
		for (Entry<String, List<String>> header : headers.entrySet()) {
			if (!skipHeader(header.getKey())) {
				for (String value : header.getValue()) {
					System.out.printf("> %s: %s", header.getKey(), value);
					System.out.println();
				}
			}
		}
		
	}

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
			throws IOException {
		// http headers
		MultivaluedMap<String,String> headers = responseContext.getHeaders();
		for (Entry<String, List<String>> header : headers.entrySet()) {
			if (!skipHeader(header.getKey())) {
				for (String value : header.getValue()) {
					System.out.println(String.format("< %s: %s", header.getKey(), value));
				}
			}
		}
	}
	
	private boolean skipHeader(String header) {
		Objects.requireNonNull(header);
		for (String iHeaderToSkip : HEADERS_TO_SKIP) {
			if (header.equalsIgnoreCase(iHeaderToSkip)) {
				return true;
			}
		}
		return false;
	}
}