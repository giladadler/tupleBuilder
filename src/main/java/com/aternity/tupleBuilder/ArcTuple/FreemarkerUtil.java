package com.aternity.tupleBuilder.ArcTuple;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;


public class FreemarkerUtil {

	// constants
	protected static final String DEFAULT_FILE_ENCODING = "ISO-8859-1";
	protected static final String NUMBER_FORMAT = "0.######";



	/**
	 * This method copy File with freemarker processing
	 * 
	 * @param sourceUri
	 *            this is the folder where the source file exists
	 * @param newFileName
	 *            this is the file name
	 * @param parameters
	 *            this is the Map for freemarker processing
	 * @param destinationUri
	 *            This is full path of destination file to be copied
	 */
	public static void transformFileToFile(String sourceUri, String newFileName, Map<?, ?> parameters, String destinationUri) throws Exception {
		transformFileToFile(sourceUri, newFileName, parameters, destinationUri, "UTF-8");
	}

	public static void transformFileToFile(String sourceUri, String newFileName, Map<?, ?> parameters, String destinationUri, String contentEncoding) throws Exception {

		Writer outputWriter = null;
		try {
			Configuration configuration = null;
			configuration = new Configuration();

			if (contentEncoding == null) {
				contentEncoding = DEFAULT_FILE_ENCODING;
			}

			configuration.setEncoding(configuration.getLocale(), contentEncoding);
			configuration.setDirectoryForTemplateLoading(new File(sourceUri));
			configuration.setSetting("number_format", NUMBER_FORMAT);
			Template template = configuration.getTemplate(newFileName);
			File outputFile = new File(destinationUri);

			if (!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}

			outputWriter = new OutputStreamWriter(new FileOutputStream(outputFile), contentEncoding);

			template.process(parameters, outputWriter);

		} catch (Exception e) {
			throw e;
		} finally {
			if (outputWriter != null) {
				outputWriter.close();
			}
		}
	}
	
	public static String transformStringToString(String ftl, Map<?, ?> parameters) throws Exception {
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		stringLoader.putTemplate("tuples", ftl);		
		Configuration config=new Configuration();
		config.setTemplateLoader(stringLoader);
		config.setObjectWrapper(new DefaultObjectWrapper());
		Template template=config.getTemplate("tuples");
		StringWriter out=new StringWriter();
		template.process(parameters, out);
		return out.toString();
	}


}
