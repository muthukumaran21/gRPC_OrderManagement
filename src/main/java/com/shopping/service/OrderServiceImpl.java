package com.shopping.service;

import com.google.protobuf.util.Timestamps;
import com.shopping.db.Order;
import com.shopping.db.OrderDao;
import com.shopping.stubs.order.OrderRequest;
import com.shopping.stubs.order.OrderResponse;
import com.shopping.stubs.order.OrderServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OrderServiceImpl  extends OrderServiceGrpc.OrderServiceImplBase {

    OrderDao orderDao = new OrderDao();
    private Logger logger = Logger.getLogger(OrderServiceImpl.class.getName());
    @Override
    public void getOrdersForUser(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {

        List<Order> orders = orderDao.getOrders(request.getUserId());

        logger.info("Got orders from the database");

        /* convert the list of orders to list of order response */
        // Start a stream operation on the list `orders`. This list is presumably a list of `Order` objects from your database.
        // The `map()` function is used to transform each `Order` object from your database into a `com.shopping.stubs.order.Order` object.
        // `com.shopping.stubs.order.Order` is the gRPC/Protobuf message representation of an Order.
        List<com.shopping.stubs.order.Order> ordersForUser =  orders.stream().map(order -> com.shopping.stubs.order.Order.newBuilder()
                        .setUserId(order.getUserId())
                        .setOrderId(order.getOrderId())
                        .setNoOfItems(order.getNoOfItems())
                        .setTotalAmount(order.getTotalAmount())
                        .setOrderDate(Timestamps.fromMillis(order.getOrderDate().getTime()))
                        .build())   // Build the gRPC/Protobuf Order object
                // The `collect(Collectors.toList())` function is used to convert the stream back into a list.
                // Now, `ordersForUser` is a list of gRPC `Order` objects.
                .collect(Collectors.toList());

// An `OrderResponse` is then built using the list of gRPC `Order` objects.
        OrderResponse orderResponse = OrderResponse.newBuilder().addAllOrder(ordersForUser).build();

        responseObserver.onNext(orderResponse);

        responseObserver.onCompleted();

    }
}
