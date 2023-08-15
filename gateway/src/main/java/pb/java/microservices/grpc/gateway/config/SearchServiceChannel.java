package pb.java.microservices.grpc.gateway.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SearchServiceChannel {
    @Value("${searchServiceUrl}")
    private String searchServiceUrl;

    @Bean
    public ManagedChannel SearchServiceChannel() {
        return ManagedChannelBuilder.forTarget(searchServiceUrl).usePlaintext().build();
    }
}
