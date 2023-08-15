package pb.java.microservices.grpc.Rate.service;

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
import pb.java.microservices.grpc.Rate.entity.Stay;
import pb.java.microservices.grpc.rate.generatedProto.RateGrpc;
import pb.java.microservices.grpc.rate.generatedProto.RatePlan;
import pb.java.microservices.grpc.rate.generatedProto.Request;
import pb.java.microservices.grpc.rate.generatedProto.Result;


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
        String jsonData = readJsonFile(filename);
        List<RatePlan> ratePlanList = parseJsonToRatePlanList(jsonData);
        this.rateTable = ratePlanList.stream().collect(Collectors.toMap(rp -> new Stay(rp.getHotelId(), rp.getInDate(), rp.getOutDate()), Function.identity()));
    }

    private List<RatePlan> parseJsonToRatePlanList(String jsonData) throws IOException {
        List<RatePlan> ratePlanList = new ArrayList<>();
        JsonArray jsonArray = new JsonParser().parse(jsonData).getAsJsonArray();
        RatePlan.Builder builder = RatePlan.newBuilder();

        for (JsonElement jsonElement : jsonArray) {
            JsonFormat.parser().ignoringUnknownFields().merge(jsonElement.toString(), builder);
            ratePlanList.add(builder.build());
            builder.clear();
        }

        return ratePlanList;
    }

    private String readJsonFile(String filename) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + filename);
        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        return FileCopyUtils.copyToString(reader);
    }
}
