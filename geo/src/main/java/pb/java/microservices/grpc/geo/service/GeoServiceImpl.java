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
    private static final int MAX_SEARCH_RESULTS = 1000000000;

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
        System.out.println("DOSTAŁEM COŚ");
        GeoPoint center = new GeoPoint();
        center.setLat(request.getLat());
        center.setLon(request.getLon());

        List<String> hotelIds = getNearbyPoints(center)
                .stream()
                .sorted(Comparator.comparingDouble(p -> distance(p, center)))
                .limit(MAX_SEARCH_RESULTS)
                .map(GeoPoint::getHotelId)
                .collect(Collectors.toList());

        Result result = Result.newBuilder()
                .addAllHotelIds(hotelIds)
                .build();

        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    private List<GeoPoint> getNearbyPoints(GeoPoint center) {
        return geoIndex.values().stream()
                .filter(point -> distance(point, center) <= MAX_SEARCH_RADIUS)
                .collect(Collectors.toList());
    }

    private String readJsonFile(String filename) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + filename);
        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        return FileCopyUtils.copyToString(reader);
    }

    // The distance calculation function using Pythagorean theorem
    private double distance(GeoPoint p1, GeoPoint p2) {
        double dx = p1.getLon() - p2.getLon();
        double dy = p1.getLat() - p2.getLat();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
