package ch.fhnw.cpthook.server;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LevelUploadResource {
	
	@JsonProperty("Level")
	private LevelResource level;
	@JsonProperty("Data")
	private String data;
	
	public LevelResource getLevel() {
		return level;
	}
	public void setLevel(LevelResource level) {
		this.level = level;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "LevelUploadResource [level=" + level + ", data=" + data + "]";
	}
}
