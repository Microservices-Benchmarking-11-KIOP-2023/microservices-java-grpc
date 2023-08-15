package pb.java.microservices.grpc.search.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GeoServiceChannel {
    @Value("${geoServiceUrl}")
    private String geoServiceUrl;

    @Bean
    public ManagedChannel GeoServiceChannel() {
        return ManagedChannelBuilder.forTarget(geoServiceUrl).usePlaintext().build();
    }
}
