package com.opendata.OpenDataMadrid;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

import org.bson.Document;

import com.google.maps.model.TravelMode;
import com.mongodb.client.model.geojson.Position;

import maps.java.Places.Rankby;

public class UtilRgr {
	
	
	/**
     * Devuelve el tipo de ordenacion de google. 
     */
	public Rankby getRankBy(String algoritmo) {
		
		if (algoritmo.equals("distancia")) {
			return Rankby.distance;
			
		}else return Rankby.prominence; //En cualquiera de los otros casos, puntuacion o relevancia el rankBy es prominence
		
	}
	
	/**
     * Devuelve el tipo de trasnporte de google. 
     */
	public TravelMode getTravelMode(String modo) {
		
		
		if (modo.equals("coche")) {
			return TravelMode.DRIVING;
			
		}else if (modo.equals("bicicleta")) {
			return TravelMode.BICYCLING;	
				
		}else return TravelMode.WALKING; //En cualquiera de los otros casos, puntuacion o relevancia el rankBy es prominence
		
	}
	
	/**
	 *  Funcion que nos devuelve el valor del sitio gastronomico en base a su rating y numero de reviews siguiendo la formula
     *  VS = Rating + (Reviews/G)
     * 	Rating < 3   G = 800
     * 	4> Rating < 3  G = 500
     * 	4.5> Rating > 4  G = 400
     * 	5 > Rating > 4,5  G = 200
     * 	5 = Rating    G = 50 
     */
	public Double getPuntuacion(Double rating,Double reviews) {
	
		Double puntuacion;
		Integer grado = 800;
		
		//VS = Rating + (Reviews/G)
	
		if (rating< 3) {
			grado = 800;
		}else if (rating>= 3 && rating < 4) {
				grado = 500;
			}else if (rating>= 4 && rating < 4.5) {
					grado = 400;	
				}else if (rating>= 4.5 && rating < 5) {
						grado = 200;
					}else if(rating == 5.0) {
							grado = 50;
					}
		puntuacion = rating + (reviews/grado);
	
		return puntuacion;
	}
	
	/**
     * Comprueba si dos coordenadas son iguales hasta el 3er decimal
     */
	@SuppressWarnings("unchecked")
	public Boolean matchPlace(Document location1, Document location2) throws ParseException {
		
		Boolean encontrado = false; 
		DecimalFormat df = new DecimalFormat("#.###");
		NumberFormat nf = NumberFormat.getInstance();
    	Position coordenadas1,coordenadas2;

		coordenadas1 =   new Position((ArrayList<Double>)location1.get("coordinates"));
		coordenadas2 =   new Position((ArrayList<Double>)location2.get("coordinates"));

		if (Double.compare(nf.parse(df.format(coordenadas1.getValues().get(0))).doubleValue(),nf.parse(df.format(coordenadas2.getValues().get(0))).doubleValue()) == 0 && 
			Double.compare(nf.parse(df.format(coordenadas1.getValues().get(1))).doubleValue(),nf.parse(df.format(coordenadas2.getValues().get(1))).doubleValue()) == 0) {
			
			encontrado = true;
		}
		
		return encontrado;
	};
	/**
     * Comprueba si una referencia esta incluida en una cadena de referencias 
     */
	public Boolean matchReferences(String referenciaBus,String referenciaOpen) {

		Boolean encontrado = false; 
		String[] referencias;
		referencias = referenciaOpen.split(",");

		for(int i=0;i<referencias.length;i++){
			 if (referencias[i].equals(referenciaBus)) {
				 encontrado = true;
			 }
		}
		return encontrado;
	};
	
	/**
     * Comprueba si dos telefonos son iguales
     */
//	public Boolean matchTelefono(String tlf1,String tlf2) {
//
//		Boolean encontrado = false; 
//		
//		String[] aBorrar = ["(+34)"];
//				
//		eliminarStrings(dir1,)
//		for(int i=0;i<referencias.length;i++){
//			 if (referencias[i].equals(referenciaBus)) {
//				 encontrado = true;
//			 }
//		}
//		return encontrado;
//	};
	
	public Boolean matchingStrings(String s1,String s2) {
		
		Boolean valor = false;
		
		if (s1!= null && s2!= null && s1.equals(s2)) {
			valor = true;
		}
	return valor;
	}
	
	/**
     * Si en la cadena s1 se encuentra alguna de las palabras 
     * del vector a palabras a eliminar s2, no la añadimos al resultado 
     */
	public String eliminaStrings(String s1,String[] s2) {
		
		String[] cadena;
		
		String resultado="";
		
		Boolean enc = false;
		
		cadena = s1.split("");
		
		for (int i = 0;i<cadena.length;i++) {
			
			for (int j = 0;j<cadena.length;j++) {
			
				if (cadena[i].equals(s2[j])){
					
					enc = true;
				}
			}
			if (enc == false) {//Sino se ha encontrado introducimos la palabra al resultado.
			
				resultado = resultado + cadena[i]+ "";
			}
			enc = false;
		}
	return resultado;
	}

}
