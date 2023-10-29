package pb.java.microservices.grpc.Rate.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pb.java.microservices.grpc.Rate.service.RateServiceImpl;

import java.io.IOException;

@Configuration
public class GrpcServerConfiguration {
    @Bean
    public Server grpcServer(RateServiceImpl rateService) throws IOException {
        Server server = ServerBuilder.forPort(8080)
                .addService(rateService)
                .build();
        server.start();
        return server;
    }
}
