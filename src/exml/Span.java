package exml;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

public class Span extends IntArrayList {
    public int getStart() {
        return get(0);
    }

    public int getEnd() {
        return get(size() - 1);
    }

    public int[] getHoles() {
        return subList(1, size() - 1).toArray();
    }

    public int spanLength() {
        int result = 0;
        for (int i = 0; i < size(); i += 2) {
            result += get(i + 1) - get(i);
        }
        return result;
    }

    public static Span setOrUnion(Span one, Span other, int threshold,
                                  int other_w) {
        Span result = new Span();
        int posn1 = 0, posn2 = 0;
        int w = 0;
        while (posn1 < one.size() && posn2 < other.size()) {
            int old_w = w;
            int diff = one.get(posn1) - other.get(posn2);
            if (diff <= 0) {
                if ((posn1 % 2) == 0) {
                    w += 1;
                } else {
                    w -= 1;
                }
            }
            if (diff >= 0) {
                if ((posn2 % 2) == 0) {
                    w += other_w;
                } else {
                    w -= other_w;
                }
            }
            if (old_w < threshold && w >= threshold) {
                result.add(one.get(posn1));
            } else if (old_w >= threshold && w < threshold) {
                result.add(one.get(posn1));
            }
            if (diff <= 0)
                posn1++;
            if (diff >= 0)
                posn2++;
        }
        if (threshold <= 1) {
            while (posn1 < one.size()) {
                result.add(one.get(posn1));
                posn1++;
            }
        }
        if (threshold <= other_w) {
            while (posn2 < other.size()) {
                result.add(other.get(posn2));
                posn2++;
            }
        }
        return result;
    }

    public Span union(Span other) {
        return setOrUnion(this, other, 1, 1);
    }

    public Span intersection(Span other) {
        return setOrUnion(this, other, 2, 1);
    }

    public Span setDiff(Span other) {
        return setOrUnion(this, other, 1, -1);
    }

}
