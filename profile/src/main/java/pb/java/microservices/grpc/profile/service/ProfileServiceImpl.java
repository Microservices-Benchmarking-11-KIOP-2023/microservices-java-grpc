package pb.java.microservices.grpc.profile.service;

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
import pb.java.microservices.grpc.profile.generatedProto.Hotel;
import pb.java.microservices.grpc.profile.generatedProto.ProfileGrpc;
import pb.java.microservices.grpc.profile.generatedProto.Request;
import pb.java.microservices.grpc.profile.generatedProto.Result;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@GrpcService
public class ProfileServiceImpl extends ProfileGrpc.ProfileImplBase {
    private Map<String, Hotel> profiles = new HashMap<>();

    ResourceLoader resourceLoader;

    @Autowired
    public ProfileServiceImpl(ResourceLoader resourceLoader) throws IOException {
        this.resourceLoader = resourceLoader;
        loadProfilesFromJsonFile("data/hotels.json");
    }

    @Override
    public void getProfiles(Request request, StreamObserver<Result> responseObserver) {
        Result.Builder resultBuilder = Result.newBuilder();
        for (String id : request.getHotelIdsList()) {
            Hotel hotel = profiles.get(id);
            if (hotel != null) {
                resultBuilder.addHotels(hotel);
            }
        }
        responseObserver.onNext(resultBuilder.build());
        responseObserver.onCompleted();
    }

    private void loadProfilesFromJsonFile(String filename) throws IOException {
        String jsonData = readJsonFile(filename);
        List<Hotel> hotelList = parseJsonToHotelList(jsonData);
        this.profiles = hotelList.stream().collect(Collectors.toMap(Hotel::getId, Function.identity()));
    }

    private List<Hotel> parseJsonToHotelList(String jsonData) throws IOException {
        List<Hotel> hotelList = new ArrayList<>();
        JsonArray jsonArray = new JsonParser().parse(jsonData).getAsJsonArray();
        Hotel.Builder builder = Hotel.newBuilder();

        for (JsonElement jsonElement : jsonArray) {
            JsonFormat.parser().ignoringUnknownFields().merge(jsonElement.toString(), builder);
            hotelList.add(builder.build());
            builder.clear();
        }

        return hotelList;
    }

    private String readJsonFile(String filename) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + filename);
        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        return FileCopyUtils.copyToString(reader);
    }
}


