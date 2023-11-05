package WordGames;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

public class Words {
	
	

	Vector<String> wordVector = new Vector<String>();
	
	public Words(String fileName) {
		try {
			FileReader reader = new FileReader(fileName);
			Scanner sc = new Scanner(reader);
			
			while(sc.hasNext()) {
				String line = sc.nextLine();
				wordVector.add(line);
				
			}
			
			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("파일이 없습니다.");
			System.exit(0);
			e.printStackTrace();
		}
	}
	
	public String getRandomWord() {
		
		Random random = new Random();
		String word = wordVector.get(random.nextInt(wordVector.size()));
		
		
		return word;
		
	}
	
	public void printAllwords() {
		for(String w : wordVector) {
			System.out.println(w);
		}
	}
}
