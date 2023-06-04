package com.shopping.server;

import com.shopping.service.OrderServiceImpl;
import com.shopping.service.UserServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class OrderServer {


    private static final Logger logger = Logger.getLogger(OrderServer.class.getName());

    private Server server;

    public void startServer() throws IOException {
        int port = 50052;

        server = ServerBuilder.forPort(port)
                .addService(new OrderServiceImpl())
                .build()
                .start();
        logger.info("server started, listening on 50052 ");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Received shutdown request");
            try {
                OrderServer.this.stopServer();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            logger.info("Successfully stopped the server");
        }));
    }

    /* Stop serving requests and shutdown resources */
    public void stopServer() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /* Block main thread until server is terminated */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        OrderServer orderServer = new OrderServer();
        orderServer.startServer();
        /* when main is cut the server will keep running untill all is done */
        orderServer.blockUntilShutdown();
    }


}
