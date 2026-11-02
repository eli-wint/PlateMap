package main.java;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;

public class PlateMap extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5505020203473897271L;

	private int total = 0;

	private ArrayList<Plate> plates;
	private String currentSave = "Main_Save";
	private java.util.Map<Plate, JLabel[]> plateLabels = new java.util.HashMap<>();

	private JLabel militaryLabel, animalLabel, universityLabel, veteranLabel, sportLabel, totalLabel;

	private JLabel currentSaveLabel;

	// Plate index
	private JScrollPane scrollPane;
	private JPanel plateDisplayPanel;
	private JLabel plateLabel;
	private JLabel plateImage;
	private JLabel plateNameLabel;
	private JPanel thumbnailPanel;
	private JScrollPane thumbnailScroll;
	private JPanel scrollPanel;

	// Search
	private JTextField searchBar;
	private JPanel searchResultsPanel;
	private JScrollPane searchScrollPane;
	private JLabel searchPlateImage;
	private JLabel searchPlateNameLabel;
	private JLabel searchStateLabel;
	private javax.swing.Timer searchDebounceTimer;

	// Layout
	private JMenuBar bar;
	private CardLayout cardLayout;
	private JPanel cards;

	private JMenu saves;
	private JMenu saveMenu;
	private JMenuItem saveButton;
	private JMenuItem addSaveButton;

	private JPanel homePanel;
	private JPanel platePanel;
	private JPanel plateSearchPanel;
	private JPanel statsPanel;
	private JPanel mapPanel;

	// Fonts
	private Font font20;
	private Font font14;
	private Font font12;
	private Font font9;
	private Font font16;
	private String font;

	private Color highlights = new Color(28, 58, 42);
	private Color mainBody = new Color(85, 107, 85);
	private Color textColor = new Color(220, 223, 230);

	public PlateMap() {
		font = "Arial";
		font20 = new Font(font, Font.BOLD, 20);
		font16 = new Font(font, Font.PLAIN, 16);
		font14 = new Font(font, Font.PLAIN, 14);
		font12 = new Font(font, Font.PLAIN, 12);
		font9 = new Font(font, Font.PLAIN, 9);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (currentSave != null) {
					SaveIO.save(plates, currentSave);
				}
				HibernateIO.shutdown();
				dispose();
				System.exit(0);
			}
		});

		setResizable(false);
		setSize(1050, 685);
		setTitle("Plate Map");

		scrollPane = new JScrollPane();
		scrollPane.setBackground(mainBody);
		scrollPane.getViewport().setBackground(mainBody);
		scrollPane.setPreferredSize(new Dimension(220, 0));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		bar = new JMenuBar();
		bar.setBackground(highlights);
		cardLayout = new CardLayout();
		cards = new JPanel(cardLayout);
		cards.setBackground(mainBody);

		instantiatePlates();

		// HOME PAGE =============================================================
		homePanel = new JPanel(new BorderLayout());
		homePanel.setBackground(mainBody);
		JLabel label = new JLabel("HOME PAGE");
		label.setFont(font20);
		label.setForeground(textColor);
		homePanel.add(label, BorderLayout.CENTER);
		homeScreen hs = new homeScreen();
		hs.setBackground(mainBody);
		homePanel.add(hs);

		// DATA STORAGE =========================================================
		saveMenu = new JMenu("SAVES");
		saveMenu.setForeground(textColor);
		saveMenu.setBackground(highlights);
		saveMenu.getPopupMenu().setBackground(highlights);
		saveMenu.getPopupMenu().setBorder(BorderFactory.createLineBorder(highlights));

		saves = new JMenu("LOAD SAVE");
		saves.setForeground(textColor);
		saves.setBackground(highlights);
		saves.getPopupMenu().setBackground(highlights);
		saves.getPopupMenu().setBorder(BorderFactory.createLineBorder(highlights));

		for (String str : SaveIO.loadSaves()) {
			JMenuItem item = new JMenuItem(str);
			item.setBackground(highlights);
			item.setForeground(textColor);
			item.addActionListener(e -> {
				System.out.println("LOADING SAVE");
				SaveIO.load(plates, str);
				currentSave = str;
				refreshAllLabels();
				refreshStats();
				plateLabel.setText("Select a state");
				plateImage.setText("");
				plateImage.setIcon(null);
				plateNameLabel.setText("");
				thumbnailPanel.removeAll();
				cardLayout.show(cards, "HOME");
			});
			saves.add(item);
		}
		saveMenu.add(saves);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (currentSave != null) {
					SaveIO.save(plates, currentSave);
				}
				HibernateIO.shutdown();
				dispose();
				System.exit(0);
			}
		});

		saveButton = new JMenuItem("SAVE");
		saveButton.setForeground(textColor);
		saveButton.addActionListener(e -> {
			SaveIO.save(plates, currentSave);
		});
		saveButton.setBackground(highlights);
		saveButton.setForeground(textColor);
		saveMenu.add(saveButton);

		addSaveButton = new JMenuItem("NEW SAVE");
		addSaveButton.setForeground(textColor);
		addSaveButton.addActionListener(e -> {
			Prompt prompt = new Prompt(this);
			String temp = "";
			try {
				temp = prompt.getVal().replace(" ", "_");
			} catch (NullPointerException e1) {
				System.err.println("WARNING: Prompt error");
			}
			if (temp != null && !temp.isBlank()) {
				if (HibernateIO.saveExists(temp)) {
					JOptionPane.showMessageDialog(this, "Save '" + temp + "' already exists.");
				} else {
					SaveIO.newSave(temp);
					refreshSaves();
				}
			}
		});
		addSaveButton.setBackground(highlights);
		addSaveButton.setForeground(textColor);
		saveMenu.add(addSaveButton);

		bar.add(saveMenu);

		// PLATE INDEX PAGE ======================================================
		platePanel = new JPanel(new BorderLayout());
		platePanel.setBackground(mainBody);
		plateDisplayPanel = new JPanel(new BorderLayout(0, 8));
		plateDisplayPanel.setBackground(mainBody);

		plateLabel = new JLabel("Select a state", JLabel.CENTER);
		plateLabel.setFont(font20);
		plateLabel.setForeground(textColor);

		plateImage = new JLabel("", JLabel.CENTER);
		plateImage.setPreferredSize(new Dimension(600, 304));

		plateNameLabel = new JLabel("", JLabel.CENTER);
		plateNameLabel.setFont(font14);
		plateNameLabel.setForeground(textColor);

		JPanel mainPlatePanel = new JPanel(new BorderLayout(0, 4));
		mainPlatePanel.setBackground(mainBody);
		mainPlatePanel.add(plateImage, BorderLayout.CENTER);
		mainPlatePanel.add(plateNameLabel, BorderLayout.SOUTH);

		thumbnailPanel = new JPanel();
		thumbnailPanel.setBackground(mainBody);
		thumbnailPanel.setLayout(new BoxLayout(thumbnailPanel, BoxLayout.X_AXIS));
		thumbnailScroll = new JScrollPane(thumbnailPanel);
		thumbnailScroll.setBackground(mainBody);
		thumbnailScroll.getViewport().setBackground(mainBody);
		thumbnailScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		thumbnailScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		thumbnailScroll.setPreferredSize(new Dimension(0, 120));
		thumbnailScroll.getHorizontalScrollBar().setUnitIncrement(16);

		plateDisplayPanel.add(plateLabel, BorderLayout.NORTH);
		plateDisplayPanel.add(mainPlatePanel, BorderLayout.CENTER);
		plateDisplayPanel.add(thumbnailScroll, BorderLayout.SOUTH);

		JLabel plateIndexLabel = new JLabel("Plate Index");
		plateIndexLabel.setForeground(textColor);
		platePanel.add(plateIndexLabel, BorderLayout.NORTH);
		platePanel.add(scrollPane, BorderLayout.WEST);
		platePanel.add(plateDisplayPanel, BorderLayout.CENTER);

		// SEARCH PAGE ===========================================================
		plateSearchPanel = new JPanel(new BorderLayout(0, 0));
		plateSearchPanel.setBackground(mainBody);

		JPanel searchTopPanel = new JPanel(new BorderLayout(8, 8));
		searchTopPanel.setBackground(mainBody);
		searchTopPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		searchBar = new JTextField();
		searchBar.setFont(font16);
		searchBar.setBackground(highlights);
		searchBar.setForeground(textColor);
		searchBar.setCaretColor(textColor);
		JButton searchBtn = new JButton("Search");
		searchBtn.setForeground(textColor);
		searchBtn.setBackground(highlights);
		searchTopPanel.add(searchBar, BorderLayout.CENTER);
		searchTopPanel.add(searchBtn, BorderLayout.EAST);

		searchResultsPanel = new JPanel();
		searchResultsPanel.setBackground(mainBody);
		searchResultsPanel.setLayout(new BoxLayout(searchResultsPanel, BoxLayout.Y_AXIS));
		searchScrollPane = new JScrollPane(searchResultsPanel);
		searchScrollPane.setBackground(mainBody);
		searchScrollPane.getViewport().setBackground(mainBody);
		searchScrollPane.setPreferredSize(new Dimension(280, 0));
		searchScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		searchScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		searchScrollPane.getVerticalScrollBar().setUnitIncrement(16);

		JPanel searchDisplayPanel = new JPanel(new BorderLayout(0, 8));
		searchDisplayPanel.setBackground(mainBody);
		searchDisplayPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		searchStateLabel = new JLabel("", JLabel.CENTER);
		searchStateLabel.setFont(font14);
		searchStateLabel.setForeground(textColor);

		searchPlateImage = new JLabel("", JLabel.CENTER);
		searchPlateImage.setPreferredSize(new Dimension(600, 304));

		searchPlateNameLabel = new JLabel("", JLabel.CENTER);
		searchPlateNameLabel.setFont(font16);
		searchPlateNameLabel.setForeground(textColor);

		JPanel searchImagePanel = new JPanel(new BorderLayout(0, 4));
		searchImagePanel.setBackground(mainBody);
		searchImagePanel.add(searchStateLabel, BorderLayout.NORTH);
		searchImagePanel.add(searchPlateImage, BorderLayout.CENTER);
		searchImagePanel.add(searchPlateNameLabel, BorderLayout.SOUTH);

		searchDisplayPanel.add(searchImagePanel, BorderLayout.CENTER);

		JPanel searchCenterPanel = new JPanel(new BorderLayout());
		searchCenterPanel.setBackground(mainBody);
		searchCenterPanel.add(searchScrollPane, BorderLayout.WEST);
		searchCenterPanel.add(searchDisplayPanel, BorderLayout.CENTER);

		plateSearchPanel.add(searchTopPanel, BorderLayout.NORTH);
		plateSearchPanel.add(searchCenterPanel, BorderLayout.CENTER);

		ActionListener doSearch = e -> performSearch(searchBar.getText().trim());
		searchBtn.addActionListener(doSearch);
		searchBar.addActionListener(doSearch);

		searchBar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				debounceSearch();
			}

			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				debounceSearch();
			}

			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				debounceSearch();
			}
		});

		// STATS PAGE ============================================================
		statsPanel = new JPanel(new GridBagLayout());
		statsPanel.setBackground(mainBody);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 16, 8, 16);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		militaryLabel = new JLabel();
		animalLabel = new JLabel();
		universityLabel = new JLabel();
		veteranLabel = new JLabel();
		sportLabel = new JLabel();
		totalLabel = new JLabel();

		for (JLabel l : new JLabel[] { militaryLabel, animalLabel, universityLabel, veteranLabel, sportLabel,
				totalLabel }) {
			l.setFont(font16);
			l.setForeground(textColor);
		}

		Object[][] rows = { { "Statistics", null }, { "Total:", totalLabel }, { "Military:", militaryLabel },
				{ "Animal:", animalLabel }, { "University:", universityLabel }, { "Veteran:", veteranLabel },
				{ "Sport:", sportLabel }, };

		for (int i = 0; i < rows.length; i++) {
			gbc.gridy = i;
			gbc.gridx = 0;
			JLabel key = new JLabel((String) rows[i][0]);
			key.setFont(i == 0 ? font20 : font16);
			key.setForeground(textColor);
			statsPanel.add(key, gbc);

			if (rows[i][1] != null) {
				gbc.gridx = 1;
				statsPanel.add((JLabel) rows[i][1], gbc);
			}
		}

		refreshStats(); // populate with real values immediately

		// MAP PAGE ==============================================================
		try {
			InputStream is = getClass().getResourceAsStream("/main/resources/map/us-map.png");
			ImageIcon mapIcon = new ImageIcon(is.readAllBytes());
			mapPanel = new JPanel(null) {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.drawImage(mapIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
				}
			};
			mapPanel.setBackground(mainBody);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		cards.add(homePanel, "HOME");
		cards.add(platePanel, "PLATES");
		cards.add(plateSearchPanel, "SEARCH");
		cards.add(statsPanel, "STATS");
		cards.add(mapPanel, "MAP");

		// STATE LIST ============================================================
		scrollPanel = new JPanel();
		scrollPanel.setBackground(mainBody);
		scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.Y_AXIS));

		for (Plate p : plates) {
			JPanel row = new JPanel(new BorderLayout());
			row.setBackground(highlights);
			row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
			row.setOpaque(true);

			JButton stateBtn = new JButton(p.getState());
			stateBtn.setBackground(mainBody);
			stateBtn.setForeground(textColor);
			stateBtn.setFont(font16);
			stateBtn.setContentAreaFilled(false);
			stateBtn.setBorderPainted(false);
			stateBtn.setOpaque(false);
			stateBtn.setHorizontalAlignment(SwingConstants.LEFT);

			JLabel countLabel = new JLabel(p.getFoundCount() + "/" + p.getVariants().size());
			countLabel.setFont(font12);
			countLabel.setForeground(textColor);
			countLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

			String text = p.getFoundCount() + "/" + p.getVariants().size() + " " + p.getCode();
			JLabel mapLabel = new JLabel(text);
			mapLabel.setFont(font9);
			mapLabel.setForeground(Color.BLACK);
			mapLabel.setOpaque(true);
			mapLabel.setBackground(new Color(245, 245, 245));
			int[] coords = p.getCoordinates();
			FontMetrics fm = mapLabel.getFontMetrics(font9);
			int width = fm.stringWidth(text);
			int height = fm.getHeight();
			mapLabel.setBounds(coords[0], coords[1], width, height);
			mapPanel.add(mapLabel);

			stateBtn.addActionListener(e -> {
				loadStateIntoIndex(p, countLabel, mapLabel);
				cardLayout.show(cards, "PLATES");
			});

			row.add(stateBtn, BorderLayout.CENTER);
			row.add(countLabel, BorderLayout.EAST);
			scrollPanel.add(row);

			plateLabels.put(p, new JLabel[] { countLabel, mapLabel });
		}

		scrollPane.setViewportView(scrollPanel);

		bar.add(createNavButton("HOME", "HOME"));
		bar.add(createNavButton("PLATE INDEX", "PLATES"));
		bar.add(createNavButton("SEARCH", "SEARCH"));
		bar.add(createNavButton("STATISTICS", "STATS"));
		bar.add(createNavButton("PLATE MAP", "MAP"));

		bar.add(Box.createHorizontalGlue());

		currentSaveLabel = new JLabel("Current Save: " + currentSave.replace("_", " "));
		currentSaveLabel.setForeground(textColor);
		currentSaveLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		bar.add(currentSaveLabel);

		setJMenuBar(bar);
		add(cards);
		setVisible(true);

		SaveIO.load(plates, currentSave);
		refreshSaves();
	}

	private void loadStateIntoIndex(Plate p, JLabel countLabel, JLabel mapLabel) {
		plateLabel.setText(p.getState());
		thumbnailPanel.removeAll();

		for (PlateVariant v : p.getVariants()) {
			JButton thumbBtn = buildThumbButton(v, () -> {
				String text = p.getFoundCount() + "/" + p.getVariants().size();
				if (countLabel != null)
					countLabel.setText(text);
				if (mapLabel != null)
					mapLabel.setText(text + " " + p.getCode());
			});
			thumbnailPanel.add(thumbBtn);
		}

		PlateVariant def = p.getDefaultVariant();
		if (def != null) {
			Image large = def.getScaledImage(600, 304);
			plateImage.setIcon(large != null ? new ImageIcon(large) : null);
			plateImage.setText(large == null ? "No image available" : "");
			plateNameLabel.setText(def.getName());
		} else {
			plateImage.setIcon(null);
			plateNameLabel.setText("");
			plateImage.setText("No plates found for this state");
		}

		thumbnailPanel.revalidate();
		thumbnailPanel.repaint();
	}

	private JButton buildThumbButton(PlateVariant v, Runnable onFoundChanged) {
		Image thumb = v.getScaledImage(150, 76);
		JButton thumbBtn = new JButton();
		thumbBtn.setBackground(mainBody);
		if (thumb != null)
			thumbBtn.setIcon(new ImageIcon(thumb));
		thumbBtn.setToolTipText(v.getName());
		thumbBtn.setContentAreaFilled(false);
		refreshThumbBorder(thumbBtn, v);

		thumbBtn.addActionListener(e -> {
			v.setFound(true);
			refreshThumbBorder(thumbBtn, v);
			onFoundChanged.run();
			showVariantInMain(v);
			refreshStats();
		});

		thumbBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					v.setFound(false);
					refreshThumbBorder(thumbBtn, v);
					onFoundChanged.run();
					refreshStats();
				}
			}
		});

		return thumbBtn;
	}

	private void refreshThumbBorder(JButton btn, PlateVariant v) {
		btn.setBorder(v.isFound() ? BorderFactory.createLineBorder(new Color(60, 180, 60), 2)
				: BorderFactory.createLineBorder(Color.GRAY, 1));
	}

	private void showVariantInMain(PlateVariant v) {
		Image large = v.getScaledImage(600, 304);
		if (large != null) {
			plateImage.setIcon(new ImageIcon(large));
			plateImage.setText("");
		} else {
			plateImage.setIcon(null);
			plateImage.setText("No image available");
		}
		plateNameLabel.setText(v.getName());
		plateImage.revalidate();
		plateImage.repaint();
	}

	private void debounceSearch() {
		if (searchDebounceTimer != null && searchDebounceTimer.isRunning())
			searchDebounceTimer.stop();
		searchDebounceTimer = new javax.swing.Timer(300, e -> performSearch(searchBar.getText().trim()));
		searchDebounceTimer.setRepeats(false);
		searchDebounceTimer.start();
	}

	private void performSearch(String query) {
		searchResultsPanel.removeAll();

		if (query.isEmpty()) {
			searchResultsPanel.revalidate();
			searchResultsPanel.repaint();
			return;
		}

		JLabel loading = new JLabel("Searching...");
		loading.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		loading.setForeground(Color.GRAY);
		searchResultsPanel.add(loading);
		searchResultsPanel.revalidate();
		searchResultsPanel.repaint();

		String lower = query.toLowerCase();

		new SwingWorker<java.util.List<JPanel>, Void>() {
			@Override
			protected java.util.List<JPanel> doInBackground() {
				java.util.List<JPanel> rows = new ArrayList<>();

				for (Plate p : plates) {
					for (PlateVariant v : p.getVariants()) {
						if (!v.getName().toLowerCase().contains(lower) && !p.getState().toLowerCase().contains(lower))
							continue;

						Image thumbImg = v.getScaledImage(76, 38);

						JPanel resultRow = new JPanel(new BorderLayout(8, 0));
						resultRow.setBackground(highlights);
						resultRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
						resultRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

						JLabel thumb = new JLabel();
						if (thumbImg != null)
							thumb.setIcon(new ImageIcon(thumbImg));
						thumb.setPreferredSize(new Dimension(80, 48));

						JPanel textPanel = new JPanel(new BorderLayout());
						textPanel.setBackground(highlights);
						textPanel.setOpaque(false);
						JLabel nameLabel = new JLabel(v.getName());
						nameLabel.setForeground(textColor);
						nameLabel.setFont(nameLabel.getFont().deriveFont(13f));
						JLabel stateLabel = new JLabel(p.getState());
						stateLabel.setFont(stateLabel.getFont().deriveFont(11f));
						stateLabel.setForeground(Color.LIGHT_GRAY);
						textPanel.add(nameLabel, BorderLayout.CENTER);
						textPanel.add(stateLabel, BorderLayout.SOUTH);

						JLabel foundLabel = new JLabel(v.isFound() ? "✓" : "");
						foundLabel.setForeground(new Color(60, 180, 60));
						foundLabel.setFont(font16);
						foundLabel.setPreferredSize(new Dimension(24, 48));

						resultRow.add(thumb, BorderLayout.WEST);
						resultRow.add(textPanel, BorderLayout.CENTER);
						resultRow.add(foundLabel, BorderLayout.EAST);

						resultRow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						resultRow.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if (SwingUtilities.isLeftMouseButton(e)) {
									v.setFound(true);
									foundLabel.setText("✓");
									Image large = v.getScaledImage(600, 304);
									searchPlateImage.setIcon(large != null ? new ImageIcon(large) : null);
									searchPlateImage.setText(large == null ? "No image available" : "");
									searchPlateNameLabel.setText(v.getName());
									searchStateLabel.setText(p.getState());
									searchPlateImage.revalidate();
									searchPlateImage.repaint();
								} else if (SwingUtilities.isRightMouseButton(e)) {
									v.setFound(false);
									foundLabel.setText("");
								}
							}
						});

						rows.add(resultRow);
						if (rows.size() >= 100)
							return rows;
					}
				}
				return rows;
			}

			@Override
			protected void done() {
				searchResultsPanel.removeAll();
				try {
					java.util.List<JPanel> rows = get();
					if (rows.isEmpty()) {
						JLabel none = new JLabel("No results for \"" + query + "\"");
						none.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
						none.setForeground(Color.GRAY);
						searchResultsPanel.add(none);
					} else {
						for (JPanel row : rows)
							searchResultsPanel.add(row);
						if (rows.size() == 100) {
							JLabel cap = new JLabel("Showing first 100 results — refine your search");
							cap.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
							cap.setForeground(Color.GRAY);
							searchResultsPanel.add(cap);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				searchResultsPanel.revalidate();
				searchResultsPanel.repaint();
			}
		}.execute();
	}

	private void instantiatePlates() {
		plates = new ArrayList<>(Arrays.asList(new Plate("alabama", "AL", 710, 375),
				new Plate("alaska", "AK", 290, 490), new Plate("arizona", "AZ", 222, 332),
				new Plate("arkansas", "AR", 600, 338), new Plate("california", "CA", 87, 264),
				new Plate("colorado", "CO", 355, 245), new Plate("connecticut", "CT", 935, 161),
				new Plate("delaware", "DE", 915, 225), new Plate("florida", "FL", 829, 457),
				new Plate("georgia", "GA", 780, 372), new Plate("hawaii", "HI", 142, 500),
				new Plate("idaho", "ID", 212, 142), new Plate("illinois", "IL", 652, 222),
				new Plate("indiana", "IN", 705, 238), new Plate("iowa", "IA", 585, 200),
				new Plate("kansas", "KS", 482, 268), new Plate("kentucky", "KY", 718, 280),
				new Plate("louisiana", "LA", 620, 430), new Plate("maine", "ME", 975, 80),
				new Plate("maryland", "MD", 878, 218), new Plate("massachusetts", "MA", 943, 149),
				new Plate("michigan", "MI", 720, 168), new Plate("minnesota", "MN", 542, 122),
				new Plate("mississippi", "MS", 652, 392), new Plate("missouri", "MO", 590, 270),
				new Plate("montana", "MT", 310, 77), new Plate("nebraska", "NE", 472, 208),
				new Plate("nevada", "NV", 152, 212), new Plate("new_hampshire", "NH", 948, 128),
				new Plate("new_jersey", "NJ", 922, 195), new Plate("new_mexico", "NM", 340, 335),
				new Plate("new_york", "NY", 885, 148), new Plate("north_carolina", "NC", 845, 300),
				new Plate("north_dakota", "ND", 458, 90), new Plate("ohio", "OH", 762, 218),
				new Plate("oklahoma", "OK", 512, 325), new Plate("oregon", "OR", 118, 118),
				new Plate("pennsylvania", "PA", 852, 195), new Plate("rhode_island", "RI", 970, 165),
				new Plate("south_carolina", "SC", 828, 345), new Plate("south_dakota", "SD", 452, 152),
				new Plate("tennessee", "TN", 705, 318), new Plate("texas", "TX", 462, 402),
				new Plate("utah", "UT", 240, 218), new Plate("vermont", "VT", 918, 110),
				new Plate("virginia", "VA", 862, 262), new Plate("washington_dc", "DC", 873, 238),
				new Plate("washington", "WA", 145, 50), new Plate("west_virginia", "WV", 808, 250),
				new Plate("wisconsin", "WI", 622, 142), new Plate("wyoming", "WY", 325, 160)));
	}

	private JButton createNavButton(String title, String card) {
		JButton button = new JButton(title);
		button.setForeground(textColor);
		button.setBackground(mainBody);
		button.addActionListener(e -> cardLayout.show(cards, card));
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setOpaque(true);
		return button;
	}

	private class homeScreen extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5133522885648631032L;

		private static final int PLATE_WIDTH = 900;
		private static final int PLATE_HEIGHT = 456;

		private Image img;
		private float x, y, speed;

		public homeScreen() {
			pickNewPlate();

			javax.swing.Timer timer = new javax.swing.Timer(16, e -> {
				x -= speed;
				if (x + PLATE_WIDTH < 0) {
					pickNewPlate();
				}
				repaint();
			});
			timer.start();
		}

		private void pickNewPlate() {
			ArrayList<PlateVariant> all = new ArrayList<>();
			for (Plate plate : plates) {
				for (PlateVariant plateVar : plate.getVariants()) {
					if (new File(plateVar.getImgURL()).exists()) {
						all.add(plateVar);
					}
				}
			}
			PlateVariant picked = all.get((int) (Math.random() * (all.size())));
			img = picked.getScaledImage(PLATE_WIDTH, PLATE_HEIGHT);
			x = getWidth();
			y = 75;
			speed = 10;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			if (img != null)
				g2.drawImage(img, (int) x, (int) y, PLATE_WIDTH, PLATE_HEIGHT, this);
		}
	}

	private void refreshSaves() {
		saveMenu.removeAll();
		saves.removeAll();

		for (String str : SaveIO.loadSaves()) {
			JMenuItem item = new JMenuItem(str.replace("_", " "));
			item.setBackground(highlights);
			item.setForeground(textColor);
			item.addActionListener(e -> {
				System.out.println("LOADING SAVE");
				SaveIO.load(plates, str);
				currentSave = str;
				refreshAllLabels();
				refreshStats();
				plateLabel.setText("Select a state");
				plateImage.setText("");
				plateImage.setIcon(null);
				plateNameLabel.setText("");
				thumbnailPanel.removeAll();
				cardLayout.show(cards, "HOME");

				try {
					currentSaveLabel.setText("Current Save: " + currentSave.replace("_", " "));
				} catch (NullPointerException e1) {
					System.err.println("WARNING: Current save does not exist");
				}

			});
			saves.add(item);
		}

		saveMenu.add(saves);
		saveMenu.add(saveButton);
		saveMenu.add(addSaveButton);
	}

	private void refreshAllLabels() {
		for (Plate p : plates) {
			JLabel[] labels = plateLabels.get(p);
			if (labels == null)
				continue;
			JLabel countLabel = labels[0];
			JLabel mapLabel = labels[1];
			String count = p.getFoundCount() + "/" + p.getVariants().size();
			countLabel.setText(count);
			mapLabel.setText(count + " " + p.getCode());
		}
	}

	private void refreshStats() {
		int[] mil = new int[2], ani = new int[2], uni = new int[2], vet = new int[2], spt = new int[2],
				tot = new int[2];

		for (Plate plate : plates) {
			mil[0] += plate.getMilitaryCount()[0];
			mil[1] += plate.getMilitaryCount()[1];
			ani[0] += plate.getAnimalCount()[0];
			ani[1] += plate.getAnimalCount()[1];
			uni[0] += plate.getUniversityCount()[0];
			uni[1] += plate.getUniversityCount()[1];
			vet[0] += plate.getVeteranCount()[0];
			vet[1] += plate.getVeteranCount()[1];
			spt[0] += plate.getSportCount()[0];
			spt[1] += plate.getSportCount()[1];
			tot[0] += plate.getTotalCount()[0];
			tot[1] += plate.getTotalCount()[1];

			total = tot[0];
		}

		militaryLabel.setText(mil[1] + " / " + mil[0]);
		animalLabel.setText(ani[1] + " / " + ani[0]);
		universityLabel.setText(uni[1] + " / " + uni[0]);
		veteranLabel.setText(vet[1] + " / " + vet[0]);
		sportLabel.setText(spt[1] + " / " + spt[0]);
		totalLabel.setText(tot[1] + " / " + tot[0]);
	}

	public int getTotal() {
		return total;
	}
}