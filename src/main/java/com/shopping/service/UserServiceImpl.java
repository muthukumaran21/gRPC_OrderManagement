package com.shopping.service;

import com.shopping.client.OrderClient;
import com.shopping.db.User;
import com.shopping.db.UserDao;
import com.shopping.stubs.order.Order;
import com.shopping.stubs.user.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase{

    private Logger logger = Logger.getLogger(UserServiceImpl.class.getName());
    UserDao userDao = new UserDao();

    @Override
    /* from the .proto file */
    /* stream observer is used to send the response back to the client */
    public void getUserDetails(UserRequest request, StreamObserver<UserResponse> responseObserver) {



        /* get the username from the request */
        User user = userDao.getDetails(request.getUsername());

        /* create a response object */
        UserResponse.Builder responseBuilder = UserResponse.newBuilder().setAge(user.getAge())
                .setGender(Gender.valueOf(user.getGender().toUpperCase()))
                .setId(user.getId())
                .setName(user.getName())
                .setUsername(user.getUsername());

        List<Order> orders = getOrders(responseBuilder);

        responseBuilder.setNoOfOrders(orders.size());

        /* build the response */

        UserResponse response = responseBuilder.build();


        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    private List<Order> getOrders(UserResponse.Builder userResponseBuilder) {
        //get orders by invoking the Order Client
        logger.info( "Creating a channel and calling Order client ");
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:50052")
                .usePlaintext().build();
        OrderClient orderClient = new OrderClient(channel);
        List<Order> orders = orderClient.getOrders(userResponseBuilder.getId());

        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            logger.log(Level.SEVERE, "Channel did not shutdown", exception);
        }
        return orders;
    }
}
