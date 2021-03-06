package com.alphabethub.api.stack;


import java.util.ArrayList;
import java.util.List;

public class Stack<E> {
    private List<E> list = new ArrayList<>();

    public void clear(){
        list.clear();
    }

    public int size(){
        return list.size();
    }

    public boolean isEmpty(){
        return list.isEmpty();
    }

    public void push(E e){
        list.add(e);
    }

    public E pop(){
        return list.remove(size()-1);
    }

    public E top(){
        return list.get(size()-1);
    }


    @Override
    public String toString() {
        return "Stack{" +
                "list=" + list +
                '}';
    }
}
