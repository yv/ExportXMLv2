// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import elkfed.ml.util.Alphabet;
import exml.objects.ObjectSchema;
import exml.objects.NamedObject;
import exml.objects.GenericObjectFactory;
import exml.objects.StringConverter;
import exml.objects.ReferenceConverter;
import exml.objects.Attribute;
import exml.objects.GenericAccessor;
import exml.objects.IAccessor;

public class TuebaSentenceSchema extends ObjectSchema<TuebaSentenceMarkable>
    {
        public static class TuebaSentenceFactory
          implements GenericObjectFactory<TuebaSentenceMarkable>
        {
            public TuebaSentenceMarkable createObject(ObjectSchema<TuebaSentenceMarkable> schema)
            {
                return new TuebaSentenceMarkable();
            }
        }
        public static TuebaSentenceFactory factory=new TuebaSentenceFactory();
        public static final Alphabet<String> global_alph=new Alphabet<String>();
        public static final TuebaSentenceSchema instance=new TuebaSentenceSchema();


        public TuebaSentenceSchema() {
            super("sentence", TuebaSentenceMarkable.class,
                  factory, global_alph);
        }
}