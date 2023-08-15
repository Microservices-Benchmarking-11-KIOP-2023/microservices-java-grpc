package pb.java.microservices.grpc.gateway.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ProfileServiceChannel {

    @Value("${profileServiceUrl}")
    private String profileServiceUrl;

    @Bean
    public ManagedChannel ProfileServiceChannel() {
        return ManagedChannelBuilder.forTarget(profileServiceUrl).usePlaintext().build();
    }
}
