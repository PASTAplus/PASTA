package edu.lternet.pasta.datapackagemanager.dc;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.filefilter.DirectoryFileFilter;

import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.ucsb.nceas.utilities.Options;


	/**
	 * This class performs batch generation of Dublin Core metadata
	 * documents from existing Level-1 EML documents. Typically, this
	 * is used when we want to bootstrap a PASTA repository with
	 * Dublin Core metadata corresponding to the set of existing
	 * EML metadata in the repository.
	 * 
	 * @author dcosta
	 *
	 */
public class BatchDublinCore {

	/*
	 * Class fields
	 */
	
	private static final String dirPath = "WebRoot/WEB-INF/conf";

	public void transformAllPastaMetadata() {
		DublinCore dublinCore = new DublinCore();
		ConfigurationListener configurationListener = new ConfigurationListener();
		configurationListener.initialize(dirPath);
		Options options = ConfigurationListener.getOptions();
		String xslDir = options.getOption("datapackagemanager.xslDir");
		String metadataDir = options.getOption("datapackagemanager.metadataDir");
		ArrayList<String> metadataDirs = getMetadataDirs(metadataDir);

		for (String metadataChildDir : metadataDirs) {
			dublinCore.transformMetadata(xslDir, metadataChildDir);
		}
	}
	
	
	/*
	 * Gets a list of harvest report directory names for the specified user id.
	 */
	private ArrayList<String> getMetadataDirs(String metadataDir) {
		ArrayList<String> metadataDirs = new ArrayList<String>();

		if (metadataDir != null) {
			File dir = new File(metadataDir);
			String[] fileNames = dir.list(DirectoryFileFilter.INSTANCE);
			if (fileNames != null) {
				for (int i = 0; i < fileNames.length; i++) {
					String childPath = String.format("%s/%s", metadataDir, fileNames[i]);
					File childFile = new File(childPath);
					if (childFile.isDirectory()) {
						metadataDirs.add(childPath);
					}
				}
			}
		}

		return metadataDirs;
	}

	
	public static void main(String[] args) {
		BatchDublinCore batchDublinCore = new BatchDublinCore();
		batchDublinCore.transformAllPastaMetadata();
	}

}
