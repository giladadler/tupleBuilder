package com.aternity.tupleBuilder.ArcTuple;



/**
 * Created with IntelliJ IDEA.
 * User: seagull
 * Date: 01/07/14
 * Time: 10:27
 * To change this template use File | Settings | File Templates.
 */
public interface ResponseParser<T> {

    public T parse(byte[] message) throws Exception;

}

