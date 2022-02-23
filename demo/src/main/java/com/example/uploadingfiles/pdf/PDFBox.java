package com.example.uploadingfiles.pdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Stack;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.PDFToImage;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

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

}
