package pb.java.microservices.grpc.search.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RateServiceChannel {

    @Value("${rateServiceUrl}")
    private String rateServiceUrl;

    @Bean
    public ManagedChannel RateServiceChannel() {
        return ManagedChannelBuilder.forTarget(rateServiceUrl).usePlaintext().build();
    }
}