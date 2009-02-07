package org.jbehave.web.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public class FileZipper {

	private byte[] BUFFER = new byte[1024];

	public void zip(File archive, List<File> files) {
		try {
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
					archive));
			// Compress the files
			for (File file : files) {
				FileInputStream in = new FileInputStream(file);

				// Add entry to output stream.
				out.putNextEntry(new ZipEntry(file.getAbsolutePath()));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(BUFFER)) > 0) {
					out.write(BUFFER, 0, len);
				}

				// close entry
				out.closeEntry();
				in.close();
			}
			// close stream
			out.close();
		} catch (Exception e) {
			throw new FileZipFailedException(archive, files);
		}
	}

	public void unzip(File archive, File outputDir) {
		try {
			ZipFile zipfile = new ZipFile(archive);
			for (Enumeration<?> e = zipfile.entries(); e.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				unzipEntry(zipfile, entry, outputDir);
			}
		} catch (Exception e) {
			throw new FileUnzipFailedException(archive, outputDir);
		}
	}

	private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir)
			throws IOException {

		if (entry.isDirectory()) {
			createDir(new File(outputDir, entry.getName()));
			return;
		}

		File outputFile = new File(outputDir, entry.getName());
		if (!outputFile.getParentFile().exists()) {
			createDir(outputFile.getParentFile());
		}

		BufferedInputStream inputStream = new BufferedInputStream(zipfile
				.getInputStream(entry));
		BufferedOutputStream outputStream = new BufferedOutputStream(
				new FileOutputStream(outputFile));

		try {
			IOUtils.copy(inputStream, outputStream);
		} finally {
			outputStream.close();
			inputStream.close();
		}
	}

	private void createDir(File dir) throws IOException {
		if (!dir.mkdirs()) {
			throw new IOException("Failed to create dir " + dir);
		}
	}
	

	@SuppressWarnings("serial")
	public static final class FileZipFailedException extends RuntimeException {

		public FileZipFailedException(File archive, List<File> files) {
			super(files.toString() + File.separator + files.toString());
		}

	}

	@SuppressWarnings("serial")
	public static final class FileUnzipFailedException extends RuntimeException {

		public FileUnzipFailedException(File archive, File outputDir) {
			super(outputDir.toString() + File.separator + archive.toString());
		}

	}

}