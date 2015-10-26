package loader;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import model.Game;

public class ModelLoader {
	
	@Getter
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public Game load(File f) throws IOException {
		return objectMapper.readValue(f, Game.class);
	}
	
	public void save(File f, Game game) throws IOException {
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(f, game);
	}
	
}
