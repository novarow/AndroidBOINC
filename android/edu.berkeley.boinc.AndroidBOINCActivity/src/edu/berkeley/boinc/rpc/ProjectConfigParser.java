/* 
 * NativeBOINC - Native BOINC Client with Manager
 * Copyright (C) 2011, Mateusz Szpakowski
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package edu.berkeley.boinc.rpc;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.berkeley.boinc.debug.Logging;
import android.util.Log;
import android.util.Xml;

/**
 * @author mat
 *
 */
public class ProjectConfigParser extends BaseParser {
	private static final String TAG = "ProjectConfigParser";
	
	private ProjectConfig mProjectConfig = null;
	private boolean mInsidePlatforms = false;
	
	public ProjectConfig getProjectConfig() {
		return mProjectConfig;
	}
	
	public static ProjectConfig parse(String rpcResult) {
		try {
			String outResult;
			int xmlHeaderStart = rpcResult.indexOf("<?xml");
			if (xmlHeaderStart!=-1) { // remove xml header inside body
				int xmlHeaderEnd = rpcResult.indexOf("?>");
				outResult = rpcResult.substring(0, xmlHeaderStart);
				outResult += rpcResult.substring(xmlHeaderEnd+2);
			} else
				outResult = rpcResult;
				
			ProjectConfigParser parser = new ProjectConfigParser();
			Xml.parse(outResult, parser);
			return parser.getProjectConfig();
		}
		catch (SAXException e) {
			if (Logging.DEBUG) Log.d(TAG, "Malformed XML:\n" + rpcResult);
			else if (Logging.INFO) Log.i(TAG, "Malformed XML");
			return null;
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase("project_config")) {
			if (Logging.INFO) {
				if (mProjectConfig != null) {
					// previous <project_config> not closed - dropping it!
					Log.i(TAG, "Dropping unfinished <project_config> data");			
				}
			}
			mProjectConfig = new ProjectConfig();
		} else if (localName.equalsIgnoreCase("platforms")) {
			mInsidePlatforms = true;
		} else {
			// Another element, hopefully primitive and not constructor
			// (although unknown constructor does not hurt, because there will be primitive start anyway)
			mElementStarted = true;
			mCurrentElement.setLength(0);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		try {
			if (mProjectConfig != null) {
				if (localName.equalsIgnoreCase("project_config")) {
					// Closing tag of <project_config> - nothing to do at the moment
				} else if (localName.equalsIgnoreCase("platforms")) {
					mInsidePlatforms = false;
				}
				else {
					// Not the closing tag - we decode possible inner tags
					trimEnd();
					if (localName.equalsIgnoreCase("error_num")) {
						mProjectConfig.error_num = Integer.parseInt(mCurrentElement.toString());
					} else if (localName.equalsIgnoreCase("error_msg")) {
						mProjectConfig.error_msg = mCurrentElement.toString();
					} else if (localName.equalsIgnoreCase("name")) {
						mProjectConfig.name = mCurrentElement.toString();
					} else if (localName.equalsIgnoreCase("platform_name") && mInsidePlatforms) {
						mProjectConfig.platforms.add(mCurrentElement.toString());
					} else if (localName.equalsIgnoreCase("master_url")) {
						mProjectConfig.master_url = mCurrentElement.toString();
					} else if (localName.equalsIgnoreCase("local_revision")) {
						mProjectConfig.local_revision = Integer.parseInt(mCurrentElement.toString());
					} else if (localName.equalsIgnoreCase("min_passwd_length")) {
						mProjectConfig.min_passwd_length = Integer.parseInt(mCurrentElement.toString());
					} else if (localName.equalsIgnoreCase("account_manager")) {
						mProjectConfig.account_manager = Integer.parseInt(mCurrentElement.toString())!=0;
					} else if (localName.equalsIgnoreCase("use_username")) {
						mProjectConfig.use_username = Integer.parseInt(mCurrentElement.toString())!=0;
					} else if (localName.equalsIgnoreCase("account_creation_disabled")) {
						mProjectConfig.account_creation_disabled = true;
					} else if (localName.equalsIgnoreCase("client_account_creation_disabled")) {
						mProjectConfig.client_account_creation_disabled = true;
					} else if (localName.equalsIgnoreCase("sched_stopped")) {
						mProjectConfig.sched_stopped = Integer.parseInt(mCurrentElement.toString())!=0;
					} else if (localName.equalsIgnoreCase("web_stopped")) {
						mProjectConfig.web_stopped = Integer.parseInt(mCurrentElement.toString())!=0;
					} else if (localName.equalsIgnoreCase("min_client_version")) {
						mProjectConfig.min_client_version = Integer.parseInt(mCurrentElement.toString());
					} else if (localName.equalsIgnoreCase("terms_of_use")) {
						mProjectConfig.terms_of_use = mCurrentElement.toString();
					}
				}
			}
		} catch (NumberFormatException e) {
			if (Logging.INFO) Log.i(TAG, "Exception when decoding " + localName);
		}
		mElementStarted = false;
	}
}
