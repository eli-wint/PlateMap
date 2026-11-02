package main.java;

import java.util.ArrayList;
import java.util.Arrays;

// Only used for the purpose of getting which statistic each plate would fall into
public class PlateTypes {
	private ArrayList<String> military;
	private ArrayList<String> animal;
	private ArrayList<String> veteran;
	private ArrayList<String> university;
	private ArrayList<String> sport;

	public PlateTypes() {
		military = new ArrayList<>(
				Arrays.asList("air force", "army", "navy", "naval", "marine", "coast guard", "national guard",
						"infantry", "combat", "airborne", "sergeant", "military", "armed forces", "pow", "mia"));

		veteran = new ArrayList<>(Arrays.asList("veteran", "purple heart", "medal of honor", "bronze star",
				"silver star", "disabled vet", "combat vet"));

		animal = new ArrayList<>(Arrays.asList("dog", "wolf", "moose", "cat", "bear", "eagle", "bird", "deer", "elk",
				"fish", "wildlife", "turkey", "duck", "fox", "badger", "turtle", "horse", "bison", "buffalo", "falcon",
				"hawk", "pets", "nature", "wild"));

		university = new ArrayList<>(
				Arrays.asList("university", "college", "state university", "tech", "polytechnic", "alumni"));

		sport = new ArrayList<>(Arrays.asList("home of", "go ", "pride of", "fighting", "wildcats", "bulldogs",
				"tigers", "eagles", "hawks", "raiders", "patriots", "warriors", "knights"));
	}

	public String evaluateType(String input) {
		String lower = input.toLowerCase();

		for (String keyword : veteran)
			if (lower.contains(keyword))
				return "veteran";
		for (String keyword : military)
			if (lower.contains(keyword))
				return "military";
		for (String keyword : sport)
			if (lower.contains(keyword))
				return "sport";
		for (String keyword : university)
			if (lower.contains(keyword))
				return "university";
		for (String keyword : animal)
			if (lower.contains(keyword))
				return "animal";

		return "other";
	}
}
