package com.opendata.OpenDataMadrid;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import maps.java.Geocoding;

/**
 * 
 */

/**
 * @author Alvaro Blanco
 *
 */
public class GeocodingFilled {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String csvFile = "C:\\Users\\mva6\\eclipse-workspace\\Mapas\\src\\files\\Trip4.csv";
		FileWriter fichero = null;
		BufferedReader br = null;
		String line = "";
		//Se define separador "," 
		String cvsSplitBy = ";";
		
		PrintWriter pw = null;
		try {
		    //br = new BufferedReader(new FileReader(csvFile));
		    br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "UTF8"));
		    fichero = new FileWriter("C:\\Users\\mva6\\eclipse-workspace\\Mapas\\src\\files\\trip5.txt");
		    pw = new PrintWriter(fichero);

		    String dat = "";
		    while ((line = br.readLine()) != null) {                
		        String[] datos = line.split(cvsSplitBy);
		        //Imprime datos.	
		        dat = "";
		        for (int i= 0;i<datos.length;i++) {
		        	dat = dat + datos[i] + ";" ;
		        }
		        Geocoding ObjGeocod=new Geocoding();
		       Point2D.Double resultadoCD=ObjGeocod.getCoordinates(datos[0]+ ", Madrid");
		       pw.println(dat +resultadoCD.x + ";" + resultadoCD.y);
		       System.out.println(dat +resultadoCD.x + ";" + resultadoCD.y);
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    if (br != null) {
		        try {
		            br.close();
		            // Nuevamente aprovechamos el finally para 
				    // asegurarnos que se cierra el fichero.
				    if (null != fichero)
				    	fichero.close();
				 } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
		}
	}

}
