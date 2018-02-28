package com.opendata.OpenDataMadrid;
import java.awt.geom.Point2D;
import java.util.Scanner;

import maps.java.Geocoding;
import maps.java.MapsJava;
import maps.java.Places;
import maps.java.Places.Rankby;



public class SistemaGastronomico {

	
   public static void error(String funcionError){
        System.err.println("Se ha producido el siguiente error: " + funcionError);
    }
   
   private static final String API_KEY = "AIzaSyAl228OPSurDrQf8kquec2003N5SJY6dKM";
   

   public static void main(String args[]) {

			System.out.println("Empezamos!");

		//1-Recuperacion de los parametros de entrada
			
			System.out.println("Introduzca direccion de inicio y pulse INTRO"); 
	        Scanner desde = new Scanner(System.in);
	        String Desde = desde.nextLine();
	        System.out.println("Introduzca direccion de fin y pulse INTRO");
	        Scanner hasta = new Scanner(System.in);
	        String Hasta = hasta.nextLine();
	        System.out.println("Introduzca tipo de algoritmo y pulse INTRO");
	        Scanner Algoritmo = new Scanner(System.in);
	        String algoritmo = Algoritmo.next(); //distancia/puntuacion/relevancia
		
			Point2D.Double cIni = new Point2D.Double();
			Point2D.Double cFin = new Point2D.Double();
			Integer radio;
			Double radioD;
			Integer numSitios = 20;
			Boolean abierto = false;
			String tipoSitio = "restaurant";
			Integer distancia =200;
			Double puntuacion = 5.0;
			if (algoritmo.equals("relevancia")) {
				 System.out.println("Introduzca la distancia a la ruta maxima a buscar");
			        Scanner Distancia = new Scanner(System.in);
			        distancia = Integer.parseInt(Distancia.nextLine());
			        System.out.println("Introduzca la puntuacion minima por la que buscar");
			        Scanner Puntuacion  = new Scanner(System.in);
			        puntuacion = Double.valueOf(Puntuacion.nextLine());
			        Puntuacion.close();
			        Distancia.close();
			}
	        desde.close();
	        hasta.close();
	        Algoritmo.close();
			//String modoViaje = "andando";
			Rankby rankby; 
			
	        System.out.println("Mostrar: "+ numSitios +" "+ tipoSitio+ "  Desde: "+ Desde + " Hasta: " + Hasta + " con el algoritmo: " + algoritmo);

		//2-Transformar direcciones en coordenadas 
			
	        Geocoding ObjGeocod=new Geocoding();
	        try { 
		        	
	        	cIni =ObjGeocod.getCoordinates(Desde);
	        	cFin =ObjGeocod.getCoordinates(Hasta);
	        	 
	        } catch (Exception e) {
	        	error("Codificacion de coordenadas");
		    }
	    //3- Obtener la distancia entre las dos coordenadas para calcular su radio haciendo uso del Distance Matrix API de Google Maps
	        
	        DistanciaRgr disRgr = new DistanciaRgr();
	        radioD = Double.valueOf(disRgr.distanciaCoord(cIni.getY(), cIni.getX(), cFin.getY(), cFin.getX()))/2;
	        radio = radioD.intValue();
		//4- Buscar sitios cercanos a las coordenadas de Inicio/Fin en Google Maps y unificar los resultados
	        
	        MapsJava.setKey(API_KEY);
	        UtilRgr utilRgr = new UtilRgr();
	        MongoRgr mongRgr = new MongoRgr();
            rankby =  utilRgr.getRankBy(algoritmo);
            
	        if("OK".equals(MapsJava.APIkeyCheck(API_KEY))){
	            Places ObjPlace=new Places();
	            
	            try {
	            	
	                String[][] resultadoIni=ObjPlace.getPlaces(cIni.getX(), cIni.getY(), radio, "", "", rankby,tipoSitio, abierto);
	                String[][] resultadoFin=ObjPlace.getPlaces(cFin.getX(), cFin.getY(), radio, "", "", rankby,tipoSitio, abierto);
	                
	                for(int i=0;i<resultadoIni.length;i++){
	                	resultadoIni[i][7] = tipoSitio ;
	                }
	                for(int i=0;i<resultadoFin.length;i++){
	                	resultadoFin[i][7] = tipoSitio ;
	                }
	                if (resultadoIni.length > 0) {	
	                	mongRgr.unificar(resultadoIni);
	                }
	                if (resultadoFin.length > 0) {	
	                	mongRgr.unificar(resultadoFin);
	                }
	                
	            } catch (Exception e) {
	                error("Place");
	            }
	            
	         //5- Buscar los sitios cercanos al origen y destino de nuestra base de datos 

//	            	LatLng l1 = new LatLng(cIni.getX(), cIni.getY());
//	            	LatLng l2 = new LatLng(cFin.getX(), cFin.getY());
//	            	disRgr.setContext(API_KEY);
//	            	disRgr.estimateRouteTime(new DateTime(), utilRgr.getTravelMode(modoViaje), l1, l2);
	            	
	            	//Insertamos los sitios cerca del origen y destino en la coleccion resultado calculando la distancia
	            	mongRgr.setCollectionResultado (cIni.getY(), cIni.getX(),cFin.getY(), cFin.getX(), radioD, tipoSitio);
	            
	          //6 Recuperar sitios definitivos por mayor puntuacion en coleccion resultado 
            	mongRgr.getCollectionResultado(algoritmo, numSitios,distancia,puntuacion, radioD, cIni, cFin);
            	
            	
	        }else{
	            System.out.println("Lo sentimos, la clave no es correcta :(");
	        }
	        
	        
		

	}
	

	
	

}
