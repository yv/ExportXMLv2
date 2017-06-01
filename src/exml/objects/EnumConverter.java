package exml.objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import elkfed.ml.util.Alphabet;
import exml.Document;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.ObjectIntMap;

public class EnumConverter extends StringConverter implements IEnumConverter<String> {
	private final Alphabet<String> names = new Alphabet<>();
	private final List<String> descriptions = new ArrayList<>();

    @Override
    public String convertToString(String obj, Document doc) {
        return obj;
    }

    @Override
    public int endIndex() {
        return names.size();
    }

    @Override
    public int indexForName(String name) {
        return names.lookupIndex(name);
    }

    @Override
    public String objectForIndex(int index) {
        return names.lookupObject(index);
    }

    @Override
    public String nameForIndex(int index) {
        return names.lookupObject(index);
    }

    public void addVal(String name, String description) {

    }

    @Override
    public String descriptionForIndex(int index) {
        try {
            return descriptions.get(index);
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }
}