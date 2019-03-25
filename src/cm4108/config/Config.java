package cm4108.config;

public class Config {
    //
// table name in DynamoDB
//
    public static final String DYNAMODB_TABLE_NAME = "cm4108-cw";
    //
// AWS Region. Refer to API to see what regions are available.
// *** To use a local server, set this to "local". ***
//
    public static final String REGION = "local";
    public static final String LOCAL_ENDPOINT = "http://localhost:8000";
} //end class
