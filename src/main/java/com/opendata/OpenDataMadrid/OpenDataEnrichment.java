package  com.opendata.OpenDataMadrid;

import static com.mongodb.client.model.Filters.near;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;


public class OpenDataEnrichment {

	@SuppressWarnings("unchecked")
	public static <E> void main(String args[]) {


		try {
		//Conexión al Server de MongoDB Pasandole el host y el puerto
			MongoClient mongoClient = new MongoClient("localhost", 27017);

		//Conexión a la base de datos
			MongoDatabase  db = mongoClient.getDatabase("gastrorecomendator");

		//Obtenemos la coleccion de buscadores de internet
			MongoCollection<Document>  buscadores = db.getCollection("BuscadoresDatasets");
		//Obtenemos la coleccion de los datos abiertos de la comunidad de madrid
			MongoCollection<Document>  opendata = db.getCollection("OpenDataMadrid");

		// Busco todos los documentos de la colección 
			MongoCursor<Document> cursorBuscadores = buscadores.find().iterator();
			
		    try {
		    	Document documentBus;
		    	Document documentOpen;
		    	Document locationOpen,locationBus;
		    	Position coordenadas;
				String nombre, nombreOpen;
				Double rating,ratingOpen;
				String referenciaBus = "";
				String referenciaOpen = "";
				String typeOpen ="";
				String typeBus = "";
				Bson query; 
				String precioOpen ="";
				Double reviews , reviewsOpen;
				Double reviewsVar = 0.0;
		        UtilRgr util = new UtilRgr();
		        Boolean encLocation = false;
		        Boolean encReference = true;
				while (cursorBuscadores.hasNext()) {
					
				//Parsear a un objeto sitio el cursor de busqueda
					documentBus = cursorBuscadores.next(); 
					System.out.println(documentBus.toString());
					locationBus = (Document)documentBus.get("location");
					coordenadas =   new Position((ArrayList<Double>)locationBus.get("coordinates"));
			    	nombre = (String)  documentBus.get("Name");
			    	rating = (Double) documentBus.get("Rating");
			    	reviews = (Double) documentBus.get("Reviews");
			    	referenciaBus = (String) documentBus.get("Reference");
			    	typeBus = (String)  documentBus.get("Type");

			    	query = near("location.coordinates", new Point(coordenadas),5.0,0.0);

				    //Buscar en la coleccion OpendaDataMadrid un sitio con las coordenadas del cursor
			    	documentOpen = opendata.find(query).first();
			    	//Si existe un algun documento recogemos sus valores
			    	if (documentOpen != null) {
			    		locationOpen = (Document)documentOpen.get("location");
				        referenciaOpen = (String) documentOpen.get("Reference");
				        typeOpen = (String) documentOpen.get("Type");
				        encLocation =  util.matchPlace(locationOpen, locationBus);
						encReference = util.matchReferences(referenciaBus,referenciaOpen);
					}
			    	
			    	// Actualizamos el sitio si encuentra un documento donde las coordenadas sean iguales y no se haya enriquecido desde el mismo buscador
			    	if (documentOpen != null && encLocation && !encReference && util.matchingStrings(typeBus,typeOpen)) {

			    			System.out.println(documentOpen.toString());
							//Si existe alguno cogerlo y parsearlo a un objeto sitio 
					      
			    			ratingOpen = (Double) documentOpen.get("Rating");
					        reviewsOpen = (Double) documentOpen.get("Reviews");
					    	nombreOpen = (String)  documentOpen.get("Name");

					        referenciaOpen = referenciaOpen + "," + referenciaBus; //Actualizamos la referencia siempre que encontramos un sitio
					        
					        if (ratingOpen.equals(0.0)){//Sino tiene rating --> Actualizamos el nombre, rating, reviews
					        	
					        	nombreOpen = nombre;
					        	ratingOpen = rating; 
					        	reviewsOpen = reviews;
					        	
					        }else { //Si ya tiene rating actualizamos con la suma de reviews y rating  
					        	
					        	reviewsVar = reviews + reviewsOpen;
					        	
					        	//Division de las sumas de los productos de numero de opiniones por rating entre la suma del numero de opiniones total
					        	ratingOpen = (reviews * rating + reviewsOpen * ratingOpen)/reviewsVar;  
					        	
					        }
					        if (documentOpen.get("Price")!= null && documentBus.get("Price")!= null){
					        	//Sino tiene precio se actualiza, sino se deja el que ya tenia
					        	precioOpen = (String) documentOpen.get("Price");
					        	if (precioOpen.equalsIgnoreCase("")){
					        		precioOpen = (String) documentBus.get("Price");
					        	}
					        }
					        
					        Document updated = new Document();
					        Document updateDoc = new Document();
					        updateDoc.put("Name",nombreOpen);
					        updateDoc.put("Rating", ratingOpen);
					        updateDoc.put("Price", precioOpen);
					        updateDoc.put("Reviews", reviewsVar);
					        updateDoc.put("Reference",referenciaOpen);
					        updateDoc.put("Puntuacion",util.getPuntuacion(ratingOpen,reviewsVar));
					        updated.append("$set", updateDoc);
					        
					        //Actualizar el documento
					        opendata.updateOne(query, updated);
					         
					}else { 
			    		//Insertamos el documento del buscador 
						documentBus.put("Puntuacion", util.getPuntuacion(rating, reviews));
				        opendata.insertOne(documentBus);
			    	}
		     }
				
		
			}catch (Exception ex) {
				System.out.println("Exception: " + ex.getMessage());
				
			} finally {
				
				cursorBuscadores.close();
			}

		    // Cerrar la conexion
			mongoClient.close();

		} catch (Exception ex) {
			System.out.println("Exception al conectar al server de Mongo: " + ex.getMessage());
		}

	}
	
	
}
