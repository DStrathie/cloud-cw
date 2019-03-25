package cm4108.user.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import cm4108.config.*;

import java.util.List;

@DynamoDBTable(tableName = Config.DYNAMODB_TABLE_NAME)
public class User {
    private String name;
    private double longitude, latitude;
    private List<String> subscriptions;
    private List<String> subSent;
    private List<String> subRec;

    public User() {
    } //end method

    public User(String name, double longitude, double latitude, List<String> subscriptions, List<String> subSent, List<String> subRec) {
        this.setName(name);
        this.setLongitude(longitude);
        this.setLatitude(latitude);
        this.setSubscriptions(subscriptions);
        this.setSubSent(subSent);
        this.setSubRec(subRec);
    } //end method

    @DynamoDBHashKey(attributeName = "name")
    public String getName() {
        return name;
    } //end method

    public void setName(String name) {
        this.name = name;
    } //end method

    @DynamoDBAttribute(attributeName = "longitude")
    public double getLongitude() {
        return longitude;
    } //end method

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    } //end method

    @DynamoDBAttribute(attributeName = "latitude")
    public double getLatitude() {
        return latitude;
    } //end method

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    } //end method

    @DynamoDBAttribute(attributeName = "subscriptions")
    public List<String> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<String> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public void addFriend(String name) {
        this.subscriptions.add(name);
    }

    @DynamoDBAttribute(attributeName = "subSent")
    public List<String> getSubSent() {
        return this.subSent;
    }

    public void setSubSent(List<String> subSent) {
        this.subSent = subSent;
    }

    public void sendRequest(String name) {
        this.subSent.add(name);
    }

    @DynamoDBAttribute(attributeName = "subRec")
    public List<String> getSubRec() {
        return this.subRec;
    }

    public void setSubRec(List<String> subRec) {
        this.subRec = subRec;
    }

    public void receiveRequest(String name) {
        this.subRec.add(name);
    }

} //end class
