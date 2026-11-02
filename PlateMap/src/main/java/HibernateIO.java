package main.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class HibernateIO {

	private static SessionFactory savesSessionFactory;
	private static SessionFactory platesSessionFactory;
	
	private static final Map<String, List<PlateVariant>> cache = new HashMap<>();

	private static int mode = 0; // mode 0 = HSQLDB, mode 1 = REAL Database

	public static void connect() {
		if (savesSessionFactory != null)
			return;
		try {
			if (mode == 0) {
				Configuration savesConfig = new Configuration();

				// Connection settings
				savesConfig.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbc.JDBCDriver");
				savesConfig.setProperty("hibernate.connection.url", "jdbc:hsqldb:file:platemap_data/db;shutdown=true");
				savesConfig.setProperty("hibernate.connection.username", "SA");
				savesConfig.setProperty("hibernate.connection.password", "");
				savesConfig.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
				savesConfig.setProperty("hibernate.hbm2ddl.auto", "update");
				savesConfig.setProperty("hibernate.show_sql", "false");

				// Register entities
				savesConfig.addAnnotatedClass(SaveRecord.class);

				savesSessionFactory = savesConfig.buildSessionFactory();
				System.out.println("SAVES BOOTED");

				Configuration platesConfig = new Configuration();

				platesConfig.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbc.JDBCDriver");
				platesConfig.setProperty("hibernate.connection.url", "jdbc:hsqldb:file:platemap_data/db;shutdown=true");
				platesConfig.setProperty("hibernate.connection.username", "SA");
				platesConfig.setProperty("hibernate.connection.password", "");
				platesConfig.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
				platesConfig.setProperty("hibernate.hbm2ddl.auto", "update");
				platesConfig.setProperty("hibernate.show_sql", "false");

				platesConfig.addAnnotatedClass(PlateVariant.class);

				platesSessionFactory = platesConfig.buildSessionFactory();
				System.out.println("PLATES BOOTED");

				System.out.println("Hibernate ready.");

			} else if (mode == 1) {
				Configuration config = new Configuration();

				// =============================================
				// make config for real database
				// =============================================

				config.addAnnotatedClass(SaveRecord.class);

				savesSessionFactory = config.buildSessionFactory();
				System.out.println("Hibernate ready.");
			}
		} catch (Exception e) {
			System.err.println("Hibernate failed: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Cannot start without database", e);
		}
	}

	public static void shutdown() {
		if (savesSessionFactory != null) {
			savesSessionFactory.close();
		}
	}

	public static void save(String saveName, ArrayList<Plate> plates) {
		try (Session session = savesSessionFactory.openSession()) {
			Transaction tx = session.beginTransaction();
			session.createMutationQuery("DELETE FROM SaveRecord r WHERE r.saveName = :name")
					.setParameter("name", saveName).executeUpdate();
			session.persist(new SaveRecord(saveName, "", ""));
			for (Plate plate : plates) {
				for (PlateVariant v : plate.getVariants()) {
					if (v.isFound()) {
						session.persist(new SaveRecord(saveName, plate.getCode(), v.getName()));
					}
				}
			}
			tx.commit();
		}
	}

	public static void load(String saveName, ArrayList<Plate> plates) {
		for (Plate plate : plates)
			for (PlateVariant v : plate.getVariants())
				v.setFound(false);

		try (Session session = savesSessionFactory.openSession()) {
			List<SaveRecord> records = session
					.createSelectionQuery("FROM SaveRecord r WHERE r.saveName = :name", SaveRecord.class)
					.setParameter("name", saveName).getResultList();

			for (SaveRecord rec : records) {
				if (rec.getStateCode().isEmpty())
					continue;
				for (Plate plate : plates) {
					if (!plate.getCode().equals(rec.getStateCode()))
						continue;
					for (PlateVariant v : plate.getVariants()) {
						if (v.getName().equals(rec.getVariantName())) {
							v.setFound(true);
						}
					}
				}
			}
		}
	}

	public static void newSave(String saveName) {
		try (Session session = savesSessionFactory.openSession()) {
			Transaction tx = session.beginTransaction();
			session.persist(new SaveRecord(saveName.replace(" ", "_"), "", ""));
			tx.commit();
		}
	}

	public static String[] getSaveNames() {
		try (Session session = savesSessionFactory.openSession()) {
			List<String> names = session.createSelectionQuery(
					"SELECT DISTINCT r.saveName FROM SaveRecord r ORDER BY r.saveName", String.class).getResultList();
			return names.toArray(new String[0]);
		}
	}

	public static boolean saveExists(String saveName) {
		try (Session session = savesSessionFactory.openSession()) {
			Long count = session
					.createSelectionQuery("SELECT COUNT(r) FROM SaveRecord r WHERE r.saveName = :name", Long.class)
					.setParameter("name", saveName).getSingleResult();
			return count > 0;
		}
	}

	public static void importPlateVariants(String csvPath) {
		int loaded = 0;
		try {
			 InputStream in = HibernateIO.class.getResourceAsStream(csvPath);
			  if (in == null) {
			      throw new FileNotFoundException("us-license-plates.csv not found on classpath");
			  }
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;

			br.readLine();

			Session session = platesSessionFactory.openSession();
			Transaction plateTX = session.beginTransaction();

			session.createMutationQuery("DELETE FROM PlateVariant").executeUpdate();
			
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");

				if (parts.length > 2) {
					String code = parts[0];
					String name = parts[1];
					String img = parts[2];
					
					String root = new File("").getAbsolutePath();
					String path = root.substring(0, root.lastIndexOf("\\") + 1);

					String imagePath = path.replace("\\", "/") + "PlateMap/plates/" + code + "/" + img;

					PlateVariant plateVar = new PlateVariant(code, name, imagePath);
					loaded++;

					session.persist(plateVar);
				}
			}
			
			System.out.println("LOADED PLATES");

			plateTX.commit();
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<PlateVariant> getDBVariants(String code) {
		Session session = platesSessionFactory.openSession();

		return (ArrayList<PlateVariant>) cache.computeIfAbsent(code,
				c -> session.createQuery("FROM PlateVariant WHERE code = :c", PlateVariant.class).setParameter("c", c)
						.getResultList());
	}
}