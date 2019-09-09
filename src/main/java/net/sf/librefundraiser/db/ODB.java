package net.sf.librefundraiser.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jopendocument.dom.OOUtils;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.Util;

public class ODB {
	private static final String beforeFilename = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<office:document-content xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\" xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\" xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\" xmlns:draw=\"urn:oasis:names:tc:opendocument:xmlns:drawing:1.0\" xmlns:fo=\"urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:meta=\"urn:oasis:names:tc:opendocument:xmlns:meta:1.0\" xmlns:number=\"urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0\" xmlns:svg=\"urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0\" xmlns:chart=\"urn:oasis:names:tc:opendocument:xmlns:chart:1.0\" xmlns:dr3d=\"urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns:form=\"urn:oasis:names:tc:opendocument:xmlns:form:1.0\" xmlns:script=\"urn:oasis:names:tc:opendocument:xmlns:script:1.0\" xmlns:ooo=\"http://openoffice.org/2004/office\" xmlns:ooow=\"http://openoffice.org/2004/writer\" xmlns:oooc=\"http://openoffice.org/2004/calc\" xmlns:dom=\"http://www.w3.org/2001/xml-events\" xmlns:db=\"urn:oasis:names:tc:opendocument:xmlns:database:1.0\" xmlns:xforms=\"http://www.w3.org/2002/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:rpt=\"http://openoffice.org/2005/report\" xmlns:of=\"urn:oasis:names:tc:opendocument:xmlns:of:1.2\" xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" xmlns:grddl=\"http://www.w3.org/2003/g/data-view#\" xmlns:tableooo=\"http://openoffice.org/2009/table\" xmlns:calcext=\"urn:org:documentfoundation:names:experimental:calc:xmlns:calcext:1.0\" xmlns:field=\"urn:openoffice:names:experimental:ooo-ms-interop:xmlns:field:1.0\" xmlns:formx=\"urn:openoffice:names:experimental:ooxml-odf-interop:xmlns:form:1.0\" xmlns:css3t=\"http://www.w3.org/TR/css3-text/\" office:version=\"1.2\"><office:scripts/><office:font-face-decls/><office:automatic-styles/><office:body><office:database><db:data-source><db:connection-data><db:database-description><db:file-based-database xlink:href=\"";
	private static final String afterFilename = "\" db:media-type=\"application/vnd.oasis.opendocument.spreadsheet\"/></db:database-description><db:login db:is-password-required=\"false\"/></db:connection-data><db:driver-settings db:system-driver-settings=\"\" db:base-dn=\"\" db:parameter-name-substitution=\"false\"/><db:application-connection-settings db:is-table-name-length-limited=\"false\" db:append-table-alias-name=\"false\" db:max-row-count=\"100\"><db:table-filter><db:table-include-filter><db:table-filter-pattern>%</db:table-filter-pattern></db:table-include-filter></db:table-filter></db:application-connection-settings></db:data-source></office:database></office:body></office:document-content>";
	private static final String[] templateFiles = new String[]{"mimetype", "settings.xml", "META-INF/manifest.xml"};
	public static void exportToODB(File f, boolean register) {
		try {
			final File odsFile = new File (f.getParent() + "/" + convertFilename(f));
			final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
			ZipEntry e = new ZipEntry("content.xml");
			out.putNextEntry(e);
			byte[] data = (beforeFilename + "../" + odsFile.getName() + "/" + afterFilename).getBytes("UTF-8");
			out.write(data, 0, data.length);
			out.closeEntry();
			for (String filename : templateFiles) {
				ZipEntry entry = new ZipEntry(filename);
				out.putNextEntry(entry);
				dumpFile(out, ODB.class.getResourceAsStream("/net/sf/librefundraiser/odbtemplate/"+filename));
				out.closeEntry();
			}
			out.close();
			Util.writeODS(Main.getWindow().getDonorTable().donors, odsFile, false);
			if (register) registerDB(f);
			OOUtils.open(f);
		} catch (IOException e) {
			throw new RuntimeException("Error with ODB export", e);
		}
	}
	private static String convertFilename(File f) {
		String originalFilename = f.getName();
		String[] originalParts = originalFilename.split("\\.");
		StringBuilder newFilename = new StringBuilder();
		boolean first = true;
		for (String part : originalParts) {
			if (part.equals("odb")) {
				part = "ods";
			}
			if (!first) {
				newFilename.append(".");
			} else {
				first = false;
			}
			newFilename.append(part);
		}
		return newFilename.toString();
	}
	private static void dumpFile(OutputStream o, InputStream i) throws IOException {
		final int bufferSizeKB = 100;
		byte[] buffer = new byte[bufferSizeKB*1024];
		int len;
		while ((len = i.read(buffer)) != -1) {
		    o.write(buffer, 0, len);
		}
	}
	private static boolean registerDB(File f) throws IOException {
		//TODO: make this work on Windows
		File registryModifs = new File(System.getProperty("user.home")+"/.config/libreoffice/3/user/registrymodifications.xcu");
		if (!registryModifs.exists()) return false;
		Runtime.getRuntime().exec("killall -9 soffice.bin");
		StringBuilder fileContents = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(registryModifs));
		final String newLineTemplate = "<item oor:path=\"/org.openoffice.Office.DataAccess/RegisteredNames\"><node oor:name=\"org.openoffice.%s\" oor:op=\"replace\"><prop oor:name=\"Location\" oor:op=\"fuse\"><value>file://%s</value></prop><prop oor:name=\"Name\" oor:op=\"fuse\"><value>%s</value></prop></node></item>";
		String newLine = String.format(newLineTemplate, f.getName(), f.getAbsolutePath(), f.getName());
		String line;
		boolean relevant = false;
		while (reader.ready()) {
			line = reader.readLine();
			if (line.contains("<item oor:path=\"/org.openoffice.Office.DataAccess/RegisteredNames\">")) {
				relevant = true;
			} else if (relevant) {
				fileContents.append(newLine);
				fileContents.append("\n");
				relevant = false;
			}
			fileContents.append(line);
			fileContents.append("\n");
		}
		reader.close();
		String entries = fileContents.toString();
		FileWriter writer = new FileWriter(registryModifs);
		writer.write(entries);
		writer.close();
		return true;
	}
}
