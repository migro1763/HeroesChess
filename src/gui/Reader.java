package gui;

import interfaces.Declarations;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.ArrayList;

public class Reader implements Declarations {

	public final int XPOS = 0;
	public final int YPOS = 1;
	public final int WIDTH = 2;
	public final int HEIGHT = 3;

	public static ArrayList<Unit> unitList = new ArrayList<Unit>();

	public Reader() {
		// create arraylist of all units as Unit objects
		for (String unit : UNITS)
			for (String state : STATES)
				unitList.add(new Unit(unit, state));
	}

	public ArrayList<Unit> readData(String path) throws IOException {

		//get properties file
		Properties prop = new Properties();
		File file = new File(path);
		prop.load(new FileInputStream(file));

		ArrayList<Unit> sprites = new ArrayList<Unit>();
		ArrayList<String> propUnits = new ArrayList<String>(prop.stringPropertyNames());

		// set number of frames for each unit +
		for (Unit unit : unitList) {
			for (String propUnit : propUnits) {
				if(propUnit.replaceAll("[^A-Za-z_]","").equals(unit.toString())) {
					unit.frames++;
				}
			}
		}

		int[] vals = new int[4];
		for (Unit unit : unitList) {
			for (int i = 0; i < unit.frames; i++) {
				// gets the four values from each 'unit + state + frameNbr = ' line
				vals = fetchArrayFromPropFile(unit.toString() + String.valueOf(i), prop);
				if(vals != null) {
					unit.addFrmDim(vals[XPOS], vals[YPOS], vals[WIDTH], vals[HEIGHT]);
					sprites.add(unit);
				}
			}
		}

		return sprites;
	}

	public static int[] fetchArrayFromPropFile(String propertyName, Properties propFile) {
		int[] array = new int[4];
		String[] tmp;

		//combine the arrays split by whitespace
		//System.out.println("propertyName: " + propFile.getProperty(propertyName));
		String valueString = propFile.getProperty(propertyName);
		if(valueString != null) {
			tmp = valueString.split("\\s+");
		} else
			return null;

		for(int i = 0; i < 4; i++) {
			array[i] = Integer.parseInt(tmp[i]);
		}
		return array;
	}
}