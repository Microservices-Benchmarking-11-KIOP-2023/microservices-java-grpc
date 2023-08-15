package pb.java.microservices.grpc.gateway.service;

import io.grpc.ManagedChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pb.java.microservices.grpc.profile.generatedProto.Hotel;
import pb.java.microservices.grpc.profile.generatedProto.ProfileGrpc;
import pb.java.microservices.grpc.profile.generatedProto.Request;
import pb.java.microservices.grpc.profile.generatedProto.Result;
import pb.java.microservices.grpc.search.generatedProto.SearchGrpc;

import java.util.*;

@Service
public class GatewayService {

    @Autowired
    @Qualifier("SearchServiceChannel")
    private ManagedChannel searchServiceChannel;

    @Autowired
    @Qualifier("ProfileServiceChannel")
    private ManagedChannel profileServiceChannel;

    public Map<String, Object> searchHotels(String inDate, String outDate, float lat, float lon) {

        SearchGrpc.SearchBlockingStub searchStub = SearchGrpc.newBlockingStub(searchServiceChannel);
        pb.java.microservices.grpc.search.generatedProto.NearbyRequest searchRequest =  pb.java.microservices.grpc.search.generatedProto.NearbyRequest.newBuilder()
                .setInDate(inDate)
                .setOutDate(outDate)
                .setLat(lat)
                .setLon(lon)
                .build();

        pb.java.microservices.grpc.search.generatedProto.SearchResult searchResponse = searchStub.nearby(searchRequest);

        ProfileGrpc.ProfileBlockingStub profileStub = ProfileGrpc.newBlockingStub(profileServiceChannel);
        Request profileRequest = Request.newBuilder()
                .addAllHotelIds(searchResponse.getHotelIdsList())
                .build();

        Result profileResponse = profileStub.getProfiles(profileRequest);

        return geoJSONResponse(profileResponse.getHotelsList());
    }

    private Map<String, Object> geoJSONResponse(List<Hotel> hotels) {
        List<Map<String, Object>> features = new ArrayList<>();
        for (Hotel hotel : hotels) {
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");
            feature.put("id", hotel.getId());

            Map<String, String> properties = new HashMap<>();
            properties.put("name", hotel.getName());
            properties.put("phone_number", hotel.getPhoneNumber());

            Map<String, Object> geometry = new HashMap<>();
            geometry.put("type", "Point");
            List<Float> coordinates = Arrays.asList(hotel.getAddress().getLon(), hotel.getAddress().getLat());
            geometry.put("coordinates", coordinates);

            feature.put("properties", properties);
            feature.put("geometry", geometry);

            features.add(feature);
        }

        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");
        geoJson.put("features", features);

        return geoJson;
    }

}
