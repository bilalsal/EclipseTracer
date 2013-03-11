package visdebugger.arrays.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.internal.debug.core.model.JDIType;

import visdebugger.arrays.control.AbstractArrayController;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

public class ArrayExpression {
	
	private String name;
	
	private IJavaArray value;
	
	private IJavaType compType;

	private Type compJDIType;
	
	private Field[] children;
	
	HashMap<FieldImpl, AbstractArrayController> controllers;

	public ArrayExpression(String name, IJavaArray value) {
		this.name = name;
		this.value = value;
		controllers = new HashMap<FieldImpl, AbstractArrayController>();
		try {
			compType = ((IJavaArrayType)value.getJavaType()).getComponentType();
			compJDIType = ((JDIType)compType).getUnderlyingType();
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String getName() {
		return name;
	}
	
	public IJavaArray getValue() {
		return value;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean hasChildren() {
		if (compType instanceof IJavaReferenceType) {
			try {
				return ((IJavaReferenceType)compType).getDeclaredFieldNames().length > 0;
			} catch (DebugException e) {
			}
		}
		return false;
	}

	public Object[] getChildren() {
		if (children != null) {
			return children;
		}		
		if (compType instanceof IJavaReferenceType) {
			IJavaReferenceType refType = (IJavaReferenceType)compType;
			try {
				String[] fnames = refType.getDeclaredFieldNames();
				ArrayList<Field> children = new ArrayList<Field>(fnames.length); 
				ReferenceType javaType = (ReferenceType)((JDIType)refType).getUnderlyingType();
				for (String fName : fnames) {
					Field fieldVariable = javaType.fieldByName(fName);
					if (!fieldVariable.isSynthetic()) {
						children.add(fieldVariable);
					}
				}
				this.children = children.toArray(new Field[0]);				
			} catch (DebugException e) {
			}
		}
		return this.children;
	}

	public AbstractArrayController getController(FieldImpl field) {
		return controllers.get(field);
	}

	public void setController(FieldImpl field, AbstractArrayController controller) {
		controllers.put(field, controller);
	}

	public Type getType(FieldImpl field) {
		if (field == null) {
			return compJDIType;
		}
		else {
			ReferenceType refType = (ReferenceType)compJDIType;
			try {
				return refType.fieldByName(field.name()).type();
			} catch (ClassNotLoadedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return compJDIType;
			}
		}
	}

}
