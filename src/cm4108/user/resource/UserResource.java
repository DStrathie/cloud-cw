package cm4108.user.resource;

//general Java

import java.util.*;
//JAX-RS

import javax.ws.rs.*;
import javax.ws.rs.core.*;

//AWS SDK
import cm4108.user.model.User;
import com.amazonaws.services.dynamodbv2.datamodeling.*;

import cm4108.aws.util.*;
import cm4108.config.*;

@SuppressWarnings("serial")

@Path("/user")
public class UserResource {

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response addAUser(@FormParam("name") String name, @FormParam("longitude") double longitude, @FormParam("latitude") double latitude, @FormParam("friends") List<String> friends, @FormParam("sentRequests") List<String> sentRequests, @FormParam("receivedRequests") List<String> receivedRequests) {
        try {
            User user = new User(name, longitude, latitude, friends, sentRequests, receivedRequests);

            DynamoDBMapper mapper = DynamoDBUtil.getDBMapper(Config.REGION, Config.LOCAL_ENDPOINT);
            mapper.save(user);

            return Response.status(201).entity("user saved").build();
        } catch (Exception e) {
            return Response.status(400).entity("error in saving user").build();
        }
    }

    private boolean isRequested(User from, User to) {
        return to.getSubRec().contains(from.getName());
    }

    private boolean hasSent(User from, User to) {
        return to.getSubSent().contains(from.getName());
    }

    private boolean isFriend(User from, User to) {
        return to.getSubscriptions().contains(from.getName());
    }

    @Path("/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User getOneUser(@PathParam("name") String name) {
        DynamoDBMapper mapper = DynamoDBUtil.getDBMapper(Config.REGION, Config.LOCAL_ENDPOINT);
        User user = mapper.load(User.class, name);

        if (user == null) {
            throw new WebApplicationException(404);
        }

        return user;
    }

    @Path("/{name}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateLocation(@PathParam("name") String name, @FormParam("latitude") double latitude, @FormParam("longitude") double longitude) {
        try {
            DynamoDBMapper mapper = DynamoDBUtil.getDBMapper(Config.REGION, Config.LOCAL_ENDPOINT);
            User from = mapper.load(User.class, name);

            from.setLatitude(latitude);
            from.setLongitude(longitude);

            mapper.save(from);

            return Response.status(201).entity("Success").build();
        } catch (NumberFormatException e) {
            return Response.status(400).entity("Not a Number").build();
        } catch (NullPointerException e) {
            return Response.status(404).entity("User not found").build();
        } catch (Exception e) {
            return Response.status(500).entity(e.toString()).build();
        }
    }

    @Path("/{name}/{newsub}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response newSubReq(@PathParam("name") String name, @PathParam("newsub") String newsub) {
        try {
            DynamoDBMapper mapper = DynamoDBUtil.getDBMapper(Config.REGION, Config.LOCAL_ENDPOINT);
            User from = mapper.load(User.class, name);
            User to = mapper.load(User.class, newsub);

            Response r = null;

            if (name.equals(newsub)) {
                return Response.status(400).entity("Cant add yourself").build();
            }

            if (!from.getName().equals(name) || !to.getName().equals(newsub)) {
                return Response.status(404).entity("User" + newsub + " does not exist").build();
            }

            if (isFriend(from, to)) {
                return Response.status(403).entity("User " + newsub + " is already your friend").build();
            }

            if (isRequested(from, to)) {
                return Response.status(403).entity("User " + newsub + " already has your friend requests").build();
            }

            if (hasSent(from, to)) {
                to.getSubSent().remove(from.getName());
                from.getSubRec().remove(to.getName());

                from.addFriend(to.getName());
                to.addFriend(from.getName());
                r = Response.status(201).entity("User " + newsub + " is now your friend").build();
            } else {
                from.sendRequest(newsub);
                to.receiveRequest(name);
            }

            mapper.save(from);
            mapper.save(to);

            return r == null ? Response.status(200).entity("request to " + newsub + " sent").build() : r;
        } catch (Exception e) {
            return Response.status(500).entity("Error adding user").build();
        }
    }

    // get all cities from db
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<User> getAllUsers() {

        DynamoDBMapper mapper = DynamoDBUtil.getDBMapper(Config.REGION, Config.LOCAL_ENDPOINT);
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<User> result = mapper.scan(User.class, scanExpression);

        return result;

    }

    // cancel a subscriptionn request
    @Path("/{name}/{newsub}")
    @DELETE
    public Response denySub(@PathParam("name") String name, @PathParam("newsub") String newsub) {
        DynamoDBMapper mapper = DynamoDBUtil.getDBMapper(Config.REGION, Config.LOCAL_ENDPOINT);
        User from = mapper.load(User.class, name);
        User to = mapper.load(User.class, newsub);

        if (isFriend(from, to)) {
            return Response.status(403).entity("User " + newsub + " is already your friend").build();
        }

        if (!isRequested(to, from)) {
            return Response.status(404).entity("User " + newsub + " does not have a friend request from you ").build();
        }

        to.getSubSent().remove(from.getName());
        from.getSubRec().remove(to.getName());

        mapper.save(from);
        mapper.save(to);

        return Response.status(200).entity("friend request removed").build();
    }
}
