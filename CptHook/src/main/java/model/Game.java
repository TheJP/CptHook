package model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Game extends Object{
	
	@Getter @Setter
	private int width;
	@Getter @Setter
	private int height;
	
	@Getter @Setter
	private Player player;
	@Getter @Setter
	private List<NPO> npos;
	
	
}
