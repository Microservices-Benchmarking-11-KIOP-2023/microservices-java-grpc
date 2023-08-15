package pb.java.microservices.grpc.geo.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pb.java.microservices.grpc.geo.service.GeoServiceImpl;

import java.io.IOException;

@Configuration
public class GrpcServerConfiguration {
    @Bean
    public Server grpcServer(GeoServiceImpl geoService) throws IOException {
        Server server = ServerBuilder.forPort(8083)
                .addService(geoService)
                .build();
        server.start();
        return server;
    }
}
