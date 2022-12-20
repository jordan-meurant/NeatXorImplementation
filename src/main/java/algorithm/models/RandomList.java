package algorithm.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

@Getter
@Setter
public class RandomList<T> {


    HashSet<T> set;
    ArrayList<T> list;

    public RandomList() {
        list = new ArrayList<>();
        set = new HashSet<>();
    }

    public T getRandom() {
        if(set.size() > 0){
            return list.get((int)(Math.random() * size()));
        }
        return null;
    }

    public int size() {
        return list.size();
    }

    public void add(T object){
        if(!set.contains(object)){
            set.add(object);
            list.add(object);
        }
    }

    public void addSorted(Comparator<T> comparator, Gene object) {
        for(int i = 0; i < this.size(); i++){
            int innovation = ((NodeGene)list.get(i)).getInnovationNumber();
            if(object.getInnovationNumber() < innovation){
                list.add(i, (T)object);
                set.add((T)object);
                return;
            }
        }
        list.add((T)object);
        set.add((T)object);
    }

    public void clear() {
        set.clear();
        list.clear();
    }

    public void sort(Comparator<T> comparator) {
        list.sort(comparator);
    }

    public T get(int index) {
        return list.get(index);
    }


    public void remove(int index) {
        list.remove(index);
    }

    public void remove(T object) {
        list.remove(object);
    }

    public boolean contains(T object){
        return set.contains(object);
    }

    public T getBest(Comparator<T> comparator) {
        if (set.size() > 0) {
            list.sort(comparator);
            return list.get(0);
        }
        return null;
    }

    @Override
    public String toString() {
        return "RandomList{" +
                "list=" + list +
                '}';
    }
}
