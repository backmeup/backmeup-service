package org.backmeup.moodle;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.connectors.FilesystemLikeDatasource;
import org.backmeup.plugin.api.connectors.FilesystemURI;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;


/**
 * This class processes the xml file generated by the server side moodle
 * plugin and downloads the content
 * 
 * @author florianjungwirth
 *
 */
public class MoodleDatasource extends FilesystemLikeDatasource {
	
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); 

	@Override
	public String getStatistics(Properties items) {
		return null;
	}

	@Override
	public InputStream getFile(Properties items, FilesystemURI uri) {
		try {
			return uri.getUri().toURL().openStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<FilesystemURI> list(Properties items, FilesystemURI uri) {

		List<FilesystemURI> results = new ArrayList<FilesystemURI>();

		String serverurl = items.getProperty("Moodle Server Url");
		String username = items.getProperty("Username");
		String password = items.getProperty("Password");

		serverurl = serverurl.endsWith("/") ? serverurl : serverurl+"/";
		
		try {
			String authUrl = serverurl
					+ "blocks/backmeup/service.php?username=" + username
					+ "&password=" + password + "&action=list";
			
			Document doc = new SAXBuilder().build( authUrl );
			
			List<Element> courses = doc.getRootElement().getChild("courses").getChildren("course");

			Iterator<Element> courseIterator = courses.iterator();
			while(courseIterator.hasNext()) {
				Element course = courseIterator.next();
				List<Element> sections = course.getChildren("section");
				Iterator<Element> sectionIterator = sections.iterator();
				
				Metainfo courseMeta = new Metainfo();
				courseMeta.setId(course.getAttributeValue("id"));
				courseMeta.setAttribute("name", course.getAttributeValue("name"));
				courseMeta.setType("course");
				courseMeta.setBackupDate(new Date());

				while(sectionIterator.hasNext()) {
					Element section = sectionIterator.next();
					List<Element> sequences = section.getChildren("sequence");
					Iterator<Element> sequenceIterator = sequences.iterator();
					
					Metainfo sectionMeta = new Metainfo();
					sectionMeta.setParent(courseMeta.getId());
					sectionMeta.setId(courseMeta.getId()+"_"+section.getAttributeValue("id"));
					sectionMeta.setType("section");
					sectionMeta.setAttribute("name", section.getAttributeValue("name"));
					if(section.getAttributeValue("summary").length() > 0)
						sectionMeta.setAttribute("summary", section.getAttributeValue("summary"));

					while(sequenceIterator.hasNext()) {
						Element sequence = sequenceIterator.next();
						Metainfo sequenceMeta = new Metainfo();
						sequenceMeta.setParent(sectionMeta.getId());
						sequenceMeta.setId(sectionMeta.getId()+"_"+sequence.getAttributeValue("id"));
						sequenceMeta.setAttribute("name", sequence.getChildText("name"));
						sequenceMeta.setAttribute("intro", sequence.getChildText("intro").replaceAll("\\<.*?\\>", ""));
						sequenceMeta.setType(sequence.getChildText("type"));

						Element data = sequence.getChild("data");
						if(data != null) {
							List<Element> files = data.getChildren("file");
							Iterator<Element> fileIterator = files.iterator();
							while(fileIterator.hasNext()) {
								Element file = fileIterator.next();
								String mappedPath = (file.getAttribute("path") != null) ? course.getAttributeValue("name")+"/"+file.getAttributeValue("path")+"/" : course.getAttributeValue("name")+"/";
								String mappedUrl = java.net.URLEncoder.encode(mappedPath, "UTF-8").replace("+", "%20")+new File(file.getText()).getName();
								FilesystemURI filesystemUri = new FilesystemURI(new URI(file.getText()), false);
								filesystemUri.setMappedUri(new URI(mappedUrl));
								
								Metainfo fileMeta = new Metainfo();
								fileMeta.setParent(sequenceMeta.getId());
								fileMeta.setModified(formatter.parse(file.getAttributeValue("modified")));
								fileMeta.setBackupDate(new Date());
								fileMeta.setDestination(mappedPath);
								fileMeta.setSource("moodle");
								fileMeta.setType(file.getAttributeValue("mime"));
								fileMeta.setAttribute("name", new File(file.getText()).getName().replace("%20", " "));
								
								filesystemUri.addMetainfo(courseMeta);
								filesystemUri.addMetainfo(sectionMeta);
								filesystemUri.addMetainfo(sequenceMeta);
								filesystemUri.addMetainfo(fileMeta);

								results.add(filesystemUri);
							}
						}
					}
				}
			}
			// add scorm files
			List<Element> scormfiles = doc.getRootElement().getChild("scorm").getChildren("file");
			
			Iterator<Element> scormIterator = scormfiles.iterator();
			while(scormIterator.hasNext()) {
				Element file = scormIterator.next();
				FilesystemURI filesystemUri = new FilesystemURI(new URI(file.getText()), false);
				filesystemUri.setMappedUri(new URI(new File(file.getText()).getName()));
				results.add(filesystemUri);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}
}