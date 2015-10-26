package main;

import java.io.File;
import java.io.IOException;

import loader.ModelLoader;
import model.Game;
import model.Block;

public class Main {

	public static void main(String[] args) throws IOException {
		ModelLoader ml = new ModelLoader();
		
		Game game = ml.load(new File("save.json"));
		
		Block block = (Block) game.getNpos().get(0);
		
		System.out.println(block.getX() + " - " + block.getY());
		
		int tmp = block.getX();
		block.setX(block.getY());
		block.setY(tmp);
		
		ml.save(new File("save.json"), game);
	}

}
