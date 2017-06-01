package exml.objects;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/** an interface for all enum converters
 */
public interface IEnumConverter<E> extends IConverter<E> {
    int endIndex();
    int indexForName(String name);
    E objectForIndex(int index);
    String nameForIndex(int index);
    String descriptionForIndex(int index);

    default E convertFromString(String name) {
        return objectForIndex(indexForName(name));
    }

    default void declareAttribute(String name, XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeStartElement("enum-attr");
        writer.writeAttribute("name", name);
        for (int i=0; i<endIndex(); i++) {
            writer.writeCharacters("\n  ");
            writer.writeStartElement("val");
            writer.writeAttribute("name", nameForIndex(i));
            String description = descriptionForIndex(i);
            if (description != null) {
                writer.writeAttribute("description", description);
            }
            writer.writeEndElement();
        }
        writer.writeCharacters("\n");
        writer.writeEndElement();
    }
    default ConverterKind getKind() {
        return ConverterKind.ENUM;
    }
}
