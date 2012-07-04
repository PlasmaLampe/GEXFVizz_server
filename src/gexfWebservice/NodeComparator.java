package gexfWebservice;

import java.util.Comparator;

class NodeComparator implements Comparator<MyNode> {
    @Override
    public int compare(MyNode o1, MyNode o2) {
        return (int)((o2.getStandardizedValue() * 1000) - (o1.getStandardizedValue() * 1000));
    }
}