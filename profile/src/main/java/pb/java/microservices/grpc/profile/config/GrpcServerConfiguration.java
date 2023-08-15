package pb.java.microservices.grpc.profile.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pb.java.microservices.grpc.profile.service.ProfileServiceImpl;

import java.io.IOException;

@Configuration
public class GrpcServerConfiguration {
    @Bean
    public Server grpcServer(ProfileServiceImpl profileService) throws IOException {
        Server server = ServerBuilder.forPort(8081)
                .addService(profileService)
                .build();
        server.start();
        return server;
    }
}
