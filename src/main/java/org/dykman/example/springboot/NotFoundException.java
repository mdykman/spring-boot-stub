package org.dykman.example.springboot;

@SuppressWarnings("serial")
public class NotFoundException extends Exception {

	private String missingResource = null;

	public String getMissingResource() {
		return missingResource;
	}

	public NotFoundException(String arg0) {
		super(arg0 + " not found");
		missingResource = arg0;
	}

}
