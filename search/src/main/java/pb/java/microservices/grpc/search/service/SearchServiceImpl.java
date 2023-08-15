package pb.java.microservices.grpc.search.service;

import io.grpc.ManagedChannel;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import pb.java.microservices.grpc.geo.generatedProto.GeoGrpc;
import pb.java.microservices.grpc.rate.generatedProto.RateGrpc;
import pb.java.microservices.grpc.rate.generatedProto.RatePlan;
import pb.java.microservices.grpc.search.generatedProto.SearchGrpc;

@GrpcService
public class SearchServiceImpl extends SearchGrpc.SearchImplBase {

    @Autowired
    @Qualifier("GeoServiceChannel")
    private ManagedChannel geoServiceChannel;

    @Autowired
    @Qualifier("RateServiceChannel")
    private ManagedChannel rateServiceChannel;
    @Override
    public void nearby(pb.java.microservices.grpc.search.generatedProto.NearbyRequest request,
                       io.grpc.stub.StreamObserver<pb.java.microservices.grpc.search.generatedProto.SearchResult> responseObserver) {

        GeoGrpc.GeoBlockingStub geoStub = GeoGrpc.newBlockingStub(geoServiceChannel);
        pb.java.microservices.grpc.geo.generatedProto.Request geoRequest = pb.java.microservices.grpc.geo.generatedProto.Request.newBuilder()
                .setLat(request.getLat())
                .setLon(request.getLon())
                .build();
        pb.java.microservices.grpc.geo.generatedProto.Result geoResult = geoStub.nearby(geoRequest);


        RateGrpc.RateBlockingStub rateStub = RateGrpc.newBlockingStub(rateServiceChannel);
        pb.java.microservices.grpc.rate.generatedProto.Request rateRequest = pb.java.microservices.grpc.rate.generatedProto.Request.newBuilder()
                .addAllHotelIds(geoResult.getHotelIdsList())
                .setInDate(request.getInDate())
                .setOutDate(request.getOutDate())
                .build();
        pb.java.microservices.grpc.rate.generatedProto.Result rateResult = rateStub.getRates(rateRequest);

        pb.java.microservices.grpc.search.generatedProto.SearchResult searchResult = pb.java.microservices.grpc.search.generatedProto.SearchResult.newBuilder()
                .addAllHotelIds(
                        rateResult.getRatePlansList().stream()
                                .map(RatePlan::getHotelId)
                                .toList()
                )
                .build();

        responseObserver.onNext(searchResult);
        responseObserver.onCompleted();
    }
}
