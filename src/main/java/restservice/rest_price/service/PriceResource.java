package restservice.rest_price.service;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;
import restservice.model.Price;

/**
 * REST Web Service
 *
 * @author AMore
 */
@Stateless
@Path("price")
public class PriceResource {

    @Context
    private UriInfo context;

    @PersistenceContext(unitName = "WebServices_PU")
    private EntityManager em;

    public PriceResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getprice/{username}/{password}/{ticketid}")
    public Response getAuth(
            @PathParam("username") String username, 
            @PathParam("password") String password, 
            @PathParam("ticketid") String ticketid) {
        JsonObject value;
        Price price;

        try {
            price = em.find(Price.class, Integer.valueOf(ticketid));
        } catch (NoResultException e) {
            price = null;
        }
        
        if (authorizeUser(username, password) && price != null) {
            return Response.status(200).entity(price).build();
        } else {
            value = Json.createObjectBuilder()
                    .add("found", "false")
                    .add("reason", "User not authorized or price dont exist")
                    .build();
            return Response.status(200).entity(value.toString()).build();
        }
        
    }

    
    @POST
    @Path("/createprice")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(Price price) {
        JsonObject value;
        
        em.persist(price);

         value = Json.createObjectBuilder()
                .add("addedprice", "true")
                .build();
        return Response.status(200).entity(value).build();
    }

    
    @PUT
    @Path("/updateprice")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAuth(Price price) {
        JsonObject value;

        // Get the existing user
        Price updateThisPrice = em.find(Price.class, price.getTicketID());

        if (updateThisPrice != null) {

            updateThisPrice.setPrice(price.getPrice());

            em.merge(updateThisPrice);

            value = Json.createObjectBuilder()
                    .add("updated", "true")
                    .add("ticketid", price.getTicketID())
                    .add("newprice", price.getPrice())
                    .build();
        } else {
            value = Json.createObjectBuilder()
                    .add("updated", "false")
                    .add("reason", "Ticketid dont exist")
                    .build();
        }
        return Response.status(200).entity(value).build();
    }

    @DELETE
    @Path("/deleteprice/{ticketid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAuth(@PathParam("ticketid") String ticketid) {
        JsonObject value;

        // Get the existing user
        Price removeThisPrice = em.find(Price.class, ticketid);

        if (removeThisPrice != null) {

            em.remove(removeThisPrice);

            value = Json.createObjectBuilder()
                    .add("deleted", "true")
                    .add("ticketid", ticketid)
                    .build();
        } else {
            value = Json.createObjectBuilder()
                    .add("deleted", "false")
                    .add("reason", "Ticket id dont exist")
                    .build();
        }

        return Response.status(200).entity(value).build();
    }


    
    
    /**
     *
     * @param username
     * @param password
     * @return
     */
    public boolean authorizeUser(String username, String password) {
        try {
            Client client = Client.create();

            WebResource webResource = client.resource("http://localhost:8080/REST_Authorization/webresources/auth/login/" + username + "/" + password);

            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            JSONObject jsonObj = new JSONObject(response.getEntity(String.class));

            return Boolean.valueOf((String) jsonObj.get("authorized"));

        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }

    }
    
    
    
}
