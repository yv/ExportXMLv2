package exml.objects;

public interface IAccessor<Obj,Val> {
	Val get(Obj o);
	void put(Obj o, Val v);
}
