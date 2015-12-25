package nosnice;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DetectorController {

	@FXML
	private Button detLiceGumb;
	@FXML
	private Button odrLiceGumb;
	@FXML
	private ImageView mjestoZaSliku;
	@FXML
	private Button ucitajGumb;
	@FXML
	private Button detUstaGumb;
	@FXML
	private Button odrUstaGumb;
	@FXML
	private Button detOciGumb;
	@FXML
	private Button odrOciGumb;
	/**
	 * matrica slike koja je prikazana - stalno se mijenja
	 */
	private Mat rgb;
	private int brojLica = 0;
	private Rect rectCrop = null;

	@FXML
	private void ucitajSliku() {
		JFileChooser chooser = new JFileChooser("C:/Users/Iva Akmadža/Downloads");
		BufferedImage img;
		Image slika;
		File file;
		chooser.showOpenDialog(null);
		file = chooser.getSelectedFile();
		if (file != null) {
			try {
				img = ImageIO.read(file);
				rgb = image2Mat(img);
				slika = SwingFXUtils.toFXImage(img, null);
				mjestoZaSliku.setImage(slika);
			} catch (IOException e1) {
			}
		}
	}

	@FXML
	private void detektirajLice() {
		if (rgb != null) {
			MatOfRect faces = new MatOfRect();
			Mat grayFrame = new Mat();
			// face cascade classifier
			CascadeClassifier faceCascade = new CascadeClassifier(
					"C:/BraneProjekt/haarcascade_frontalface_default.xml");
			// convert the frame in gray scale
			Imgproc.cvtColor(rgb, grayFrame, Imgproc.COLOR_BGR2GRAY);
			// equalize the frame histogram to improve the result
			Imgproc.equalizeHist(grayFrame, grayFrame);

			if (!grayFrame.empty()) {
				// detect faces
				faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
						new Size(0.2*mjestoZaSliku.getFitWidth(), 0.2*mjestoZaSliku.getFitHeight()), new Size());

				// each rectangle in faces is a face: draw them!
				Rect[] facesArray = faces.toArray();
				brojLica = facesArray.length;
				System.out.println("broj lica:" + brojLica);
				for (int i = 0; i < facesArray.length; i++) {
					Imgproc.rectangle(rgb, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
					// ovdje stvaram pravokutnik koji mi je ROI(region of interest)
					// da bi mogao odrezat sve sto nije lice kasnije
					rectCrop = new Rect(facesArray[i].tl(), facesArray[i].br());
				}
				Image slikaZaVan = mat2Image(rgb);
				mjestoZaSliku.setImage(slikaZaVan);
			}
		} else {
			System.out.println("Slika nije ucitana!");
		}
	}

	private Image mat2Image(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer, according to the PNG format
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}

	public Mat image2Mat(BufferedImage im) {
		// Convert INT to BYTE
		// im = new BufferedImage(im.getWidth(),
		// im.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
		// Convert bufferedimage to byte array
		byte[] pixels = ((DataBufferByte) im.getRaster().getDataBuffer()).getData();

		// Create a Matrix the same size of image
		Mat image = new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);
		// Fill Matrix with image values
		image.put(0, 0, pixels);

		return image;

	}

	@FXML
	private void odreziLice() {
		// na fotografiji mora biti jedno lice, ako nije javit ce gresku
		if (brojLica != 1) {
			System.out.println("Mora biti jedno lice na slici!");
		} else {
			if (rectCrop == null) {
				System.out.println("Greska kod pravokutnika koji oznacava ROI");
			} else {
				rgb = new Mat(rgb,rectCrop);
				Image slikaZaVan = mat2Image(rgb);
				mjestoZaSliku.setImage(slikaZaVan);		
			}
		}
	}
	
	/**
	 * metoda radi na isti nacin kao i <code>detektriajLice</code>
	 * jedina razlika je sto ovdje primjenjujem algoritam za usta
	 */
	@FXML
	private void detektirajUsta() {
		if (rgb != null) {
			MatOfRect mouths = new MatOfRect();
			Mat grayFrame = new Mat();
			// face cascade classifier
			CascadeClassifier mouthCascade = new CascadeClassifier(
					"C:/BraneProjekt/Mouth.xml");
			// convert the frame in gray scale
			Imgproc.cvtColor(rgb, grayFrame, Imgproc.COLOR_BGR2GRAY);
			// equalize the frame histogram to improve the result
			Imgproc.equalizeHist(grayFrame, grayFrame);

			if (!grayFrame.empty()) {
				// detect faces
				mouthCascade.detectMultiScale(grayFrame, mouths, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
						new Size(0.3*mjestoZaSliku.getFitWidth(), 0.3*mjestoZaSliku.getFitHeight()), new Size());

				// each rectangle in faces is a face: draw them!
				Rect[] mouthsArray = mouths.toArray();
				brojLica = mouthsArray.length;
				System.out.println("broj usta:" + brojLica);
				for (int i = 0; i < mouthsArray.length; i++) {
					Imgproc.rectangle(rgb, mouthsArray[i].tl(), mouthsArray[i].br(), new Scalar(0, 255, 0), 3);
					// ovdje stvaram pravokutnik koji mi je ROI(region of interest)
					// da bi mogao odrezat sve sto nije lice kasnije
					rectCrop = new Rect(mouthsArray[i].tl(), mouthsArray[i].br());
				}
				Image slikaZaVan = mat2Image(rgb);
				mjestoZaSliku.setImage(slikaZaVan);
			}
		} else {
			System.out.println("Slika nije ucitana!");
		}
	}
	
	@FXML
	private void odreziUsta() {
		
	}
	
	/**
	 * metoda radi na isti nacin kao i <code>detektriajLice</code>
	 * jedina razlika je sto ovdje primjenjujem algoritam za oci
	 */
	@FXML
	private void detektirajOci() {
		if (rgb != null) {
			MatOfRect eyes = new MatOfRect();
			Mat grayFrame = new Mat();
			// face cascade classifier
			CascadeClassifier eyeCascade = new CascadeClassifier(
					"C:/BraneProjekt/eyes.xml");
			// convert the frame in gray scale
			Imgproc.cvtColor(rgb, grayFrame, Imgproc.COLOR_BGR2GRAY);
			// equalize the frame histogram to improve the result
			Imgproc.equalizeHist(grayFrame, grayFrame);

			if (!grayFrame.empty()) {
				// detect faces
				eyeCascade.detectMultiScale(grayFrame, eyes, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
						new Size(0.05*mjestoZaSliku.getFitWidth(), 0.05*mjestoZaSliku.getFitHeight()), new Size());

				// each rectangle in faces is a face: draw them!
				Rect[] eyesArray = eyes.toArray();
				brojLica = eyesArray.length;
				System.out.println("broj ociju:" + brojLica);
				for (int i = 0; i < eyesArray.length; i++) {
					Imgproc.rectangle(rgb, eyesArray[i].tl(), eyesArray[i].br(), new Scalar(0, 255, 0), 3);
					// ovdje stvaram pravokutnik koji mi je ROI(region of interest)
					// da bi mogao odrezat sve sto nije lice kasnije
					rectCrop = new Rect(eyesArray[i].tl(), eyesArray[i].br());
				}
				Image slikaZaVan = mat2Image(rgb);
				mjestoZaSliku.setImage(slikaZaVan);
			}
		} else {
			System.out.println("Slika nije ucitana!");
		}
	}
	
	@FXML
	private void odreziOci() {
		
	}
}
