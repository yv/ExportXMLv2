// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import elkfed.ml.util.Alphabet;
import exml.objects.*;

public class TuebaDiscRelSchema extends ObjectSchema<TuebaDiscRelEdge>
    {
        public static final GenericObjectFactory<TuebaDiscRelEdge> factory =
                ((schema) -> new TuebaDiscRelEdge());
        public static final Alphabet<String> global_alph=new Alphabet<String>();
        public static final Attribute<TuebaDiscRelEdge, String> ATTR_relation = new Attribute<TuebaDiscRelEdge, String> ("relation",
                new IAccessor<TuebaDiscRelEdge, String>() {
                    public String get(TuebaDiscRelEdge o) {
                       return o.getRelation(); }
                    public void put(TuebaDiscRelEdge o, String v) {
                       o.setRelation(v); }},
                new EnumConverter());
        public static final Attribute<TuebaDiscRelEdge, String> ATTR_marking = new Attribute<TuebaDiscRelEdge, String> ("marking",
                new IAccessor<TuebaDiscRelEdge, String>() {
                    public String get(TuebaDiscRelEdge o) {
                       return o.getMarking(); }
                    public void put(TuebaDiscRelEdge o, String v) {
                       o.setMarking(v); }},
                new StringConverter());
        public static final Attribute<TuebaDiscRelEdge, NamedObject> ATTR_arg2 = new Attribute<TuebaDiscRelEdge, NamedObject> ("arg2",
                new IAccessor<TuebaDiscRelEdge, NamedObject>() {
                    public NamedObject get(TuebaDiscRelEdge o) {
                       return o.getArg2(); }
                    public void put(TuebaDiscRelEdge o, NamedObject v) {
                       o.setArg2(v); }},
                new ReferenceConverter<>());
        public static final TuebaDiscRelSchema instance=new TuebaDiscRelSchema();


        public TuebaDiscRelSchema() {
            super("discRel", TuebaDiscRelEdge.class,
                  factory, global_alph);
            addAttribute(ATTR_relation);
            addAttribute(ATTR_marking);
            addAttribute(ATTR_arg2);
        }
}