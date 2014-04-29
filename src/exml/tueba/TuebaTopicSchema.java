// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import elkfed.ml.util.Alphabet;
import exml.objects.GenericAccessor;
import exml.objects.GenericObjectFactory;
import exml.objects.ObjectSchema;
import exml.objects.StringConverter;

public class TuebaTopicSchema extends ObjectSchema<TuebaTopicMarkable>
    {
        public static class TuebaTopicFactory
          implements GenericObjectFactory<TuebaTopicMarkable>
        {
            public TuebaTopicMarkable createObject(ObjectSchema<TuebaTopicMarkable> schema)
            {
                return new TuebaTopicMarkable();
            }
        }
        public static TuebaTopicFactory factory=new TuebaTopicFactory();
        public static final Alphabet<String> global_alph=new Alphabet<String>();
        public static final int IDX_description=global_alph.lookupIndex("description");
        public static final int IDX_discRel=global_alph.lookupIndex("discRel");
        public static final TuebaTopicSchema instance=new TuebaTopicSchema();


        public TuebaTopicSchema() {
            super("topic",factory,global_alph);
            addAttribute("description", new StringConverter(),
                         new GenericAccessor<TuebaTopicMarkable,String>(IDX_description));
            addRelation("discRel", TuebaDiscRelSchema.instance);
        }

}