package org.uncommons.zeitgeist;

/**
 * @author Daniel Dyer
*/
public class WeightedItem<T> implements Comparable<WeightedItem<T>>
{
    private final double weight;
    private final T item;


    WeightedItem(double weight, T item)
    {
        this.weight = weight;
        this.item = item;
    }


    public double getWeight()
    {
        return weight;
    }


    public T getItem()
    {
        return item;
    }


    @SuppressWarnings("unchecked")
    public int compareTo(WeightedItem<T> other)
    {
        int compare = Double.compare(weight, other.weight);
        if (compare == 0 && item instanceof Comparable)
        {
            compare = ((Comparable<T>) item).compareTo(other.item);
        }
        return compare;
    }


    @Override
    public String toString()
    {
        return weight + ": " + item;
    }
}
