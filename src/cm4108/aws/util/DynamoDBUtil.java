package cm4108.aws.util;

//AWS SDK

import com.amazonaws.client.builder.*;
import com.amazonaws.client.builder.AwsClientBuilder.*;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.datamodeling.*;

public class DynamoDBUtil {
    private static AmazonDynamoDB dbClient = null;        //a reuable DynamoDBClient
    private static DynamoDBMapper mapper = null;        //a reusable DynamoDBMapper

    /**
     * This method provides a handy way to get a <a href="http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/dynamodbv2/datamodeling/DynamoDBMapper.html">DynamoDBMapper</a> object.
     * The same object is reused in different requests.
     *
     * @param region The AWS region to connect to. e.g. "eu-west-1".
     *               To connect to a local server, use "local".
     * @parm endPoint    The URL of the local DynamoDB server. e.g. http://localhost:8000
     * This parameter is only used if region is specified as "local".
     * @return A DynamoDBMapper object for accessing DynamoDB.
     */
    public static DynamoDBMapper getDBMapper(String region, String endPoint) {
        if (DynamoDBUtil.mapper == null)    //no mapper yet
        {
            DynamoDBUtil.dbClient = getDynamoDBClient(region, endPoint);    //get DynamoDB client
            DynamoDBUtil.mapper = new DynamoDBMapper(dbClient);    //create DynamoDBMapper object
        }
        return DynamoDBUtil.mapper;
    } //end method

    /**
     * Return a DynamoDB client.
     *
     * @return
     */
    public static AmazonDynamoDB getDynamoDBClient(String region, String endPoint) {
        if (DynamoDBUtil.dbClient != null)        //if there is already a client
            return DynamoDBUtil.dbClient;    //return it

//otherwise create one

        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();    //create a client builder
        if (region.equals("local")) {
            //
            //if using local DynamoDB server, set endpoint to given URL.
            //
            EndpointConfiguration epConfig = new AwsClientBuilder.EndpointConfiguration(endPoint, region);
            builder.setEndpointConfiguration(epConfig);
        } else builder.setRegion(region);    //otherwise set builder to use specified region

        return builder.build();    //build and return +DynamoDB client
    } //end method
} //end class
