package pb.java.microservices.grpc.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pb.java.microservices.grpc.gateway.service.GatewayService;

import java.util.Map;

@RestController
public class GatewayController {

    @Autowired
    private GatewayService gatewayService;

    @RequestMapping("/hotels")
    public Map<String, Object> searchHandler(
            @RequestParam(value = "inDate") String inDate,
            @RequestParam(value = "outDate") String outDate,
            @RequestParam(value = "lat") float lat,
            @RequestParam(value = "lon") float lon) {

        return gatewayService.searchHotels(inDate, outDate, lat, lon);
    }
}
