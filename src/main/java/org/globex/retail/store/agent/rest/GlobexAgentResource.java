package org.globex.retail.store.agent.rest;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.globex.retail.store.cart.service.CartService;
import org.globex.retail.store.catalog.service.CatalogService;
import org.globex.retail.store.customer.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.globex.retail.store.order.service.OrderService;

@Path("/agents")
@Authenticated

public class GlobexAgentResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobexAgentResource.class);
    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    CartService cartService;

    @Inject
    OrderService orderService;

    @Inject
    CustomerService customerService;

    @Inject
    CatalogService catalogService;


    @GET
    @Path("/order/{customerId}/orders")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getOrdersByCustomerId(@PathParam("customerId") String customerId) {
        LOGGER.info("[GlobexAgentResource] Received request to fetch orders for customerId: {}", securityIdentity.getPrincipal().getName());
        return Uni.createFrom().item(() -> null).emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(n -> orderService.getOrderByCustomerId(customerId))
                .onItem().transform(orders -> Response.ok(orders).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Exception while fetching order by customerId and orderId", throwable);
                    return Response.serverError().build();
                });
    }

    @GET
    @Path("/customer/email/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getCustomerByUserEmail(@PathParam("email") String email) {
        LOGGER.info("[GlobexAgentResource] Received request to fetch customer by email: {}", email);
        return Uni.createFrom().item(() -> email).emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(u -> customerService.getCustomerByEmail(u))
                .onItem().transform(customerDto -> {
                    if (customerDto == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    } else {
                        return Response.ok(customerDto).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Exception while fetching customer", throwable);
                    return Response.serverError().build();
                });
    }

    @GET
    @Path("/customer/id/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getCustomerByUserId(@PathParam("userId") String userId) {
        LOGGER.info("[GlobexAgentResource] Received request to fetch customer by userId: {}", userId);
        return Uni.createFrom().item(() -> userId).emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(u -> customerService.getCustomerByCustomerId(u))
                .onItem().transform(customerDto -> {
                    if (customerDto == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    } else {
                        return Response.ok(customerDto).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Exception while fetching customer", throwable);
                    return Response.serverError().build();
                });
    }

    @GET
    @Path("/catalog/product/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getProductById(@PathParam("id") String id, @QueryParam("inventory") Boolean inventory) {
        
        LOGGER.info("[GlobexAgentResource] securityIdentity.getPrincipal {}", securityIdentity.getPrincipal().toString());

        LOGGER.info("[GlobexAgentResource] Received request to fetch product by id: {}, with inventory: {}", id, inventory);
        final boolean inv = inventory == null || inventory;
        return Uni.createFrom().item(() -> id).emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(productId -> catalogService.read(productId, inv))
                .onItem().transform(productDto -> {
                    if (productDto == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    } else {
                        return Response.ok(productDto).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Exception while fetching product by id", throwable);
                    return Response.serverError().build();
                });
    }    


}
