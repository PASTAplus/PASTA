/*
 *
 * Copyright 2011, 2012, 2013 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative
 * Agreements #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */

package edu.lternet.pasta.common.eml;

import org.apache.log4j.Logger;

/**
 * @author servilla
 * @since Nov 5, 2012
 *
 */
public class Title {
	
	// Optional title type.
	enum TitleType {
		MainTitle,
		AlternativeTitle,
		Subtitle,
		TranslatedTitle
	};
	
	/*
	 * Class variables
	 */
	
	public static final String MAIN = "MainTitle";
	public static final String ALT = "AlternativeTitle";
	public static final String SUB = "Subtitle";
	public static final String TRANS = "TranslatedTitle";
	
	/*
	 * Instance variables
	 */

	private Logger logger = Logger.getLogger(Title.class);

	private String title = null;
	private TitleType titleType = null;
	
	/*
	 * Constructors
	 */
	
	/*
	 * Class methods
	 */
	
	/*
	 * Instance methods
	 */
	
	public void setTitleType(String titleType) throws Exception {
		
		if (titleType.equals(Title.MAIN) || titleType.equals(Title.ALT) || titleType.equals(Title.SUB) || titleType.equals(Title.TRANS)) {
			this.titleType = (TitleType) Enum.valueOf(TitleType.class, titleType);
		} else {
			String gripe = "Operation not supported for this \"title type\": ";
			throw new Exception(gripe);
		}
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitleType() {
		return this.titleType.toString();
	}
	
	public String getTitle() {
		return this.title;
	}
	
}
