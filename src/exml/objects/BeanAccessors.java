package exml.objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import exml.MissingObjectException;
import exml.annotations.EXMLAttribute;
import exml.annotations.EXMLRelation;
import exml.annotations.MarkableSchema;
import exml.simple.SimpleToken;
import exml.tueba.TuebaNEMarkable;

/**
 * BeanAccessors provides some magic glue such that you can use any
 * subclass of GenericMarkable or GenericTerminal with bean attributes
 * in a convenient fashion.
 * 
 * Any class used here must either have a default constructor or one
 * that only uses an ObjectSchema parameter.
 * @author yannick
 *
 */
public class BeanAccessors extends ClassLoader implements Opcodes{
	@SuppressWarnings("rawtypes")
	Map<Class,ObjectSchema> _schemas = new HashMap<Class,ObjectSchema>();
	static BeanAccessors instance = new BeanAccessors();
	
	public static BeanAccessors getInstance() {
		return instance;
	}
	
	static enum AccessorType {
		METHOD,
		FIELD;
	}
	
	static class BeanAccessor<E, V> {
		public AccessorType tp = null;
		public boolean isRelation;
		public Class<E> host_cls;
		public Class<V> val_cls;
		public Type val_type;
		public String attName;
		public IAccessor<E, V> accessor;
		
		/** creates an object describing a getX method */
		@SuppressWarnings("unchecked")
		public BeanAccessor(Method m, Class<E> cls) {
            EXMLAttribute att = m.getAnnotation(EXMLAttribute.class);
			EXMLRelation att2 = m.getAnnotation(EXMLRelation.class);
			tp = AccessorType.METHOD;
			host_cls = cls;
			val_cls = ((Class<V>)m.getReturnType());
			val_type = m.getGenericReturnType();
			attName = StringUtils.uncapitalize(m.getName().substring(3));
			if (att != null) {
				if (att.value() != null) {
					attName = att.value();
				}
			}
			if (att2 != null) {
				isRelation = true;
				if (att2.value() != null) {
					attName = att2.value();
				}
			}
		}
		
		public BeanAccessor(Field f, Class<E> cls) {
			EXMLAttribute att = f.getAnnotation(EXMLAttribute.class);
			EXMLRelation att2 = f.getAnnotation(EXMLRelation.class);
			tp = AccessorType.FIELD;
			host_cls = cls;
			val_cls = ((Class<V>)f.getType());
			val_type = f.getGenericType();
			attName = f.getName();
			if (att != null) {
				if (att.value() != null) {
					attName = att.value();
				}
			}
			if (att2 != null) {
				isRelation = true;
				if (att2.value() != null) {
					attName = att2.value();
				}
			}
		}
		
		public Class<?> memberType() {
			if (List.class.isAssignableFrom(val_cls)) {
				ParameterizedType t_lst = (ParameterizedType) val_type;
				Type t_mem = t_lst.getActualTypeArguments()[0];
				try {
					return (Class<?>) t_mem;
				} catch (ClassCastException ex) {
					return (Class<?>)((ParameterizedType) t_mem).getRawType();
				}
			} else {
				return null;
			}
		}
		
		public <M extends GenericObject> Relation<E,M> makeRelation() {
			Class<M> cls = (Class<M>)memberType();
			ObjectSchema<M> schema = instance.schemaForClass(cls);
			IAccessor<E, List<M>> acc = (IAccessor<E, List<M>>)accessor;
			return new Relation<E,M>(attName, acc, schema);
		}
		
		public String getClassName() {
			return String.format("Auto_%s_%s", host_cls.getSimpleName(), attName);
		}
	}
	
	public static String nameForClass(Class<?> cls) {
		return "L"+cls.getCanonicalName().replace(".", "/")+";";
	}

	public static String classnameForClass(Class<?> cls) {
		return cls.getCanonicalName().replace(".", "/");
	}

	/**
	 * compiles an accessor for a bean property (with get... and set... methods)
	 * @param cls the method in question
	 * @param att the attribute
	 * @return bytecode for the class
	 * @throws NoSuchMethodException
	 */
	public static <E,V> byte[] compile(Class<E> cls, BeanAccessor<E, V> att) throws NoSuchMethodException {
		String attName = att.attName;
		Class<?> val_type = att.val_cls;
		String my_classname = att.getClassName();
		String cls_name = nameForClass(cls);
		String val_name = nameForClass(val_type);
		String cls_classname = classnameForClass(cls);
		String val_classname = classnameForClass(val_type);
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;
		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER,
				my_classname,
				String.format("Ljava/lang/Object;Lexml/objects/IAccessor<%s%s>;", 
						cls_name, val_name), 
				"java/lang/Object", new String[]{"exml/objects/IAccessor"});
		// default constructor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		// getter
		mv = cw.visitMethod(ACC_PUBLIC, "get",
				String.format("(%s)%s", cls_name, val_name),
				null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 1);
		if (att.tp == AccessorType.METHOD) {
			mv.visitMethodInsn(INVOKEVIRTUAL, cls_classname, "get"+StringUtils.capitalize(attName),
					"()"+val_name, false);
		} else {
			mv.visitFieldInsn(GETFIELD, cls_classname, attName, val_name);
		}
		mv.visitInsn(ARETURN);
		mv.visitMaxs(1, 2);
		mv.visitEnd();
		// setter
		mv = cw.visitMethod(ACC_PUBLIC, "put",
				String.format("(%s%s)V", cls_name, val_name),
				null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		if (att.tp == AccessorType.METHOD) {
			mv.visitMethodInsn(INVOKEVIRTUAL, cls_classname, "set"+StringUtils.capitalize(attName),
					"("+val_name+")V", false);
		} else {
			mv.visitFieldInsn(PUTFIELD, cls_classname, attName, val_name);
		}
		mv.visitInsn(RETURN);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
		
		// virtual put
		mv = cw.visitMethod(ACC_PUBLIC+ACC_BRIDGE+ACC_SYNTHETIC, "put",
				"(Ljava/lang/Object;Ljava/lang/Object;)V",
				null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(CHECKCAST, cls_classname);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitTypeInsn(CHECKCAST, val_classname);
		mv.visitMethodInsn(INVOKEVIRTUAL, my_classname, "put",
				String.format("(%s%s)V", cls_name, val_name),
				false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(3, 3);
		mv.visitEnd();
		
		// virtual get
		mv = cw.visitMethod(ACC_PUBLIC+ACC_BRIDGE+ACC_SYNTHETIC, "get",
				"(Ljava/lang/Object;)Ljava/lang/Object;",
				null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(CHECKCAST, cls_classname);
		mv.visitMethodInsn(INVOKEVIRTUAL, my_classname, "get",
				String.format("(%s)%s", cls_name, val_name),
				false);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
		
		return cw.toByteArray();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E,V> BeanAccessor<E,V> makeAccessor(Method m, Class<E> cls) {
		try {
			BeanAccessor acc = new BeanAccessor(m, cls);
			Class clazz;
			clazz = instance.findLoadedClass(acc.getClassName());
			if (clazz == null) {
				byte[] data= compile(cls, acc);
				clazz= instance.defineClass(acc.getClassName(),
						data, 0, data.length);
			}
			acc.accessor = (IAccessor)clazz.newInstance();
			return acc;
		} catch(Exception ex) {
			throw new RuntimeException("Cannot build accessor",ex);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E,V> BeanAccessor<E,V> makeAccessor(Field f, Class<E> cls) {
		try {
			BeanAccessor acc = new BeanAccessor(f, cls);
			Class clazz;
			clazz = instance.findLoadedClass(acc.getClassName());
			if (clazz == null) {
				byte[] data= compile(cls, acc);
				clazz = instance.defineClass(acc.getClassName(),
						data, 0, data.length);
			}
			acc.accessor = (IAccessor)clazz.newInstance();
			return acc;
		} catch(Exception ex) {
			throw new RuntimeException("Cannot build accessor",ex);
		}
	}
	public static <E> List<BeanAccessor<E,?>> getAccessors(Class<E> cls) {
		//TODO also treat public properties as attributes
		//TODO ignore properties with JsonIgnore annotation
		List<BeanAccessor<E,?>> result = new ArrayList<BeanAccessors.BeanAccessor<E,?>>();
		for (Method m: cls.getMethods()) {
			String m_name = m.getName();
			if (m_name.matches("get(Holes|Start|End|XMLId|Class)") &&
					(m.getModifiers() & Modifier.STATIC) == 0)
				continue;
			if(m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
				result.add(makeAccessor(m, cls));
			}
		}
		for (Field f: cls.getFields()) {
			String f_name = f.getName();
			if ((f.getModifiers() & Modifier.PUBLIC) != 0 &&
					(f.getModifiers() & Modifier.STATIC) == 0 &&
					!f_name.matches("class")) {
				result.add(makeAccessor(f, cls));
			}
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <E extends GenericObject, V> List<Attribute<E, V>> addBeanAttributes(ObjectSchema<E> schema, Class<E> cls) {
		List<Attribute<E,V>> att = new ArrayList<Attribute<E,V>>();
		for (BeanAccessor acc: getAccessors(cls)) {
			Class<V> val_cls = acc.val_cls;
			if (acc.isRelation) {
				schema.addRelation(acc.makeRelation());
			} else {
				IConverter conv = null;
				if (val_cls == String.class) {
					conv =StringConverter.instance;
				} else if (NamedObject.class.isAssignableFrom(val_cls)) {
					conv = new ReferenceConverter<NamedObject>();
				} else if (val_cls.isEnum()) {
					conv = new JavaEnumConverter(val_cls);
				} else {
					throw new RuntimeException("No converter for class:"+val_cls.toString());
				}
				schema.addAttribute(new Attribute<E, V>(acc.attName, acc.accessor, conv));
			}
		}
		return att;
	}
	
	public static <E extends GenericObject> GenericObjectFactory<E> factoryForClass(final Class<E> cls) {
		return new GenericObjectFactory<E>() { 
				public E createObject(ObjectSchema<E> schema) { 
					try {
						try {
							return cls.newInstance();
						} catch (InstantiationException ex2) {
							    Constructor<E> c = cls.getConstructor(ObjectSchema.class);
							    return c.newInstance(schema);
						}
					} catch (Exception ex) {
						throw new RuntimeException("Cannot create", ex);}
				}};
	}
	
	@SuppressWarnings("unchecked")
	public <E extends GenericObject> ObjectSchema<E> schemaForClass(final Class<E> cls) {
		ObjectSchema<E> result;
		result = _schemas.get(cls);
		if (result == null) {
			MarkableSchema schema_anno = cls.getAnnotation(MarkableSchema.class);
			if (schema_anno != null) {
				Class<? extends ObjectSchema> schema_cls = schema_anno.value();
				try {
					try {
						result = (ObjectSchema<E>) schema_anno.annotationType().getField("instance").get(null);
					} catch (NoSuchFieldException ex){
						result = schema_cls.newInstance();
					}
				} catch (ReflectiveOperationException ex) {
					throw new RuntimeException("Cannot acquire schema instance: "+schema_cls.toString());
				}
			} else {
				result = new ObjectSchema<E>(cls.getSimpleName().toLowerCase(),
						cls,
						factoryForClass(cls));
				addBeanAttributes(result, cls);
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
		ObjectSchema<TuebaNEMarkable> schema = instance.schemaForClass(TuebaNEMarkable.class);
		ObjectSchema<SimpleToken> token_schema = instance.schemaForClass(SimpleToken.class);
		ObjectSchema<SimpleToken> tokn_schema = instance.schemaForClass(SimpleToken.class);
		TuebaNEMarkable m = schema.createMarkable();
		m.setKind("ORG");
		@SuppressWarnings("unchecked")
		Attribute<TuebaNEMarkable, String> att =
			(Attribute<TuebaNEMarkable, String>) schema.getAttribute("type");
		System.out.println(att.getString(m, null));
		try {
			att.putString(m, "PER", null);
			System.out.println(m.getKind());
		} catch (MissingObjectException ex) {
			ex.printStackTrace();
		}
		SimpleToken tok = token_schema.createMarkable();
		tok.setWord("foo");
		@SuppressWarnings("unchecked")
		Attribute<SimpleToken, String> word =
			(Attribute<SimpleToken, String>) token_schema.getAttribute("word");
		System.out.println(word.getString(tok, null));
		try {
			word.putString(tok, "bar", null);
		} catch (MissingObjectException ex) {
			ex.printStackTrace();
		}
		System.out.println(tok.getWord());
	}
}
