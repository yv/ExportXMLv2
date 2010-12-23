package exml;

import exml.objects.INamedObject;

public interface IMarkable extends INamedObject {
	int getStart();
	int getEnd();
	int[] getHoles();
}
