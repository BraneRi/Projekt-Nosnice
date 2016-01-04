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
import org.opencv.core.Point;
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
	private Button detNosGumb;
	@FXML
	private Button odrNosGumb;
	@FXML
	private Button detOciGumb;
	@FXML
	private Button odrOciGumb;
	@FXML
	private Button smanjiRezGumb;
	@FXML
	private Button cannyGumb;
	/**
	 * matrica slike koja je prikazana - stalno se mijenja
	 */
	private Mat rgb;
	private int brojLica = 0;
	private int brojParovaOciju = 0;
	private int brojNoseva = 0;
	private Rect rectCropLice = null;
	private Rect rectCropOci = null;
	private Rect rectCropNos = null;
	private int donjiPrag = 140;
	private int gornjiPrag = 200;

	@FXML
	private void ucitajSliku() {
		// prvo podesavam sirinu i visinu mejsta za sliku zbog rezloucije
		mjestoZaSliku.setFitWidth(640);
		mjestoZaSliku.setFitHeight(480);

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
	private void smanjiRezoluciju() {
		Imgproc.resize(rgb, rgb, new Size(rgb.width() * 0.3, rgb.height() * 0.3));
		Image slikaZaVan = mat2Image(rgb);
		mjestoZaSliku.setImage(slikaZaVan);
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
						new Size(0.2 * mjestoZaSliku.getFitWidth(), 0.2 * mjestoZaSliku.getFitHeight()), new Size());

				// each rectangle in faces is a face: draw them!
				Rect[] facesArray = faces.toArray();
				brojLica = facesArray.length;
				System.out.println("broj lica:" + brojLica);
				for (int i = 0; i < facesArray.length; i++) {
					Imgproc.rectangle(rgb, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
					// ovdje stvaram pravokutnik koji mi je ROI(region of
					// interest)
					// da bi mogao odrezat sve sto nije lice kasnije
					rectCropLice = new Rect(facesArray[i].tl(), facesArray[i].br());
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
			if (rectCropLice == null) {
				System.out.println("Greska kod pravokutnika koji oznacava ROI");
			} else {
				rgb = new Mat(rgb, rectCropLice);
				Image slikaZaVan = mat2Image(rgb);
				mjestoZaSliku.setImage(slikaZaVan);
			}
		}
	}

	/**
	 * metoda radi na isti nacin kao i <code>detektriajLice</code> jedina
	 * razlika je sto ovdje primjenjujem algoritam za nos
	 */
	@FXML
	private void detektirajNos() {
		if (rgb != null) {
			MatOfRect noses = new MatOfRect();
			Mat grayFrame = new Mat();
			// face cascade classifier
			CascadeClassifier noseCascade = new CascadeClassifier("C:/BraneProjekt/FaceFeaturesDetectors/Nariz.xml");
			// convert the frame in gray scale
			Imgproc.cvtColor(rgb, grayFrame, Imgproc.COLOR_BGR2GRAY);
			// equalize the frame histogram to improve the result
			Imgproc.equalizeHist(grayFrame, grayFrame);

			if (!grayFrame.empty()) {
				// detect mouths
				noseCascade.detectMultiScale(grayFrame, noses);

				// each rectangle in noses is nose
				Rect[] nosesArray = noses.toArray();
				brojNoseva = nosesArray.length;
				System.out.println("broj noseva:" + brojNoseva);
				// ukoliko mi algoritam pronadje vise "noseva" uzet cu
				// pravokutnik kojemu je pozicija najbliza sredini slike
				Rect privremeni;
				Point srediste;
				Point sredisteSlike = new Point(
						mjestoZaSliku.getX() + (mjestoZaSliku.getFitWidth() + mjestoZaSliku.getX()) / 2,
						mjestoZaSliku.getY() + (mjestoZaSliku.getFitHeight() + mjestoZaSliku.getY()) / 2);
				double udaljenost;
				// najmanjau udaljenost sam postavio na sirinu slike, jer ce sve ostale sigurno biti manje
				double minUdaljenost = mjestoZaSliku.getFitWidth();
				for (int i = 0; i < nosesArray.length; i++) {
					Imgproc.rectangle(rgb, nosesArray[i].tl(), nosesArray[i].br(), new Scalar(0, 255, 0), 3);
					privremeni = new Rect(nosesArray[i].tl(), nosesArray[i].br());
					srediste = new Point((privremeni.x + (privremeni.width + privremeni.x)) / 2,
							(privremeni.y + (privremeni.height + privremeni.y)) / 2);
					udaljenost = Math.sqrt(Math.pow((srediste.x - sredisteSlike.x), 2) + Math.pow((srediste.y - sredisteSlike.y), 2));
					// ispitujem je li udaljenost trenutnog "nosa" najmanja u odnosu na srediste slike
					if (udaljenost < minUdaljenost) {
						minUdaljenost = udaljenost;
						rectCropNos = privremeni;
					}
				}
				Image slikaZaVan = mat2Image(rgb);
				mjestoZaSliku.setImage(slikaZaVan);
			}
		} else {
			System.out.println("Slika nije ucitana!");
		}
	}

	@FXML
	private void odreziNos() {
		// ne moram provjeravati je li jedan nos jer sam izabrao najvecu
		// povrsinu
		// koja predstavlja nos
		if (rectCropNos == null) {
			System.out.println("Greska kod pravokutnika koji oznacava ROI");
		} else {
			rgb = new Mat(rgb, rectCropNos);
			Image slikaZaVan = mat2Image(rgb);
			mjestoZaSliku.setImage(slikaZaVan);
		}

	}

	/**
	 * metoda radi na isti nacin kao i <code>detektriajLice</code> jedina
	 * razlika je sto ovdje primjenjujem algoritam za oci
	 */
	@FXML
	private void detektirajOci() {
		if (rgb != null) {
			MatOfRect eyes = new MatOfRect();
			Mat grayFrame = new Mat();
			// face cascade classifier
			CascadeClassifier eyeCascade = new CascadeClassifier("C:/BraneProjekt/eyes.xml");
			// convert the frame in gray scale
			Imgproc.cvtColor(rgb, grayFrame, Imgproc.COLOR_BGR2GRAY);
			// equalize the frame histogram to improve the result
			Imgproc.equalizeHist(grayFrame, grayFrame);

			if (!grayFrame.empty()) {
				// detect faces
				eyeCascade.detectMultiScale(grayFrame, eyes, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
						new Size(0.05 * mjestoZaSliku.getFitWidth(), 0.05 * mjestoZaSliku.getFitHeight()), new Size());

				// each rectangle in faces is a face: draw them!
				Rect[] eyesArray = eyes.toArray();
				brojParovaOciju = eyesArray.length;
				System.out.println("broj parova ociju:" + brojParovaOciju);
				for (int i = 0; i < eyesArray.length; i++) {
					Imgproc.rectangle(rgb, eyesArray[i].tl(), eyesArray[i].br(), new Scalar(0, 255, 0), 3);
					// ovdje stvaram pravokutnik koji mi je ROI(region of
					// interest)
					// da bi mogao odrezat sve sto nije lice kasnije
					rectCropOci = new Rect(new Point(eyesArray[i].tl().x, rgb.height()), eyesArray[i].br());
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
		// na fotografiji mora biti jedno lice, ako nije javit ce gresku
		if (brojParovaOciju != 1) {
			System.out.println("Mora biti jedan par ociju na slici!");
		} else {
			if (rectCropOci == null) {
				System.out.println("Greska kod pravokutnika koji oznacava ROI");
			} else {
				rgb = new Mat(rgb, rectCropOci);
				Image slikaZaVan = mat2Image(rgb);
				mjestoZaSliku.setImage(slikaZaVan);
			}
		}
	}

	@FXML
	private void canny() {
		Mat grayFrame = new Mat();
		Imgproc.cvtColor(rgb, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(grayFrame, grayFrame, new Size(3, 3), 1.4);
		Imgproc.Canny(grayFrame, grayFrame, donjiPrag, gornjiPrag, 3, true);
		Image slikaZaVan = mat2Image(grayFrame);
		mjestoZaSliku.setImage(slikaZaVan);
	}
	
	@FXML
	private void vratiRgbSliku() {
		Image slikaZaVan = mat2Image(rgb);
		mjestoZaSliku.setImage(slikaZaVan);
	}
	
	/**
	 * izmjena donjeg threshold parametra za canny
	 */
	@FXML
	private void povecajDonjiPrag() {
		donjiPrag = donjiPrag + 4;
	}
	
	@FXML
	private void smanjiDonjiPrag() {
		donjiPrag = donjiPrag - 4;
	}
	
	/**
	 * izmjena gornjeg threshold parametra za canny
	 */
	@FXML
	private void povecajGornjiPrag() {
		gornjiPrag = gornjiPrag + 4;
	}
	
	@FXML
	private void smanjiGornjiPrag() {
		gornjiPrag = gornjiPrag - 4;
	}

	// TO DO: izdvojit crna podrucja (BLOB), heuristika: nosnice su najblize
	// sredisnjem meridijanu, odrezat pravokutnik oko njih i malo vise
	// potom probat canny - RIBARICEVI SAVJETI

}
