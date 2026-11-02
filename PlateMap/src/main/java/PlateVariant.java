package main.java;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import jakarta.persistence.*;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "PLATE_VARIANT")
public class PlateVariant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code")
	private String code;

	@Column(name = "name")
	private String name;

	@Column(name = "image_url")
	private String imageURL;

	@Column(name = "found")
	private boolean found = false;

	@Column(name = "type")
	private String type;

	@Transient
	private BufferedImage cachedImage;

	@Transient
	private boolean imageLoaded = false;
	
	@Transient
	private Map<String, Image> scaledCache = new HashMap<>();

	private static final PlateTypes plateTypes = new PlateTypes();

	private static int loaded = 0;

	public PlateVariant() {
	}

	public PlateVariant(String code, String name, String imgURL) {
		this.code = code;
		this.name = name;
		this.imageURL = imgURL;
		this.type = plateTypes.evaluateType(name);
	}

	public BufferedImage getImage() {
		if (!imageLoaded) {
			imageLoaded = true;
			try {
				cachedImage = ImageIO.read(new File(imageURL));
				return cachedImage;
			} catch (Exception e) {
				cachedImage = null;
			}
		}
		return cachedImage;
	}

	public Image getScaledImage(int width, int height) {
	    String key = width + "x" + height;

	    if (scaledCache.containsKey(key))
	        return scaledCache.get(key);

	    BufferedImage img = getImage();
	    if (img == null)
	        return null;

	    Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	    scaledCache.put(key, scaled);
	    return scaled;
	}


	public ImageIcon getIcon() {
		BufferedImage img = getImage();
		return img == null ? null : new ImageIcon(img);
	}

	public String getName() {
		return name;
	}

	public boolean isFound() {
		return found;
	}

	public void setFound(boolean found) {
		this.found = found;
	}

	public String getType() {
		return type;
	}

	public String getImgURL() {
		return imageURL;
	}

	public String getCode() {
		return code;
	}
}