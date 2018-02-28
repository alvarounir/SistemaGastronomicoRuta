package  com.opendata.OpenDataMadrid;

import static com.mongodb.client.model.Filters.near;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;


public class MongoRgr {

	
	public void unificar (String[][] resultados) {

		try {
		//Conexión al Server de MongoDB Pasandole el host y el puerto
			MongoClient mongoClient = new MongoClient("localhost", 27017);

		//Conexión a la base de datos
			MongoDatabase  db = mongoClient.getDatabase("gastrorecomendator");

		//Obtenemos la coleccion de los datos abiertos de la comunidad de madrid
			MongoCollection<Document>  opendata = db.getCollection("OpenDataMadrid");

		    try {
		    	Document documentOpen;
		    	Position coordenadas;
		    	ArrayList<Double>  coordenadas2 = new ArrayList<Double>();     
				String  referenciaOpen;
				Double ratingOpen,ratingDoc;
				Bson query; 
				Double reviewsOpen;
		        UtilRgr utilRgr = new UtilRgr();

				
				 for(int i=0;i<resultados.length;i++){
                     
					 	Document resDoc = new Document();
					
					 	resDoc.put("Reference", "Google Maps");
					 	resDoc.put("Name", resultados[i][0]);
					 	resDoc.put("Address", resultados[i][1]);
					 	resDoc.put("Reviews", new Double(0.0));
					 	resDoc.put("location", new BasicDBObject()
				                .append("type", "Point")
				                .append("coordinates", Arrays.asList(new Double(resultados[i][3]), new Double(resultados[i][2]))));
					 	resDoc.put("Rating",new Double(resultados[i][6]));
					 	resDoc.put("Puntuacion",new Double(resultados[i][6]));
					 	resDoc.put("Type",resultados[i][7]);
					 	resDoc.put("PlaceId", resultados[i][8]);

				        
				    	coordenadas2.add(new Double(resultados[i][3]));
				    	coordenadas2.add(new Double(resultados[i][2]));
						coordenadas =   new Position(coordenadas2);

				    	query = near("location.coordinates", new Point(coordenadas),5.0,0.0);

				    	 //Buscar en la coleccion OpendaDataMadrid un sitio con las coordenadas del cursor
				    	documentOpen = opendata.find(query).first();

				    	// Actualizamos el documento si se ha encontrado uno que no se haya insertado ya
				    	if (documentOpen != null && documentOpen.get("PlaceId") == null && utilRgr.matchingStrings(resDoc.getString("Type"),documentOpen.getString("Type"))) {
				    		
			    			System.out.println("Actualizamos: " + documentOpen.toString());

					    			
							//Si existe alguno cogerlo y parsearlo a un objeto sitio 
					        ratingOpen = (Double) documentOpen.get("Rating");
					        reviewsOpen = (Double) documentOpen.get("Reviews");
					        referenciaOpen = (String) documentOpen.get("Reference");
					        ratingDoc  = resDoc.getDouble("Rating");

					        referenciaOpen = referenciaOpen + "+" + resDoc.getString("Reference"); //Actualizamos la referencia siempre que encontramos un sitio
					        
					        if (ratingOpen.equals(0.0)){//Sino tiene rating --> Actualizamos el nombre, rating, reviews
					        	
					        	ratingOpen =  ratingDoc; 
					        	
					        }else { //Si ya tiene rating actualizamos con la suma de reviews y rating  
					        						        	
					        	//Division de las sumas de los productos de numero de opiniones por rating entre la suma del numero de opiniones total
					        	ratingOpen = (ratingDoc + reviewsOpen * ratingOpen)/(reviewsOpen+1.0);  
					        	
					        }
					        Document updated = new Document();
					        Document updateDoc = new Document();
					        updateDoc.put("Name",resDoc.getString("Name"));
					        updateDoc.put("Rating", ratingOpen);
					        updateDoc.put("Reference",referenciaOpen);
					        updateDoc.put("Puntuacion",utilRgr.getPuntuacion(ratingOpen,reviewsOpen+1));
					        updateDoc.put("PlaceId",resDoc.getString("PlaceId"));
					        updated.append("$set", updateDoc);
					        
					        //Actualizar el documento 
					        opendata.updateOne(query, updated);
					         
					}else if (documentOpen== null){ 
			    		//Insertamos el documento de Google Maps 
					 	
					 	System.out.println("Insertamos: " + resDoc.toString());
				        opendata.insertOne(resDoc);
			    	}
				    coordenadas2.clear();

		     }
		
			}catch (Exception ex) {
				System.out.println("Exception: " + ex.getMessage());
				
			}

		    // Cerrar la conexion
			mongoClient.close();

		} catch (MongoException ex) {
			System.out.println("Exception al conectar al server de Mongo: " + ex.getMessage());
		}

	}
	
	
	public void setCollectionResultado (Double c1x, Double c1y,Double  c2x,Double c2y, Double radio, String type) {

		try {
		//Conexión al Server de MongoDB Pasandole el host y el puerto
			MongoClient mongoClient = new MongoClient("localhost", 27017);

		//Conexión a la base de datos
			MongoDatabase  db = mongoClient.getDatabase("gastrorecomendator");

		//Obtenemos la coleccion de los datos abiertos de la comunidad de madrid
			MongoCollection<Document>  opendata = db.getCollection("OpenDataMadrid");
			
			MongoCollection<Document>  colResultado = db.getCollection("ResultadoCollection");


		    try {
		    	FindIterable<Document> resultado1;
		    	FindIterable<Document> resultado2;
		    	DistanciaRgr distRgr = new DistanciaRgr();
		    	Bson query1,query2; 
		    	Bson queryId;

				query1 = Filters.and(near("location.coordinates", c1x,c1y,radio,0.0),Filters.gt("Puntuacion",3.5),Filters.eq("Type",type));

		    	//Buscar en la coleccion OpendaDataMadrid un sitio con las coordenadas del cursor
		    	resultado1 = opendata.find(query1).limit(300);
		    	
		    	query2 = Filters.and(near("location.coordinates", c2x,c2y,radio,0.0),Filters.gt("Puntuacion",3.5),Filters.eq("Type",type));

		    	//Buscar en la coleccion OpendaDataMadrid un sitio con las coordenadas del cursor
		    	resultado2 = opendata.find(query2).limit(300);

		    	 for (Document doc1 : resultado1) {

		    		 doc1.append("Distancia", distRgr.sumaDistancia(doc1,c1x, c1y, c2x, c2y)- distRgr.distanciaCoord(c1x, c1y, c2x, c2y));
		    		 queryId = Filters.eq("_id",doc1.getObjectId("_id"));

		    		 if (colResultado.find(queryId).first()== null) {
		    			 colResultado.insertOne(doc1);
			    		 //System.out.println("Insertamos el doc1: " + doc1.toString());
		    		 }
 		            
		         }		  
		    	 for (Document doc2 : resultado2) {
		    		 
		    		 doc2.append("Distancia", distRgr.sumaDistancia(doc2,c1x, c1y, c2x, c2y)- distRgr.distanciaCoord(c1x, c1y, c2x, c2y));
		    		 queryId = Filters.eq("_id",doc2.getObjectId("_id"));

		    		 if (colResultado.find(queryId).first()== null) {
		    			 colResultado.insertOne(doc2);
			             //System.out.println("Insertamos el doc2: " + doc2.toString());
		    		 }
		    		 
		         }
		    	 
		    
		    }catch (Exception ex) {
				System.out.println("Exception: " + ex.getMessage());
				
			}

		    
		    // Cerrar la conexion
			mongoClient.close();

		} catch (MongoException ex) {
			System.out.println("Exception al conectar al server de Mongo: " + ex.getMessage());
		}

		
		
	}
	
	public void getCollectionResultado (String algoritmo, Integer numSitios,Integer distancia, Double puntuacion,  Double radio, Point2D.Double inicio, Point2D.Double fin) {

		try {
		//Conexión al Server de MongoDB Pasandole el host y el puerto
			MongoClient mongoClient = new MongoClient("localhost", 27017);

		//Conexión a la base de datos
			MongoDatabase  db = mongoClient.getDatabase("gastrorecomendator");
			
			MongoCollection<Document>  colResultado = db.getCollection("ResultadoCollection");

			try {
		    	FindIterable<Document> resultado;
		    	Bson querySort;
		    	Bson query;
		    	
				if (algoritmo.equals("puntuacion")) {	 
				
					query = Filters.lt("Distancia", 2*radio); //Puntuacion mayor que 7 y Distancia a la ruta de menos de la distancia entre el inicio y el fin
					querySort = Sorts.descending("Puntuacion");
					resultado = colResultado.find(query).sort(querySort).limit(numSitios);

				}else if (algoritmo.equals("distancia")) {	 
				
					querySort = Sorts.ascending("Distancia");
					resultado = colResultado.find().sort(querySort).limit(numSitios);
			    
				}else  {	//algoritmo relevancia 
					querySort = Sorts.orderBy(Sorts.ascending("Distancia"));
					query = Filters.and(Filters.gt("Puntuacion", puntuacion),Filters.lt("Distancia", distancia)); //Puntuacion mayor que 7 y Distancia menos de 300m
					resultado = colResultado.find(query).sort(querySort).limit(15);
				}
				
		    	
		    	if (resultado != null) {
		    		System.out.println("Los sitios recomendados son:"); 
		    	}
		    	int i = 1; 
		    	
		    	ArrayList<Double> coordenadas;
		        
		        Document location;
				
				String waypoints = "";

				DecimalFormat dfD = new DecimalFormat("#.#");
				DecimalFormat dfP = new DecimalFormat("#.##");

				
				for (Document document : resultado) {
		    	
		    		location = (Document)document.get("location");
					coordenadas = (ArrayList<Double>)location.get("coordinates");
					
		    		System.out.println("Sitio numero"+i+ ":\t" + document.getString("Name") + "\tDireccion:"+ document.getString("Address") +"\tPuntuacion:"+ dfP.format(document.getDouble("Puntuacion")).toString() + "\tDistancia a la ruta (metros):"+ dfD.format(document.getDouble("Distancia")).toString());
		    		//System.out.println("Coordenadas son: [" +  coordenadas.get(1).toString() + ","+ coordenadas.get(0).toString() + "]");
		    		//System.out.println(coordenadas.get(1).toString() + ","+ coordenadas.get(0).toString());
		    		if (i<6) {
		    			waypoints = waypoints+coordenadas.get(1).toString() + ","+ coordenadas.get(0).toString()+",\n";
		    		}
		    		i++;

				}
				//ruta.getRoute(inicio, fin,waypoints ,true, Route.mode.walking, Route.avoids.nothing);
				
				System.out.println(inicio.getX()+","+inicio.getY()+",\n"+ waypoints.toString()+ fin.getX()+ ","+ fin.getY());
		    
		    }catch (Exception ex) {
				System.out.println("Exception: " + ex.getMessage());
				
			}
			//Eliminamos la coleccion.
			colResultado.drop();
			
		    // Cerrar la conexion
			mongoClient.close();

			
		} catch (MongoException ex) {
			System.out.println("Exception al conectar al server de Mongo: " + ex.getMessage());
		}

		
	}
	
}
