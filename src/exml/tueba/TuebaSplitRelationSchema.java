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

public class TuebaSplitRelationSchema extends ObjectSchema<TuebaSplitRelationEdge>
    {
        public static class TuebaSplitRelationFactory
          implements GenericObjectFactory<TuebaSplitRelationEdge>
        {
            public TuebaSplitRelationEdge createObject(ObjectSchema<TuebaSplitRelationEdge> schema)
            {
                return new TuebaSplitRelationEdge();
            }
        }
        public static TuebaSplitRelationFactory factory=new TuebaSplitRelationFactory();
        public static final Alphabet<String> global_alph=new Alphabet<String>();
        public static final Attribute<TuebaSplitRelationEdge, String> ATTR_type = new Attribute<TuebaSplitRelationEdge, String> ("type",
                new IAccessor<TuebaSplitRelationEdge, String>() {
                    public String get(TuebaSplitRelationEdge o) {
                       return o.getType(); }
                    public void put(TuebaSplitRelationEdge o, String v) {
                       o.setType(v); }},
                new StringConverter());
        public static final Attribute<TuebaSplitRelationEdge, String> ATTR_target = new Attribute<TuebaSplitRelationEdge, String> ("target",
                new IAccessor<TuebaSplitRelationEdge, String>() {
                    public String get(TuebaSplitRelationEdge o) {
                       return o.getTarget(); }
                    public void put(TuebaSplitRelationEdge o, String v) {
                       o.setTarget(v); }},
                new StringConverter());
        public static final TuebaSplitRelationSchema instance=new TuebaSplitRelationSchema();


        public TuebaSplitRelationSchema() {
            super("splitRelation", TuebaSplitRelationEdge.class,
                  factory, global_alph);
            addAttribute(ATTR_type);
            addAttribute(ATTR_target);
        }
}