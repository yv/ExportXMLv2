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

public class TuebaEduRangeSchema extends ObjectSchema<TuebaEduRangeMarkable>
    {
        public static class TuebaEduRangeFactory
          implements GenericObjectFactory<TuebaEduRangeMarkable>
        {
            public TuebaEduRangeMarkable createObject(ObjectSchema<TuebaEduRangeMarkable> schema)
            {
                return new TuebaEduRangeMarkable();
            }
        }
        public static TuebaEduRangeFactory factory=new TuebaEduRangeFactory();
        public static final Alphabet<String> global_alph=new Alphabet<String>();
        public static final int IDX_discRel=global_alph.lookupIndex("discRel");
        public static final TuebaEduRangeSchema instance=new TuebaEduRangeSchema();


        public TuebaEduRangeSchema() {
            super("edu-range",factory,global_alph);
            addRelation("discRel", TuebaDiscRelSchema.instance);
        }

}