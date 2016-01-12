package ch.fhnw.cpthook.server;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LevelResource {
	@JsonProperty("ID")
	private String id;
	@JsonProperty("Name")
	private String name;
	@JsonProperty("Author")
	private String author;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public String toString() {
		return "Name: " + name + ", Author: " + author;
	}
}
