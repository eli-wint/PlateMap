package main.java;

import java.util.ArrayList;

public class Plate {
    private String state;
    private String code;
    private int x, y;
    private ArrayList<PlateVariant> variants = new ArrayList<>();

    public Plate(String state, String code, int x, int y) {
        this.state = state;
        this.code = code;
        this.x = x;
        this.y = y;
        loadVariants();
    }

    public Plate() {}
	
	private void loadVariants() { // refactor this to work with database
		variants = HibernateIO.getDBVariants(code);
	}

	public String getState() {
		String[] temp = state.split("_");
		String result = "";
		for (String str : temp)
			result += str.substring(0, 1).toUpperCase() + str.substring(1) + " ";
		return result.trim();
	}

	public String getCode() {
		return code;
	}

	public ArrayList<PlateVariant> getVariants() {
		return variants;
	}

	public PlateVariant getDefaultVariant() {
		return variants.isEmpty() ? null : variants.get(0);
	}

	public int[] getCoordinates() {
		return new int[] { x, y };
	}

	public int getFoundCount() {
		int count = 0;
		for (PlateVariant v : variants)
			if (v.isFound())
				count++;
		return count;
	}

	public int[] getAnimalCount() {
		int[] animal = new int[2];
		for (PlateVariant v : variants) {
			if (v.getType().equals("animal")) {
				animal[0]++;
				if (v.isFound())
					animal[1]++;
			}
		}
		return animal;
	}

	public int[] getMilitaryCount() {
		int[] military = new int[2];
		for (PlateVariant v : variants) {
			if (v.getType().equals("military")) {
				military[0]++;
				if (v.isFound())
					military[1]++;
			}
		}
		return military;
	}

	public int[] getVeteranCount() {
		int[] veteran = new int[2];
		for (PlateVariant v : variants) {
			if (v.getType().equals("veteran")) {
				veteran[0]++;
				if (v.isFound())
					veteran[1]++;
			}
		}
		return veteran;
	}

	public int[] getUniversityCount() {
		int[] university = new int[2];
		for (PlateVariant v : variants) {
			if (v.getType().equals("university")) {
				university[0]++;
				if (v.isFound())
					university[1]++;
			}
		}
		return university;
	}

	public int[] getSportCount() {
		int[] sport = new int[2];
		for (PlateVariant v : variants) {
			if (v.getType().equals("sport")) {
				sport[0]++;
				if (v.isFound())
					sport[1]++;
			}
		}
		return sport;
	}

	public int[] getTotalCount() {
		int[] total = new int[2];
		for (PlateVariant v : variants) {
			total[0]++;
			if (v.isFound())
				total[1]++;
		}
		return total;
	}
}