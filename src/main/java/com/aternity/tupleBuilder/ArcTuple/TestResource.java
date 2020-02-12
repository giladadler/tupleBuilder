package com.aternity.tupleBuilder.ArcTuple;


import java.io.File;
import java.util.Map;


public class TestResource {

	private String relativePath;

	
	public TestResource(String relativePath) {
		super();
		this.relativePath = relativePath;
	}

	/**
     * This function gets absolute resources path
     * @return absolutePath
     */
	public String getAbsolutePath() throws Exception {
		String absolutePath ="NA";
		if (relativePath.startsWith("/") || ( relativePath.startsWith("\\") && !relativePath.startsWith("\\\\"))) { //remove leading slash
			relativePath = relativePath.substring(1);
		} 
		if (relativePath.contains(":") || relativePath.contains("\\\\")){
			absolutePath = relativePath;
		}
		return absolutePath;
	}

	/**
     * This function gets absolute resources path while transform the file with Freemarker processing
     * 
     * @param parameters HashMap of key-value to be used by Freemarker processing
     * @return absolutePath
     */
	public String getAbsolutePath(Map<?, ?> parameters) throws Exception {
		String sourceUri =  new File(getAbsolutePath()).getParent(); // get containing folder
		String fileName = new File(getAbsolutePath()).getName();
		String fileExtention = fileName.substring(fileName.indexOf("."));
		String destinationUri = File.createTempFile(fileName.substring(0, fileName.indexOf(".")), fileExtention).getAbsolutePath();
		FreemarkerUtil.transformFileToFile(sourceUri, fileName, parameters, destinationUri);
		return destinationUri;		
	}
	
	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
}

