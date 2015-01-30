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
        public static final Attribute<TuebaTopicMarkable, String> ATTR_description = new Attribute<TuebaTopicMarkable, String> ("description",
                new IAccessor<TuebaTopicMarkable, String>() {
                    public String get(TuebaTopicMarkable o) {
                       return o.getDescription(); }
                    public void put(TuebaTopicMarkable o, String v) {
                       o.setDescription(v); }},
                new StringConverter());
        public static final int IDX_discRel=global_alph.lookupIndex("discRel");
        public static final TuebaTopicSchema instance=new TuebaTopicSchema();


        public TuebaTopicSchema() {
            super("topic", TuebaTopicMarkable.class,
                  factory, global_alph);
            addAttribute(ATTR_description);
            addRelation("discRel", TuebaDiscRelSchema.instance);
        }
}