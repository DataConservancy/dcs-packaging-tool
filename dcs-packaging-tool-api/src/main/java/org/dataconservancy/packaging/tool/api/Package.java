/*
 * Copyright 2013 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.packaging.tool.api;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Serialized data package.
 * <p>
 * This is the output of {@link PackageGenerator}. It containes access to the
 * serialized bytes of the package, along with some basic file metadata. {@link #cleanupPackage()} should be called
 * once the package is no longer in use.
 * </p>
 * 
 */
public interface Package {
    
	/** Return bytes of serialized package 
	 * 
	 *  @throws FileNotFoundException 
	 */
	public InputStream serialize() throws FileNotFoundException;

	/**
	 * Suggested filename (excluding path)
	 */
	public String getPackageName();

	/**
	 * MIME type of the resulting package serialization
	 */
	public String getContentType();
	
	/**
	 * Cleans up any resources associated with the package. 
	 */
	public void cleanupPackage();
	
	/**
	 * Determines if the content of the package is still available.
	 * @return True if the content is available, false otherwise.
	 */
	public boolean isAvailable();

}
