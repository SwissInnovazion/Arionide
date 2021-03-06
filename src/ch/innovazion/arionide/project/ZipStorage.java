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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import ch.innovazion.arionide.debugging.Debug;
import ch.innovazion.arionide.debugging.IAm;
import ch.innovazion.arionide.project.mutables.MutableCodeChain;
import ch.innovazion.arionide.project.mutables.MutableHierarchyElement;
import ch.innovazion.arionide.project.mutables.MutableHistoryElement;
import ch.innovazion.arionide.project.mutables.MutableInheritanceElement;
import ch.innovazion.arionide.project.mutables.MutableStructure;

public class ZipStorage extends Storage {

	private static final Map<String, String> fileSystemEnvironment = new HashMap<>();

	static {
		fileSystemEnvironment.put("create", "true");
	}
	
	private final File location;
	
	private URI uri;
	private FileSystem fs;
	
	private Path metaPath;
	private Path hierarchyPath;
	private Path inheritancePath;
	private Path callGraphPath;
	private Path historyPath;
	private Path structureMetaPath;
	private Path codePath;
	private Path languagesPath;
		
	@IAm("initializing the project storage")
	public ZipStorage(File path) {
		this.location = path;
	}
	
	public void initFS() {
		try {
			try {
				File backup = new File(this.location.getParentFile(), this.location.getName() + ".bak");
								
				if(this.location.exists()) {
					Files.copy(this.location.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
				
				this.uri = URI.create("jar:" + this.location.toURI());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.initFS0();
						
			this.metaPath = this.init(null, null, "meta", "project");
			this.hierarchyPath = this.init(ArrayList<MutableHierarchyElement>::new, this::setMutableHierarchy, "struct", "hierarchy");
			this.inheritancePath = this.init(HashMap<Integer, MutableInheritanceElement>::new, this::setMutableInheritance, "struct", "inheritance");
			this.callGraphPath = this.init(ArrayList<MutableHierarchyElement>::new, this::setMutableCallGraph, "struct", "callgraph");
			this.structureMetaPath = this.init(HashMap<Integer, MutableStructure>::new, this::setMutableStructures, "meta", "structures");
			this.historyPath = this.init(ArrayList<MutableHistoryElement>::new, this::setMutableHistory, "history");
			this.codePath = this.init(HashMap<Integer, MutableCodeChain>::new, this::setMutableCode, "code");
			this.languagesPath = this.init(HashMap<String, Integer>::new, this::setMutableLanguages, "lang");
		} catch (IOException exception) {
			Debug.exception(exception);
		}
	}
	
	private void initFS0() throws IOException {
		try {
			this.fs = FileSystems.getFileSystem(this.uri);
		} catch(Exception e) {
			this.fs = FileSystems.newFileSystem(this.uri, fileSystemEnvironment);
		}
	}
	
	protected void closeFS() {
		if(this.fs != null && this.fs.isOpen()) {
			try {
				this.fs.close();
			} catch (IOException e) {
				// Do not use an AWT popup in a shutdown hook
				e.printStackTrace();
			}
		}
	}
	
	private <T> Path init(Supplier<T> allocator, Consumer<T> initializer, String first, String... more) throws IOException {
		Path path = this.fs.getPath(first, more);
		
		if(!Files.exists(path)) {
			if(more.length > 0) {
				String[] dirs = new String[more.length - 1];
				
				System.arraycopy(more, 0, dirs, 0, dirs.length);
				
				Files.createDirectories(this.fs.getPath(first, dirs));
			}
			
			Files.createFile(path);
						
			if(allocator != null) {
				T object = allocator.get();
				
				if(initializer != null) {
					initializer.accept(object);
				}
				
				this.save(path, object);
			}
		}
			
		return path;
	}
	
	public File getLocation() {
		return this.location;
	}
		
	public Path getMetaPath() {
		return this.metaPath;
	}
	
	public void loadHierarchy() throws StorageException {	
		this.setMutableHierarchy(this.load(this.hierarchyPath));
	}
	
	public void saveHierarchy() throws StorageException {
		this.save(this.hierarchyPath, this.getMutableHierarchy());
	}
	
	public void loadInheritance() throws StorageException {	
		this.setMutableInheritance(this.load(this.inheritancePath));
	}
	
	public void saveInheritance() throws StorageException {
		this.save(this.inheritancePath, this.getMutableInheritance());
	}
	
	public void loadCallGraph() throws StorageException {	
		this.setMutableCallGraph(this.load(this.callGraphPath));
	}
	
	public void saveCallGraph() throws StorageException {
		this.save(this.callGraphPath, this.getMutableCallGraph());
	}
	
	public void loadStructureMeta() throws StorageException {	
		this.setMutableStructures(this.load(this.structureMetaPath));
	}
	
	public void saveStructureMeta() throws StorageException {
		this.save(this.structureMetaPath, this.getMutableStructures());
	}
	
	public void loadHistory() throws StorageException {	
		this.setMutableHistory(this.load(this.historyPath));
	}
	
	public void saveHistory() throws StorageException {
		this.save(this.historyPath, this.getMutableHistory());
	}
	
	public void loadCode() throws StorageException {
		this.setMutableCode(this.load(this.codePath));
	}
	
	public void saveCode() throws StorageException {
		this.save(this.codePath, this.getMutableCode());
	}

	public void loadLanguages() throws StorageException {
		this.setMutableLanguages(this.load(this.languagesPath));
	}

	public void saveLanguages() throws StorageException {
		this.save(this.languagesPath, this.getMutableLanguages());
	}
	
	@SuppressWarnings("unchecked")
	private synchronized <T extends Serializable> T load(Path path) throws StorageException {
		try(ObjectInputStream input = new ObjectInputStream(Files.newInputStream(path))) {
			return (T) input.readObject();
		} catch (IOException | ClassNotFoundException exception) {
			throw new StorageException("Unable to load resource", exception);
		}
	}
	
	private synchronized void save(Path path, Object object) throws StorageException {
		if(object != null) {
			assert object instanceof Serializable;
	
			try(ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
				output.writeObject(object);
			} catch (IOException exception) {
				throw new StorageException("Unable to save resource", exception);
			}
			
			location.setLastModified(System.currentTimeMillis());
		}
	}
}
