package com.example.uploadingfiles.pdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.PDFToImage;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;

public class PDFBox {

	public PDFBox() {
	}

	public void createBlank(final Path filename) throws IOException {
		PDDocument doc = new PDDocument();
		System.out.println("save doc, " + filename);
		doc.save("C:\\dev\\blank.pdf");
		System.out.println("PDF created");
		doc.close();

	}

	public int getNumberOfPagesPdf(InputStream input) throws IOException {
		PDDocument doc = PDDocument.load(input);
		int numberOfPages = doc.getNumberOfPages();
		doc.close();
		return numberOfPages;

	}

	public BufferedImage convertPdfPageToImage(InputStream input, int page) throws IOException {
		PDDocument doc = PDDocument.load(input);
		int numberOfPages = doc.getNumberOfPages();
		if (page < 0 || page + 1 > numberOfPages) {
			doc.close();
			throw new RuntimeException("error param page, outscope pdf file"); // TODO
		}
		PDFRenderer pdfRenderer = new PDFRenderer(doc);
		BufferedImage img = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
		doc.close();
		return img;

	}

	public boolean validationPdfA2b(InputStream input) throws IOException {
		PDFAFlavour flavour = PDFAFlavour.fromString("2b");
		try (PDFAParser parser = Foundries.defaultInstance().createParser(input, flavour)) {
			PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false);
			ValidationResult result = validator.validate(parser);
			return result.isCompliant();
		} catch (IOException | ValidationException | ModelParsingException | EncryptedPdfException exception) {
			throw new RuntimeException("error validation pdfa"); // TODO
		}

	}

}
