package pb.java.microservices.grpc.Rate.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import pb.java.microservices.grpc.Rate.entity.Stay;
import pb.java.microservices.grpc.rate.generatedProto.RateGrpc;
import pb.java.microservices.grpc.rate.generatedProto.RatePlan;
import pb.java.microservices.grpc.rate.generatedProto.Request;
import pb.java.microservices.grpc.rate.generatedProto.Result;
import pb.java.microservices.grpc.rate.generatedProto.RoomType;
import com.fasterxml.jackson.core.type.TypeReference;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@GrpcService
public class RateServiceImpl extends RateGrpc.RateImplBase {
    private Map<Stay, RatePlan> rateTable = new HashMap<>();

    ResourceLoader resourceLoader;

    @Autowired
    public RateServiceImpl(ResourceLoader resourceLoader) throws IOException {
        this.resourceLoader = resourceLoader;
        loadRateTableFromJsonFile("data/inventory.json");
    }

    @Override
    public void getRates(Request request, StreamObserver<Result> responseObserver) {
        Result.Builder resultBuilder = Result.newBuilder();
        for (String hotelId : request.getHotelIdsList()) {
            Stay stay = new Stay(hotelId, request.getInDate(), request.getOutDate());
            RatePlan ratePlan = rateTable.get(stay);
            if (ratePlan != null) {
                resultBuilder.addRatePlans(ratePlan);
            }
        }
        responseObserver.onNext(resultBuilder.build());
        responseObserver.onCompleted();
    }

    private void loadRateTableFromJsonFile(String filename) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + filename);
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(resource.getInputStream())) {
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    RatePlan.Builder ratePlanBuilder = RatePlan.newBuilder();
                    RoomType.Builder roomTypeBuilder = RoomType.newBuilder();
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        String fieldName = parser.getCurrentName();
                        parser.nextToken(); // move to value
                        switch (fieldName) {
                            case "hotelId":
                                ratePlanBuilder.setHotelId(parser.getText());
                                break;
                            case "code":
                                ratePlanBuilder.setCode(parser.getText());
                                break;
                            case "inDate":
                                ratePlanBuilder.setInDate(parser.getText());
                                break;
                            case "outDate":
                                ratePlanBuilder.setOutDate(parser.getText());
                                break;
                            case "roomType":
                                // Parse nested roomType object
                                while (parser.nextToken() != JsonToken.END_OBJECT) {
                                    String roomTypeField = parser.getCurrentName();
                                    parser.nextToken(); // move to value
                                    switch (roomTypeField) {
                                        case "bookableRate":
                                            roomTypeBuilder.setBookableRate(parser.getDoubleValue());
                                            break;
                                        // Handle other fields similarly
                                    }
                                }
                                ratePlanBuilder.setRoomType(roomTypeBuilder);
                                break;
                        }
                    }
                    RatePlan ratePlan = ratePlanBuilder.build();
                    Stay stay = new Stay(ratePlan.getHotelId(), ratePlan.getInDate(), ratePlan.getOutDate());
                    this.rateTable.put(stay, ratePlan);
                }
            }
        }
    }

}
