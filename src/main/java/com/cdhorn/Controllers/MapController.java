package com.cdhorn.Controllers;

import com.cdhorn.Classes.DirectionResponse;
import com.cdhorn.Classes.GeocodingResponse;
import com.cdhorn.GoogleMaps.ApiKey;
import com.cdhorn.GoogleMaps.ApiStaticMap;
import com.cdhorn.Interfaces.DirectionsInterface;
import com.cdhorn.Interfaces.GeocodingInterface;
import com.cdhorn.Interfaces.MapRepository;
import com.cdhorn.Models.Map;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class MapController {

    @Autowired
    MapRepository mapRepo;

    @RequestMapping("/map/{runId}/routeStart")
    public String createRoute(Model model) throws IOException {
        ApiKey apiKey = new ApiKey();
        GeocodingInterface geocodingInterface = Feign.builder()
                .decoder(new GsonDecoder())
                .target(GeocodingInterface.class, "https://maps.googleapis.com");
        GeocodingResponse response = geocodingInterface.geocodingResponse("starbucks+east+north+street+greenville+sc",
                apiKey.getGEOCODING_API());
        System.out.println(response.getResults().get(0).getGeometry().getLocation().getLat());
        System.out.println(response.getResults().get(0).getGeometry().getLocation().getLng());
//        System.out.println(response.results.get(0).geometry.location.lng);
        return "routeStart";
    }

    @RequestMapping(value = "/map/{runId}/routeStart", method = RequestMethod.POST)
    public String createRoute(@PathVariable("runId") String runId,
            @RequestParam("address") String address,
                              Model model) {
        String addressNoSpaces = address.replace(" ", "+");
        ApiKey apiKey = new ApiKey();
        GeocodingInterface geocodingInterface = Feign.builder()
                .decoder(new GsonDecoder())
                .target(GeocodingInterface.class, "https://maps.googleapis.com");
        GeocodingResponse response = geocodingInterface.geocodingResponse(addressNoSpaces + "+greenville+sc",
                apiKey.getGEOCODING_API());
        double lat = response.getResults().get(0).getGeometry().getLocation().getLat();
        double lng = response.getResults().get(0).getGeometry().getLocation().getLng();
        String startPosition = lat + "," + lng;
        Map newMap = new Map();
        newMap.setStartPosition(startPosition);
        mapRepo.save(newMap);
        System.out.println(response.getResults().get(0).getGeometry().getLocation().getLat());
        System.out.println(response.getResults().get(0).getGeometry().getLocation().getLng());

        model.addAttribute("runId", runId);
        return "redirect:/map/"+runId+"/routeLeg";
    }

    @RequestMapping("/map/{runId}/routeLeg")
    public String addRouteLeg(@PathVariable("runId") String runId,
                              @RequestParam("leg") String leg) {

        return "routeLeg";
    }

    @RequestMapping("/map/{runId}/routeEnd")
    public String testMap(Model model) throws IOException {

        ApiKey apiKey = new ApiKey();
        DirectionsInterface directionsInterface = Feign.builder()
                .decoder(new GsonDecoder())
                .target(DirectionsInterface.class, "https://maps.googleapis.com");
        DirectionResponse response = directionsInterface.directionResponse("34.9195746,-82.4217151", "34.8701836,-82.44755599999999", apiKey.getDIRECTIONS_API());
        String polyline = response.getRoutes().get(0).getOverview_polyline().getPoints();
        System.out.println(polyline);
        System.out.println("");
        String reformattedPolyline = polyline.replace("|", "%7C");
        System.out.println(reformattedPolyline);
        System.out.println("");

        ApiStaticMap apiStaticMap = new ApiStaticMap();
        String url = apiStaticMap.getStaticMapUrl();
        String startMarker = apiStaticMap.getStartMarkerParameters();
        String finishMarker = apiStaticMap.getFinishMarkerParameters();
        String pathParams = apiStaticMap.getPathParameters();
        String staticMapApiKeyParams = apiStaticMap.getStaticMapApiKey();

        url += startMarker + "34.9195746,-82.4217151" + finishMarker + "34.8701836,-82.4217151" + staticMapApiKeyParams + apiKey.getSTATIC_MAP_API() + pathParams + reformattedPolyline;
//
        System.out.println(url);
        model.addAttribute("url", url);
        return "map";

    }

}
