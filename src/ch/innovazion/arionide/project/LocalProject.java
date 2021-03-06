/*******************************************************************************
 * This file is part of Arionide.
 *
 * Arionide is an IDE used to conceive applications and algorithms in a three-dimensional environment. 
 * It is the work of Arion Zimmermann for his final high-school project at Calvin College (Geneva, Switzerland).
 * Copyright (C) 2016-2020 Innovazion. All rights reserved.
 *
 * Arionide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Arionide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Arionide.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The copy of the GNU General Public License can be found in the 'LICENSE.txt' file inside the src directory or inside the JAR archive.
 *******************************************************************************/
package ch.innovazion.arionide.project;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import ch.innovazion.arionide.SystemCache;
import ch.innovazion.arionide.Utils;
import ch.innovazion.arionide.coders.Coder;
import ch.innovazion.arionide.coders.Decoder;
import ch.innovazion.arionide.coders.Encoder;
import ch.innovazion.arionide.debugging.Debug;
import ch.innovazion.arionide.debugging.IAm;
import ch.innovazion.arionide.project.managers.StructureManager;

public class LocalProject implements Project {

	public static final long versionUID = 500L;
	
	private static final Map<String, byte[]> projectProtocolMapping = new LinkedHashMap<>();
	
	static {
		projectProtocolMapping.put("version", Long.toString(versionUID).getBytes(Coder.charset));
		projectProtocolMapping.put("name", new String("Undefined").getBytes(Coder.charset));
		projectProtocolMapping.put("structureGen", Integer.toString(0).getBytes(Coder.charset));
		projectProtocolMapping.put("specificationGen", Integer.toString(0).getBytes(Coder.charset));
		projectProtocolMapping.put("seed", Long.toString(new Random().nextLong()).getBytes(Coder.charset));
		projectProtocolMapping.put("user", new String("0.0|0.0|5.0|0.0|0.0").getBytes(Coder.charset));
	}
	
	private final ZipStorage storage;
	private final StructureManager manager;
	private final Map<String, byte[]> properties = new LinkedHashMap<>();
		
	public LocalProject(File path) {
		this.storage = new ZipStorage(path);
		this.manager = new StructureManager(this);
	}
	
	public void initFS() {
		if(!this.properties.isEmpty()) {
			return;
		}
		
		this.storage.initFS();
		
		Runtime.getRuntime().addShutdownHook(new Thread(this::closeFS));
	}
	
	private void closeFS() {
		try {
			this.save();
			this.storage.closeFS();
		} catch(StorageException exception) {
			Debug.exception(exception);
		}
	}
	
	public void loadMeta() throws StorageException {
		if(!this.properties.isEmpty()) {
			return;
		}
				
		try {
			byte[] data = Files.readAllBytes(this.storage.getMetaPath());
			
			int startIndex = 0;
			int endIndex = 0;
			
			while((endIndex = Utils.search(data, startIndex, data.length, Coder.separator)) > -1) {
				byte[] keyBuffer = new byte[endIndex - startIndex];
				System.arraycopy(data, startIndex, keyBuffer, 0, keyBuffer.length);
								
				startIndex = Utils.search(data, endIndex, data.length, Coder.sectionStart) + 1;
				endIndex = Utils.search(data, startIndex, data.length, Coder.sectionEnd);
				byte[] valueBuffer = new byte[endIndex - startIndex];
				System.arraycopy(data, startIndex, valueBuffer, 0, valueBuffer.length);
				
				this.properties.put(new String(keyBuffer, Coder.charset).replaceAll(Coder.whitespaceRegex, Coder.empty), valueBuffer);
				
				startIndex = endIndex + 1;
			}
						
			this.verifyProtocol();
		} catch (Exception exception) {
			throw new StorageException("Failed to load project metadata", exception);
		}
	}
	
	@IAm("loading a project")
	public void load() throws StorageException {
		try {
			loadMeta();
			
			storage.loadHierarchy();
			storage.loadInheritance();
			storage.loadCallGraph();
			storage.loadStructureMeta();
			storage.loadHistory();
			storage.loadCode();
			storage.loadLanguages();
		} catch (StorageException exception) {
			throw new StorageException("Project loading failed", exception);
		}
	}

	@IAm("saving a project")
	public void save() throws StorageException {		
		try(OutputStream stream = Files.newOutputStream(this.storage.getMetaPath(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

			this.verifyProtocol();
						
			for(Entry<String, byte[]> property : this.properties.entrySet()) {
				stream.write(property.getKey().getBytes(Coder.charset));
				stream.write(Coder.separator);
				stream.write(Coder.space);
				stream.write(Coder.sectionStart);
				
				if(property.getValue().length > 64) {
					stream.write(Coder.windowsNewline);
					stream.write(Coder.newline);
					stream.write(Coder.tab);
					stream.write(property.getValue());
					stream.write(Coder.windowsNewline);
					stream.write(Coder.newline);
					stream.write(Coder.sectionEnd);
				} else {
					stream.write(property.getValue());
					stream.write(Coder.sectionEnd);
				}
				
				stream.write(Coder.windowsNewline);
				stream.write(Coder.newline);
			}
			
			storage.saveHierarchy();
			storage.saveInheritance();
			storage.saveCallGraph();
			storage.saveStructureMeta();
			storage.saveHistory();
			storage.saveCode();
			storage.saveLanguages();
		} catch(IOException exception) {
			throw new StorageException("Project saving failed", exception);
		}
	}
	
	private void verifyProtocol() {
		for(Entry<String, byte[]> entry : LocalProject.projectProtocolMapping.entrySet()) {
			if(!this.properties.containsKey(entry.getKey())) {
				this.properties.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public <T> T getProperty(String key, Decoder<T> decoder) {
		String hash = this.hashCode() + key;
				
		if(SystemCache.has(hash)) {
			return SystemCache.get(hash);
		}

		T decoded = decoder.decode(this.properties.get(key));
		
		SystemCache.set(hash, decoded, SystemCache.NEVER);
		
		return decoded;
	}

	public <T> void setProperty(String key, T value, Encoder<T> encoder) {
		this.properties.put(key, encoder.encode(value));
		SystemCache.set(this.hashCode() + key, value, SystemCache.NEVER);
	}

	public Map<String, byte[]> getProtocolMapping() {
		return LocalProject.projectProtocolMapping;
	}
	
	public String getName() {
		return this.getProperty("name", Coder.stringDecoder);
	}

	public File getPath() {
		return this.storage.getLocation();
	}
	
	public Storage getStorage() {
		return this.storage;
	}

	public StructureManager getStructureManager() {
		return this.manager;
	}
	
	public boolean checkVersionCompatibility() {
		return this.getProperty("version", Coder.integerDecoder) == LocalProject.versionUID;
	}
	
	public boolean equals(Object other) {
		if(other instanceof LocalProject) {
			if(this.getPath().equals(((LocalProject) other).getPath())) {
				return true;
			}
		}

		return false;
	}
	
	public int hashCode() {
		return this.getPath().hashCode();
	}
}