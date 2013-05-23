package edu.lternet.pasta.datapackagemanager.checksum;

import java.io.File;
import java.util.ArrayList;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataManagerClient;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageMetadata;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;
import edu.lternet.pasta.datapackagemanager.DataPackageReport;
import edu.lternet.pasta.doi.Resource;


public class ChecksumBackfiller {
	
	private static final String dirPath = "WebRoot/WEB-INF/conf";
	
	public static void main(String[] args) {
		int checksumCount = 0;
		try {
		    ConfigurationListener configurationListener = new ConfigurationListener();
		    configurationListener.initialize(dirPath);
			DataPackageManager dpm = new DataPackageManager();
			DataPackageRegistry dpr = DataPackageManager.makeDataPackageRegistry();
			ArrayList<Resource> resources = dpr.listChecksumlessResources();
			
			for (Resource resource: resources) {
				String resourceId = resource.getResourceId();
				String resourceType = resource.getResourceType();
				String scope = resource.getScope();
				Integer identifier = resource.getIdentifier();
				Integer revision = resource.getRevision();
				EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
				EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope,
				    identifier.toString(), revision.toString());
				
				if (resourceType != null) {
					if (resourceType.equals("data")) {
						// Store the checksum of the data entity resource
						String entityId = resource.getEntityId();
						DataManagerClient dataManagerClient = new DataManagerClient();
						String resourceLocation = dpr.getResourceLocation(resourceId);
						File file = dataManagerClient.getDataEntityFile(resourceLocation, scope,
						    identifier, revision.toString(), entityId);
						dpm.storeChecksum(resourceId, file);
						checksumCount++;
					}
					else if (resourceType.equals("metadata")) {
						DataPackageMetadata dataPackageMetadata = new DataPackageMetadata(emlPackageId);
						if (dataPackageMetadata != null) {
							boolean evaluateMode = false;
							File file = dataPackageMetadata.getMetadata(evaluateMode);
							dpm.storeChecksum(resourceId, file);
							checksumCount++;
						}
					}
					else if (resourceType.equals("report")) {
						DataPackageReport dataPackageReport = new DataPackageReport(emlPackageId);
						boolean evaluate = false;
						String transaction = null;
						if (dataPackageReport != null) {
							File file = dataPackageReport.getReport(evaluate, transaction);
							dpm.storeChecksum(resourceId, file);
							checksumCount++;
						}
					}
					else {
						System.err.println("Unknown resource type: " + resourceType);
					}
				}
				
			}		
		}
		catch (Exception e) {
			System.err.println("Exception constructing DataPackageManager object: " + e.getMessage());
		}
		
		System.err.println(String.format("Finished processing. Checksums generated for %d resources.", checksumCount));
	}

}
