package com.aternity.tupleBuilder.ArcTuple;


import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
public class BasicArcMessage extends OutgoingSimulatorMessage {

	private String file;
    private String data;
	
	public static enum HttpMessages {
              report("/Httpagent/Report", 'R'),
              tuple("/Httpagent/Tuple", 'x'),  // for ARC this is small letter. Non-arc would be capital letter 'X'...
              UnmonitoredUsers("/Httpagent/UnmonitoredUsers", 'U'),
              keepAlive("/Httpagent/KeepAlive", 'M'),
              registerSession("/Httpagent/RegisterSession", 'K'),
              EventLog("/Httpagent/EventLog", 'E');

      private String url;
      char messageId;

      HttpMessages(String url, char messageId) {
          this.url = url;
          this.messageId = messageId;
      }

      public String getUrl() {
          return url;
      }

      public char getMessageId() {
          return messageId;
      }
  }
	
	public BasicArcMessage(char messageId, boolean addMessageLength, String file)throws IOException {
		  super((byte)messageId, addMessageLength);
	      this.file = file;
	}

    public BasicArcMessage(char messageId, boolean addMessageLength, String file, String data)throws IOException {
        super((byte)messageId, addMessageLength);
        this.file = file;
        this.data = data;
    }

	public byte[] encode(String content) throws IOException {
		int pos1 = length;
        writeBytes(new byte[4]);//the index in the message - 5.
        try {
            writeStringIntPrefix(content);
        } catch (Exception e) {
            return null;
        }

        byte[] bytes = super.encode();

        byte[] lengthBytes = getIntegerBytes(length - pos1 - 4);
        System.arraycopy(lengthBytes, 0, bytes, pos1 + 4, 4);
        return bytes;
	 }
	
	@Override
    public byte[] encode() throws IOException {
        //write the message length.
        int pos1 = length;
        writeBytes(new byte[4]);//the index in the message - 5.

        try {
            String fileStr ;
            if (file != null)
                fileStr= FileUtils.readFileToString(new File(file));
            else
                fileStr= data;
            writeStringIntPrefix(fileStr);

        } catch (Exception e) {
            return null;
        }

        byte[] bytes = super.encode();

        byte[] lengthBytes = getIntegerBytes(length - pos1 - 4);
        System.arraycopy(lengthBytes, 0, bytes, pos1 + 4, 4);
        return bytes;
    }

}

