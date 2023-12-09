package pb.java.microservices.grpc.geo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.protobuf.util.JsonFormat;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import pb.java.microservices.grpc.geo.entity.GeoPoint;
import pb.java.microservices.grpc.geo.generatedProto.GeoGrpc;
import pb.java.microservices.grpc.geo.generatedProto.Request;
import pb.java.microservices.grpc.geo.generatedProto.Result;

import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@GrpcService
public class GeoServiceImpl extends GeoGrpc.GeoImplBase {
    private static final double MAX_SEARCH_RADIUS = 10;
    private static final int MAX_SEARCH_RESULTS = Integer.MAX_VALUE;
    private static final double EARTH_RADIUS = 6371;
    private static final double RADIANS_CONST = Math.PI / 180;

    private Map<String, GeoPoint> geoIndex = new HashMap<>();

    private ResourceLoader resourceLoader;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public GeoServiceImpl(ResourceLoader resourceLoader) throws IOException {
        this.resourceLoader = resourceLoader;
        String jsonData = readJsonFile("data/geo.json");

        JsonArray jsonArray = new JsonParser().parse(jsonData).getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            GeoPoint geoPoint = objectMapper.readValue(jsonElement.toString(), GeoPoint.class);
            geoIndex.put(geoPoint.getHotelId(), geoPoint);
        }
    }

    @Override
    public void nearby(Request request, StreamObserver<Result> responseObserver) {
        GeoPoint center = new GeoPoint();
        center.setLat(request.getLat());
        center.setLon(request.getLon());

        List<String> hotelIds = getNearbyHotels(center);

        Result result = Result.newBuilder()
                .addAllHotelIds(hotelIds)
                .build();

        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    public List<String> getNearbyHotels(GeoPoint center) {

        return geoIndex.values().stream()
                .map(p -> new AbstractMap.SimpleEntry<>(p, haversineDistance(p, center)))
                .filter(entry -> entry.getValue() <= MAX_SEARCH_RADIUS)
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .map(entry -> entry.getKey().getHotelId())
                .collect(Collectors.toList());
    }

    private String readJsonFile(String filename) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + filename);
        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        return FileCopyUtils.copyToString(reader);
    }

    // Haversine distance calculation
    private double haversineDistance(GeoPoint p1, GeoPoint p2) {
        double latDistance = Math.toRadians(p2.getLat() - p1.getLat());
        double lonDistance = Math.toRadians(p2.getLon() - p1.getLon());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(p1.getLat())) * Math.cos(Math.toRadians(p2.getLat()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double centralAngle = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * centralAngle;
    }
}
