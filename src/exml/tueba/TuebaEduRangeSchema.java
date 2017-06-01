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

public class TuebaEduRangeSchema extends ObjectSchema<TuebaEduRangeMarkable>
    {
        static final Alphabet<String> global_alph=new Alphabet<String>();
        static final int IDX_discRel=global_alph.lookupIndex("discRel");
        public static final TuebaEduRangeSchema instance=new TuebaEduRangeSchema();


        public TuebaEduRangeSchema() {
            super("edu-range", TuebaEduRangeMarkable.class,
                    (schema) -> new TuebaEduRangeMarkable(), global_alph);
            addRelation("discRel", TuebaDiscRelSchema.instance);
        }
}