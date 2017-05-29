package exml.objects;

/** Rough categorization of converters for
 *  (a) being able to special-case value conversion in MsgPack
 *  (b) being able to create a suitable attribute incl. converter
 *
 *  These are used as int IDs in MessagePack -> be careful with changes
 */
public enum ConverterKind {
    // serialized to a string and back, open set of values
    STRING,
    // serialized to a string and back, (mostly) closed set of values
    // that is included as tag set
    ENUM,
    // the string value is the ID to another node
    REF,
    // the string value is a list of node IDs
    REFLIST,
    // one or multiple struct-valued items
    EDGE,
    OTHER;
}
