// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import elkfed.ml.util.Alphabet;
import exml.objects.ObjectSchema;
import exml.objects.NamedObject;
import exml.objects.GenericObjectFactory;
import exml.objects.StringConverter;
import exml.objects.ReferenceConverter;
import exml.objects.GenericAccessor;

public class TuebaTextSchema extends ObjectSchema<TuebaTextMarkable>
    {
        public static class TuebaTextFactory
          implements GenericObjectFactory<TuebaTextMarkable>
        {
            public TuebaTextMarkable createObject(ObjectSchema<TuebaTextMarkable> schema)
            {
                return new TuebaTextMarkable();
            }
        }
        public static TuebaTextFactory factory=new TuebaTextFactory();
        public static final Alphabet<String> global_alph=new Alphabet<String>();
        public static final int IDX_origin=global_alph.lookupIndex("origin");
        public static final TuebaTextSchema instance=new TuebaTextSchema();


        public TuebaTextSchema() {
            super("text",factory,global_alph);
            addAttribute("origin", new StringConverter(),
                         new GenericAccessor<TuebaTextMarkable,String>(IDX_origin));
        }

}