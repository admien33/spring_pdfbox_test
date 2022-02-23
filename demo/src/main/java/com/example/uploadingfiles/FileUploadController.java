package com.example.uploadingfiles;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.uploadingfiles.pdf.PDFBox;
import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

@Controller
public class FileUploadController {

	private final StorageService storageService;
	private PDFBox pdfBox = new PDFBox();

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files",
				storageService.loadAll()
						.map(path -> MvcUriComponentsBuilder
								.fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
								.build().toUri().toString())
						.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

		storageService.store(file);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");

		return "redirect:/";
	}

	@PostMapping("/pdf-nb-pages")
	public ResponseEntity<Object> handleFileUploadPdfNbPages(@RequestParam("file") MultipartFile file) {

		try {
			int nbPages = pdfBox.getNumberOfPagesPdf(file.getInputStream());
			return ResponseEntity.ok(Collections.singletonMap("nbPages", nbPages));
		} catch (IOException e) {
			return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/pdf-page-to-image")
	@ResponseBody
	public ResponseEntity<Object> handleFileUploadPdfPageToImages(@RequestParam("file") MultipartFile file,
			@RequestParam("page") int page) {

		try {
			BufferedImage img = pdfBox.convertPdfPageToImage(file.getInputStream(), page);
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			ImageIO.write(img, "png", bao);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "page_"+ page +".png" + "\"")
					.header(HttpHeaders.CONTENT_TYPE, "image/png").body(bao.toByteArray());
		} catch (IOException e) {
			return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping("/pdf-validation-a-2b")
	public ResponseEntity<Object> handleFileUploadPdfValidationPdfa(@RequestParam("file") MultipartFile file) {

		try {
			Boolean result = pdfBox.validationPdfA2b(file.getInputStream());
			return ResponseEntity.ok(Collections.singletonMap("status", result));
		} catch (IOException e) {
			return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
		}
	}

//	@RequestMapping(value = "/pdf-page-to-image-bis", method = RequestMethod.POST, produces = "image/png")
//	public @ResponseBody byte[] handleFileUploadPdfPageToImage(@RequestParam("file") MultipartFile file,
//			@RequestParam("page") int page) {
//		try {
//			BufferedImage img = pdfBox.convertPdfPageToImage(file.getInputStream(), page);
//			ByteArrayOutputStream bao = new ByteArrayOutputStream();
//			ImageIO.write(img, "png", bao);
//			return bao.toByteArray();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
