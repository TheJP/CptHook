package ch.fhnw.cpthook.server;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Server Api for CptHook level Server.
 * This first version is pretty basic and without security!
 * It will also always load all available levels so it's not really performant.
 * This is more or less meant as a proof of concept.
 */
public class ServerApi {
	public final String BaseUrl = "http://localhost:8080";
	
	/**
	 * Returns a list of saved levels
	 */
	public List<LevelResource> getLevels() {
		try {
			Client client = ClientBuilder.newClient();
			WebTarget target = client.target(BaseUrl + "/api/v1/levels");
			Response response = target.request().get();
			return response.readEntity(new GenericType<List<LevelResource>>() {});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();	
	}
	
	/**
	 * Saves a level on the server
	 */
	public void postLevels(String name, String author, String data) {
		LevelResource lr = new LevelResource();
		lr.setName(name);
		lr.setAuthor(author);
		LevelUploadResource lur = new LevelUploadResource();
		lur.setLevel(lr);
		lur.setData(data);
		System.out.println(lur);
		try {
			Client client = ClientBuilder.newClient();
			WebTarget target = client.target(BaseUrl + "/api/v1/levels");
			Response response = target.request().post(Entity.entity(lur, MediaType.APPLICATION_JSON_TYPE));
			System.out.println("status: " + response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Returns a level as a string (json format) or null if an error occurred
	 */
	public String getLevel(String id) {
		try {
			Client client = ClientBuilder.newClient();
			WebTarget target = client.target(BaseUrl + "/levels/" + id + ".save");
			return target.request().get().readEntity(String.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
			
}
