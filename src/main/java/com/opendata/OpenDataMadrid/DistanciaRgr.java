package com.opendata.OpenDataMadrid;

import java.util.ArrayList;

import org.bson.Document;
import org.joda.time.DateTime;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

public class DistanciaRgr {

	private static GeoApiContext context;
	
		
//	public String[] getDistance(String[] origenes, String[] destinos) {
//		
//		
//	}
	
	public double distanciaCoord(double lat1, double lng1, double lat2, double lng2) {  

        double radioTierra = 6371;//en kilómetros  
        double dLat = Math.toRadians(lat2 - lat1);  
        double dLng = Math.toRadians(lng2 - lng1);  
        double sindLat = Math.sin(dLat / 2);  
        double sindLng = Math.sin(dLng / 2);  
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)  
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));  
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));  
        double distancia = radioTierra * va2;  
   
        return distancia*1000;   
    } 

	public DistanceMatrix estimateRouteTime(DateTime time, TravelMode modoViaje,LatLng departure, LatLng... arrivals) {
    
		try {
        DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(getContext());
        
        DistanceMatrix trix = req.origins(departure)
                .destinations(arrivals)
                .mode(modoViaje)
                .language("es-ES")
                .await();
        return trix;

    } catch (ApiException e) {
        System.out.println(e.getMessage());
    } catch (Exception e) {
        System.out.println(e.getMessage());
    }
    return null;
}
	
	public Double sumaDistancia(Document doc,Double lat1,Double lng1,Double lat2,Double lng2) {
		
		Double suma = 0.0; 
		
//		BasicDBObject loc = (BasicDBObject) doc.get("location");
//
//      BasicDBList coordinates = (BasicDBList) loc.get("coordinates"); // BasicDBList contains coordinates
        ArrayList<Double> coordenadas;
        
        Document location = (Document)doc.get("location");
		coordenadas = (ArrayList<Double>)location.get("coordinates");
   		
        suma = distanciaCoord(coordenadas.get(0), coordenadas.get(1), lat1, lng1) + distanciaCoord(coordenadas.get(0), coordenadas.get(1), lat2, lng2);
		
		return suma;
	}

	public void  setContext(String key) {
			
		context = new GeoApiContext().setApiKey(key);
	}

	public static GeoApiContext getContext() {
		return context;
	}
}