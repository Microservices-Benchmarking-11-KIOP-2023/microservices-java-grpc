package pb.java.microservices.grpc.search.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pb.java.microservices.grpc.search.service.SearchServiceImpl;

import java.io.IOException;

@Configuration
public class GrpcServerConfiguration {
    @Bean
    public Server grpcServer(SearchServiceImpl searchService) throws IOException {
        Server server = ServerBuilder.forPort(8082)
                .addService(searchService)
                .build();
        server.start();
        return server;
    }
}
