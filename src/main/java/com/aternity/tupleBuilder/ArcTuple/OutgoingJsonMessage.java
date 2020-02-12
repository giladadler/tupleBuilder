package com.aternity.tupleBuilder.ArcTuple;


import java.io.IOException;


public class OutgoingJsonMessage extends OutgoingSimulatorMessage {

    private String file;
   

    public static enum HttpMessages {
        report("/Httpagent/Report", 'R'),
        reportUserStatics("/Httpagent/Report", 't'), // http://wiki/wiki/index.php/Agent_NG_-_Server_Communication_Layer#Users_Statics_Report_.28agent.2Fserver_8.1.2B.29_.28Outgoing.29
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


    public OutgoingJsonMessage(char messageId, boolean addMessageLength, String fileStr)
            throws IOException {
        super((byte) messageId, addMessageLength);
        this.file = fileStr;
    }

    @Override
    public byte[] encode(String fileStr) throws IOException {
        //write the message length.
        int pos1 = length;
        writeBytes(new byte[4]);//the index in the message - 5.


        try {
            writeStringIntPrefix(fileStr);

        } catch (Exception e) {
        }

        byte[] bytes = super.encode();

        byte[] lengthBytes = getIntegerBytes(length - pos1 - 4);
        System.arraycopy(lengthBytes, 0, bytes, pos1 + 4, 4);
        return bytes;
    }
}

