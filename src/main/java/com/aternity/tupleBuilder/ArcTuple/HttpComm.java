package com.aternity.tupleBuilder.ArcTuple;


import com.aternity.agentSimulator.simulator.Configuration;
import com.aternity.agentSimulator.simulator.commManager.http.HttpMessageType;
import com.aternity.agentSimulator.simulator.commManager.mobile.MobileRequest;

import com.aternity.agentSimulator.simulator.messages.incomingMessages.IncomingMacIdMessage;
import com.aternity.agentSimulator.simulator.messages.incomingMessages.IncomingMessage;
import com.aternity.agentSimulator.simulator.messages.outgoingMessages.OutgoingMacRegisterMessage;
import com.aternity.epm.httpagent.messages.outgoing.serializers.OutgoingMessagesSerializer;

import com.aternity.tupleBuilder.utils.SslHelper;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.params.HttpClientParams;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.aternity.agentSimulator.simulator.commManager.http.HttpCommManager.prepareBaseUrl;
import static com.aternity.agentSimulator.simulator.messages.incomingMessages.IncomingConfigurationMessage.extractConfigurationFromResponse;

/**
 * Created with IntelliJ IDEA. User: seagull Date: 01/07/14 Time: 10:25 To
 * change this template use File | Settings | File Templates.
 */
public class HttpComm {
	private static Logger LOG = LoggerFactory.getLogger(HttpComm.class);

	public static String DEFAULT_URL = "http://localhost/Management/Httpagent/";

	private static final int BYTE_MAX_SIZE = 256;

	private String url;

	private boolean isTrace = false;

	public void setTrace(boolean trace) {
		isTrace = trace;
	}

	public static void init(boolean isSsl) {
        if(isSsl){
            SslHelper.trustSelfSignedSSL();
        }
        initHttpClient();
    }
	
	 private static HttpClient httpClient;

	public static Integer sendHttpMacConnMessage(int agentId, String epmIp, Integer port, String agentVersionValue, String clientHostname, HashMap<Integer, List<String>> staticAttributes, String accountKey) throws Exception {
		HashMap<String, Object> params = new HashMap();
		String retMsg = prepareHttpCom(epmIp, port.intValue(), HttpMessageType.GetHandshakeAttributes).sendRequestByText(params);
		if (retMsg != null && retMsg.length() >= 1) {
			params.put("agentVersion", agentVersionValue);
			if(accountKey != null){
				params.put("account", accountKey);
			}
			String jsonMessageBody = (new OutgoingMacRegisterMessage(staticAttributes)).getMessage();
			int max = 4;
			byte[] RegisterResponse = new byte[0];

			for(int i = 0; i < max; ++i) {
				RegisterResponse = prepareHttpCom(epmIp, port.intValue(), HttpMessageType.RegisterAgent).sendRequest(params, jsonMessageBody.getBytes());
				if (RegisterResponse == null) {
					Thread.sleep(1000L);
				} else {
					i = 100;
				}
			}

			IncomingMacIdMessage message = new IncomingMacIdMessage();

			try {
				Integer agentIdFromEpm = message.getEpId(RegisterResponse);
				if (agentIdFromEpm.intValue() < 0) {
					return agentIdFromEpm;
				} else {
					return agentIdFromEpm;
				}
			} catch (Exception var15) {
				throw var15;
			}
		} else {
			throw new Exception("sendHttpMacConnMessage: did not get handshake attributes list from server for agentLocalId=" + agentId + ", version=" + agentVersionValue);
		}
	}

	public static com.aternity.agentSimulator.simulator.commManager.http.HttpComm prepareHttpCom(String epmIp, int port, HttpMessageType httpMessageType) {
		com.aternity.agentSimulator.simulator.commManager.http.HttpComm httpComm = null;

		try {
			String httpEpmUrl = prepareBaseUrl(epmIp, port);
			httpComm = new com.aternity.agentSimulator.simulator.commManager.http.HttpComm(httpEpmUrl + httpMessageType);
		} catch (Throwable var5) {
		}

		return httpComm;
	}
	 public static void initHttpClient() {
	        MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();
	        mgr.getParams().setDefaultMaxConnectionsPerHost(1);
	        mgr.getParams().setMaxTotalConnections(1);

	        // Trying to keep connection open
	        mgr.getParams().setLinger(5);
	        mgr.getParams().setConnectionTimeout(1000);
	        mgr.getParams().setSoTimeout(1000);

	        httpClient = new HttpClient(mgr);

	        HttpClientParams httpClientParams = new HttpClientParams();
	        httpClientParams.setSoTimeout(1000);
	        httpClient.setParams(httpClientParams);
	    }
	public HttpComm() {
		this(DEFAULT_URL);
	}

	public HttpComm(String url) {
		this.url = url;
	}

	public void sendRequest(Map<String, String> params) {
		sendRequest(params, null);
	}

	public void sendRequest(Map<String, String> params, byte[] data) {
		sendRequest(null, params, data, null);
	}

	public void sendRequest(String urlSuffix, Map<String, String> params, byte[] data, ResponseParser responseParser) {
		String urlToCall = url;
		if (StringUtils.isNotEmpty(urlSuffix)) {
			urlToCall = urlToCall + urlSuffix;
		}
		invokeCall(getHttpClient(), urlToCall, params, data, responseParser);
	}

	public HttpClient getHttpClient() {
		initHttpClient();
		return httpClient;
	}

	public static void log(String msg) {
		System.out.println(new Date() + "||" + msg);
	}

	private void  invokeCall(HttpClient client, String url, Map<String, String> params, byte[] data,
			ResponseParser responseParser) {

		HttpMethodBase method = null;
		if (params == null) {
			method = new GetMethod(url);
		} else {

			List<Part> parts = new ArrayList<Part>();
			for (String paramName : params.keySet()) {
				parts.add(new StringPart(paramName, params.get(paramName)));
			}

			if (Configuration.accountKey != null) {
				String connector = (url.contains("?")) ? "&" : "?";
				url += connector + MobileRequest.ParamNames.account.name() + "=" + Configuration.accountKey;
			}

			log(getClass().getName() + "::Calling Url[" + url + "] with params[" + params + "]");

			if (data != null && data.length > 0) {
				parts.add(new FilePart("msgData", new ByteArrayPartSource("msgData", data), "application/octet-stream",
						"UTF-8"));
			}

			// Create a post instance.
			PostMethod post = new PostMethod(url);
			method = post;
			// method.setRequestHeader("Accept", "application/octet-stream");

			// method.setRequestHeader("Content-Type", "multipart/form-data");

			MultipartRequestEntity requestEntity = new MultipartRequestEntity(parts.toArray(new Part[parts.size()]),
					post.getParams());
			post.setRequestEntity(requestEntity);
			// post.setRequestEntity(new
			// ByteArrayRequestEntity(data,"application/octet-stream"));

		}

		try {
			// method.setRequestHeader("Accept-Charset","UTF-8");
			// Execute the post.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + method.getStatusLine());
			}
			byte[] responseBody = null;
			responseBody = method.getResponseBody();
			if (isTrace) {
				log("Response Content Lenth:" + method.getResponseContentLength());
			}

			Header requestHeader = method.getResponseHeader("Content-Type");
			if (responseBody.length > 0) {


				if (OutgoingMessagesSerializer.ContentType.BINARY.getHttpContentType().equals(requestHeader.getValue())) {
					DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(responseBody));
					byte[] msgLenByteArr = new byte[4];
					dataInputStream.read(msgLenByteArr);
					int msgLength = getMsgLength(msgLenByteArr);
					byte opCode = dataInputStream.readByte();
					String opCodeStr = new String(new byte[] { opCode });
					log(getClass().getName() + "::Got Msg Length - " + msgLength + "| Op Code - " + opCodeStr);

					// Deal with the response.
					// Use caution: ensure correct character encoding and is not
					// binary data
					byte[] subarray = ArrayUtils.subarray(responseBody, 5, responseBody.length);
					if (responseParser != null) {
						responseParser.parse(subarray);
					}
				} else if (OutgoingMessagesSerializer.ContentType.XML.getHttpContentType()
						.equals(requestHeader.getValue())) {
					log(getClass().getName() + "::Got response:" + new String(responseBody));
					if (responseParser != null) {
						responseParser.parse(responseBody);
					}
				}

			} else {
				if (isTrace) {
					log("Response Body is Empty");
				}
			}

		} catch (Exception e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Release the connection.
			// method.releaseConnection();
		}
	}

	public String  invokeGenericJsonCall( String url, Map<String, String> params, byte[] data, boolean isZip) {

		HttpMethodBase method = null;
		byte[] responseBody = null;
		String responseStr = "Empty Response";
		if (params == null) {
			method = new GetMethod(url);
		} else {

			List<Part> parts = new ArrayList<Part>();
			for (String paramName : params.keySet()) {
				parts.add(new StringPart(paramName, params.get(paramName)));
			}

			if (Configuration.accountKey != null && !url.contains(MobileRequest.ParamNames.account.name())) {
				String connector = (url.contains("?")) ? "&" : "?";
				url += connector + MobileRequest.ParamNames.account.name() + "=" + Configuration.accountKey;
			}

			log(getClass().getName() + "::Calling Url[" + url + "] with params[" + params + "]");

			if (data != null && data.length > 0) {
				parts.add(new FilePart("msgData", new ByteArrayPartSource("msgData", data), "application/octet-stream",
						"UTF-8"));
			}

			// Create a post instance.
			PostMethod post = new PostMethod(url);
			method = post;
			// method.setRequestHeader("Accept", "application/octet-stream");

			// method.setRequestHeader("Content-Type", "multipart/form-data");

			MultipartRequestEntity requestEntity = new MultipartRequestEntity(parts.toArray(new Part[parts.size()]),
					post.getParams());
			post.setRequestEntity(requestEntity);
			// post.setRequestEntity(new
			// ByteArrayRequestEntity(data,"application/octet-stream"));

		}

		try {
			// method.setRequestHeader("Accept-Charset","UTF-8");
			// Execute the post.
			HttpClient client = getHttpClient();
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + method.getStatusLine());
				responseStr = ("Method failed: " + method.getStatusLine());
			}

			responseBody = method.getResponseBody();
			if (isTrace) {
				log("Response Content Lenth:" + method.getResponseContentLength());
			}

			Header requestHeader = method.getResponseHeader("Content-Type");
			if (responseBody.length > 0) {


						if (OutgoingMessagesSerializer.ContentType.BINARY.getHttpContentType().equals(requestHeader.getValue())) {
				//if (true) {
					DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(responseBody));
					byte[] resForParse = Arrays.copyOfRange(responseBody, 4, responseBody.length);
					IncomingMessage incomingMessage = IncomingMessage.construct(1, resForParse);
					responseStr = "Got BINARY response : "  + incomingMessage;
					if (incomingMessage == null){
						byte[] tmp = Arrays.copyOfRange(responseBody, 9, responseBody.length);
						responseStr = new String(tmp);
						//responseStr = responseStr.substring(responseStr.indexOf("{"));
					}
					if (isZip){
						responseStr = extractConfigurationFromResponse(responseBody);
					}

				} else if (OutgoingMessagesSerializer.ContentType.XML.getHttpContentType()
						.equals(requestHeader.getValue())) {
					responseStr = "Got XML response:" + new String(responseBody);
				}

			}

		} catch (Exception e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Release the connection.
			// method.releaseConnection();
		}
		return responseStr;
	}
	/**
	 * Text, not binary. For Mobile
	 *
	 * @param url
	 * @param params
	 * @param client
	 * @return
	 */
	public String invokeCall(String url, Map<String, Object> params, HttpClient client) {
		HttpMethodBase method = null;
		String responseBody = null;
		boolean firstTime = true;
		long counterCopy;
		long counterLoopCopy;
		if (params == null) {
			method = new GetMethod(url);
		} else {

			List<Part> parts = new ArrayList<Part>();
			for (String paramName : params.keySet()) {
				Object pval = params.get(paramName);
				if (pval instanceof String) {
					String s = (String) pval;
					if (paramName.equals(MobileRequest.ParamNames.attributes.name())
							|| paramName.equals(MobileRequest.ParamNames.events.name())
							|| paramName.equals(MobileRequest.ParamNames.breadcrumb.name())) {
						parts.add(new StringPart(paramName, s));
					} else {
						String connector = (firstTime ? "?" : "&");
						url = url + connector + paramName + "=" + s;
						firstTime = false;
					}

				} else if (pval instanceof byte[]) {

					PartSource partSource = getBinaryPartSource((byte[]) pval);
					parts.add(new FilePart(paramName, partSource));
				}

			}

			// Example from real from tomcat log:
			// 192.168.111.143 - - [28/Jan/2015:14:59:30 +0200] "POST
			// /Httpagent/Tuple?epId=4321&account=71db315b9a3ac71b HTTP/1.1" 200
			// 22
			if (Configuration.accountKey != null) {
				url += "&" + MobileRequest.ParamNames.account.name() + "=" + Configuration.accountKey;
			}
			// Create a post instance.
			PostMethod post = new PostMethod(url);
			method = post;
			MultipartRequestEntity requestEntity = new MultipartRequestEntity(parts.toArray(new Part[parts.size()]),
					post.getParams());
			post.setRequestEntity(requestEntity);
		}

		try {
			// method.setRequestHeader("Accept-Charset","UTF-8");

			long timeBeforeExecute = System.currentTimeMillis();

			// Execute the post.
			int statusCode = client.executeMethod(method);

			// Read the response body.
			responseBody = method.getResponseBodyAsString();

			if (statusCode != HttpStatus.SC_OK) {

				Header location = method.getResponseHeader("Location");
				if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
					;
				} else if ((responseBody != null) && (responseBody.contains("already exists"))) {
					;
				} else {
					String message = "Method failed: " + method.getStatusLine() + "\nlocationHeader=" + location
							+ "\nurl=" + url + "\nparams=";
					throw new Exception(message);
				}
			} else {

				/*
				 * {"instructions":{"Configuration":{"cfg_ver":"0","General":{},
				 * "Operational":{"LogLevel":"Default","Mode":"Disabled"},
				 * "Monitoring":{"EventFilters":{"INCLUDE_REPORTS":[]}}}}}
				 */

				if (responseBody.contains("\"Mode\":\"Stopped\"") || responseBody.contains("\"Mode\":\"Disabled\"")) {
					String message = "Ep is not active. Response body received from server: " + responseBody;
					throw new Exception(message);
				}

			}

		} catch (Exception e) {

		} finally {
			// Release the connection.
			method.releaseConnection();
		}
		return responseBody;
	}

	private PartSource getBinaryPartSource(byte[] pval) {
		final byte[] val = pval;
		return new PartSource() {
			@Override
			public long getLength() {
				return val.length;
			}

			@Override
			public String getFileName() {
				return "dummy";
			}

			@Override
			public InputStream createInputStream() throws IOException {
				return new ByteArrayInputStream(val);
			}
		};
	}

	private static int getMsgLength(byte[] msgBody) {
		int a = msgBody[0];
		if (a < 0) {
			a += BYTE_MAX_SIZE;
		}
		int b = msgBody[1];
		if (b < 0) {
			b += BYTE_MAX_SIZE;
		}
		int c = msgBody[2];
		if (c < 0) {
			c += BYTE_MAX_SIZE;
		}
		int d = msgBody[3];
		if (d < 0) {
			d += BYTE_MAX_SIZE;
		}
		return a + (b << 8) + (c << 16) + (d << 24);

	}

}
