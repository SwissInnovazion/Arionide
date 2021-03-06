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
package ch.innovazion.arionide.menu.params;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import ch.innovazion.arionide.events.GeometryInvalidateEvent;
import ch.innovazion.arionide.events.MessageEvent;
import ch.innovazion.arionide.events.MessageType;
import ch.innovazion.arionide.lang.symbols.AtomicValue;
import ch.innovazion.arionide.lang.symbols.Enumeration;
import ch.innovazion.arionide.lang.symbols.Information;
import ch.innovazion.arionide.lang.symbols.InvalidValueException;
import ch.innovazion.arionide.lang.symbols.Node;
import ch.innovazion.arionide.lang.symbols.Numeric;
import ch.innovazion.arionide.lang.symbols.Reference;
import ch.innovazion.arionide.lang.symbols.Text;
import ch.innovazion.arionide.lang.symbols.Variable;
import ch.innovazion.arionide.menu.MenuDescription;
import ch.innovazion.arionide.menu.MenuManager;
import ch.innovazion.arionide.project.managers.specification.InformationManager;
import ch.innovazion.arionide.ui.overlay.Views;
import ch.innovazion.automaton.Export;
import ch.innovazion.automaton.Inherit;

public class NodeUpdater extends ParameterUpdater {

	private InformationManager infoManager;
	private List<Node> children;
	private int separator;
	
	@Inherit
	@Export
	protected boolean frozen;
	
	private Node currentNode;
	private Node selectedNode;
	
	public NodeUpdater(MenuManager manager) {
		super(manager);
	}
	
	protected void onEnter() {
		super.onEnter();
		updateCurrentNode(currentNode);
	}
	
	protected void onRefresh(String identifier, Object prevValue) {
		super.onRefresh(identifier, prevValue);
				
		if(identifier.equals("value")) {			
			if(value instanceof Information) {
				this.infoManager = getSpecificationManager().loadInformationManager(value);
				updateCurrentNode(null);
			} else if(value instanceof Node) {
				this.infoManager = getSpecificationManager().loadInformationManager(null);
				updateCurrentNode((Node) value);
			}
		}
	}
	
	private void updateCurrentNode(Node currentNode) {		
		if(currentNode == null) {
			if(infoManager != null && infoManager.hasContext()) {
				currentNode = infoManager.getRootNode();
			} else {
				go("..");
				return;
			}
		}
		
		this.currentNode = currentNode;
		this.children = currentNode.getNodes();
		
		List<String> elements = new ArrayList<>();
				
		boolean frozen = this.frozen;
		
		if(value instanceof Node) {
			Node current = (Node) value;
			while((current = current.getParent()) != null);
			frozen |= currentNode.equals(current); // Freeze root nodes
		}
		
		if(currentNode instanceof Numeric) {
			elements.add("Set number");
			
			if(!frozen) {
				elements.add(null);
				elements.addAll(Arrays.asList("As node", "As text", "As enumeration", "As constant", "As variable", "As reference"));
			}
			
			this.separator = elements.size();
		} else if(currentNode instanceof Text) {
			elements.add("Set text");
			
			if(!frozen) {
				elements.add(null);
				elements.addAll(Arrays.asList("As node", "As number", "As enumeration", "As constant", "As variable", "As reference"));
			}
			
			this.separator = elements.size();
		} else if(currentNode instanceof Enumeration) {		
			if(!frozen) {
				elements.add("Setup enumeration");
				elements.add(null);
				elements.addAll(Arrays.asList("As node", "As number", "As text", "As constant", "As variable", "As reference"));
			} else {
				elements.add("Select element");
			}
			
			this.separator = elements.size();
		} else if(currentNode instanceof Variable) {
			if(!frozen) {
				elements.add("Setup variable");
				elements.add(null);
				elements.addAll(Arrays.asList("As node", "As number", "As text", "As constant", "As enumeration", "As reference"));
			} else {
				elements.add("Select variable");
			}
			
			this.separator = elements.size();
		} else if(currentNode instanceof Reference) {
			if(!frozen) {
				elements.add("Setup reference");
				elements.add(null);
				elements.addAll(Arrays.asList("As node", "As number", "As text", "As enumeration", "As constant", "As variable"));
			} else {
				elements.add("Select reference");
			}
			
			this.separator = elements.size();
		} else {
			elements.add("New node");
			
			elements.add(null);
			this.separator = elements.size();
			children.stream().map(Node::getLabel).forEach(elements::add);
			
			if(!frozen) {
				elements.add(null);
				elements.addAll(Arrays.asList("As number", "As text", "As variable", "As constant", "As reference", "As enumeration"));
			}
		}		
		
		elements.add("From constant");
		
		elements.add(null);
		
		if(currentNode.getParent() != null) {
			elements.add("Parent");
		}
		
		if(!frozen) {
			elements.add("Label");
			elements.add("Destroy");
		}
		
		setDynamicElements(elements.toArray(new String[0]));
		
		updateCursor(0);
		
		updateDescription();
	}

	public void onAction(String action) {		
		if(id < separator || id >= separator + children.size()) {
			switch(action) {
			case "New node":
				createNode();
				break;
			case "As node":
				reassignAsNode();
				break;
			case "As number":
				reassignAsParseable(Numeric::new);
				break;
			case "Set number":
				setParseable();
				break;
			case "As text":
				reassignAsParseable(Text::new);
				break;
			case "Set text":
				setParseable();
				break;
			case "As variable":
				reassignAsVariable();
				break;
			case "Setup variable":
				this.value = currentNode;
				go("var");
				break;
			case "Select variable":
				this.value = currentNode;
				go("var:assign");
				break;
			case "As reference":
				reassignAsReference();
				break;
			case "Setup reference":
				this.value = currentNode;
				go("ref");
				break;
			case "Select reference":
				this.value = currentNode;
				go("ref:assign");
				break;
			case "As enumeration":
				reassignAsEnumeration();
				break;
			case "Setup enumeration":
				this.value = currentNode;
				go("enum");
				break;
			case "Select element":
				this.value = currentNode;
				go("enum:assign");
				break;
			case "From constant":
				if(currentNode.getParent() != null) {
					this.value = currentNode;
				}
				
				go("const");
				break;
			case "Parent":
				back();
				break;
			case "Label":
				Views.input.setText("Please enter the label of the information")
						   .setPlaceholder("Information label")
						   .setResponder(this::label)
						   .stackOnto(Views.code);				
				break;
			case "Destroy":
				destroy();
				break;
			}
		} else {
			updateCurrentNode(children.get(id - separator));
			go(".");
		}
	}
	
	public void up() {
		back();
	}
	
	private void createNode() {
		MessageEvent result = infoManager.createNode(currentNode);
		
		dispatch(result);
		
		if(result.getMessageType() != MessageType.ERROR) {
			dispatch(new GeometryInvalidateEvent(0));
			int nodeIndex = currentNode.getNumElements() - 1;
			
			updateParameter();			
			updateCursor(separator + nodeIndex);
		}
	}
	
	private void reassignAsNode() {
		Node node = new Node("new_node");
		dispatch(infoManager.assign(currentNode, node));
		dispatch(new GeometryInvalidateEvent(0));
		
		updateCurrentNode(node);
		updateParameter();
	}
	
	private void reassignAsEnumeration() {
		Enumeration enumeration = new Enumeration();
		dispatch(infoManager.assign(currentNode, enumeration));
		dispatch(new GeometryInvalidateEvent(0));
		
		updateCurrentNode(enumeration);
		updateParameter();
		
		this.value = enumeration;
		go("enum");
	}
	
	private void reassignAsVariable() {
		Variable variable = new Variable();
		dispatch(infoManager.assign(currentNode, variable));
		dispatch(new GeometryInvalidateEvent(0));
		
		updateCurrentNode(variable);
		updateParameter();
		
		this.value = variable;
		go("var");
	}
	
	private void reassignAsReference() {
		Reference ref = new Reference();
		dispatch(infoManager.assign(currentNode, ref));
		dispatch(new GeometryInvalidateEvent(0));
		
		updateCurrentNode(ref);
		updateParameter();
		
		this.value = ref;		
		go("ref");
	}
	
	private void reassignAsParseable(Supplier<Node> valueAllocator) {
		Views.input.setText("Please enter the value of the information")
				   .setPlaceholder("Value")
				   .setResponder(rawValue -> reassignParseable0(rawValue, valueAllocator))
				   .stackOnto(Views.code);
	}
	
	private void reassignParseable0(String rawValue, Supplier<Node> valueAllocator) {
		Node value = valueAllocator.get();
		
		try {
			value.parse(rawValue);
			dispatch(infoManager.assign(currentNode, value));
			dispatch(new GeometryInvalidateEvent(0));
			updateCurrentNode(value);
		} catch (InvalidValueException e) {
			dispatch(new MessageEvent(e.getMessage(), MessageType.ERROR));
		}
				
		updateParameter();
	}
	
	private void setParseable() {
		Views.input.setText("Please enter the value of the information")
				   .setPlaceholder("Value")
				   .setResponder(this::setParseable0)
				   .stackOnto(Views.code);
	}
	
	private void setParseable0(String rawValue) {		
		try {
			currentNode.parse(rawValue);
			dispatch(new GeometryInvalidateEvent(0));
		} catch (InvalidValueException e) {
			dispatch(new MessageEvent(e.getMessage(), MessageType.ERROR));
		}
				
		updateParameter();
	}
	
	private void back() {
		if(currentNode != null) {
			updateCurrentNode(currentNode.getParent());
			go(".");
		}
	}
	
	private void label(String name) {
		dispatch(infoManager.setLabel(currentNode, name));
		dispatch(new GeometryInvalidateEvent(0));
		updateParameter();
	}
	
	private void destroy() {
		if(currentNode.getParent() != null) {
			dispatch(infoManager.destroy(currentNode));
			dispatch(new GeometryInvalidateEvent(2));
			updateParameter();
			back();	
		} else if(infoManager.hasContext()) {
			updateCurrentNode(infoManager.reset());
			updateParameter();
		} else {
			System.err.println("Failed to destroy node '" + currentNode.getPath() + "'");
		}
	}
	
	protected void updateCursor(int cursor) {
		super.updateCursor(cursor);
		
		if(children != null) {
			if(id >= separator && id < separator + children.size()) {
				this.selectedNode = children.get(id - separator);
			} else {
				this.selectedNode = null;
			}	
		}
		
		if(value != null) {
			updateDescription();
		}
	}
	
	protected void updateDescription() {
		List<String> elements = new ArrayList<>();
			
		elements.add(getDescriptionTitle());
		
		if(currentNode != null) {
			elements.addAll(currentNode.getDisplayValue());
		} else if(value != null) {
			elements.addAll(value.getDisplayValue());
		}

		elements.add(new String());
		
		if(selectedNode != null) {
			elements.addAll(selectedNode.getDisplayValue());
		}
		
		this.description = new MenuDescription(elements.toArray(new String[0]));
	}
	
	protected String getDescriptionTitle() {
		if(currentNode != null) {
			if(currentNode instanceof AtomicValue) {
				return "Setting value of '" + currentNode.getPath() + "'";
			} else {
				return "Modifying structure of node '" + currentNode.getPath() + "'";
			}
		} else {
			return "Modifying node structure";
		}
	}
}