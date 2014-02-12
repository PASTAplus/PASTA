package edu.lternet.pasta.client;

import java.util.ArrayList;

import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.EMLParser;


public class RecentUpload {
	
	enum Service { INSERT, UPDATE };
	
	
	// Class fields
	
	
	// Instance fields	
	String uploadDate;
	Service service;
	String title;
	String url;
	String scope;
	String identifier;
	String revision;
	
	
	// Constructors
	
	public RecentUpload(DataPackageManagerClient dpmClient, String uploadDate, String serviceMethod, String url) {
		this.uploadDate = uploadDate;

		if (serviceMethod.equals("createDataPackage")) {
			this.service = Service.INSERT;
		}
		else if (serviceMethod.equals("updateDataPackage")) {
			this.service = Service.UPDATE;
		}

		this.url = url;
		this.scope = parseScope(url);
		this.identifier = parseIdentifier(url);
		this.revision = parseRevision(url);
		this.title = parseTitle(dpmClient);
	}
	
	
	private String parseTitle(DataPackageManagerClient dpmClient) {
		String title = "";
		Integer id = new Integer(this.identifier);

		try {
			String eml = dpmClient.readMetadata(scope, id, revision);
			EMLParser emlParser = new EMLParser();
			DataPackage dataPackage = emlParser.parseDocument(eml);
			ArrayList<String> titleList = dataPackage.getTitles();
			if (titleList.size() > 0) {
				title = titleList.get(0);
			}
		}
		catch (Exception e) {

		}

		return title;
	}


	// Class methods
	
	
	// Instance methods

	public String getUploadDate() {
		return uploadDate;
	}


	public Service getService() {
		return service;
	}


	public String getTitle() {
		return title;
	}

	
	public String getUrl() {
		return url;
	}
	
	
	public String getScope() {
		return scope;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	
	private String parseScope(String url) {
		String[] tokens = url.split("/");
		String scope = "";
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("package") && tokens[i+1].equals("eml")) {
				scope = tokens[i+2];
				break;
			}
		}
		
		return scope;
	}
	
	
	private String parseIdentifier(String url) {
		String[] tokens = url.split("/");
		String identifier = "";
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("package") && tokens[i+1].equals("eml")) {
				identifier = tokens[i+3];
				break;
			}
		}
		
		return identifier;
	}
	
	private String parseRevision(String url) {
		String[] tokens = url.split("/");
		String revision = "";
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("package") && tokens[i+1].equals("eml")) {
				revision = tokens[i+4];
				break;
			}
		}
		
		return revision;
	}
	
}
