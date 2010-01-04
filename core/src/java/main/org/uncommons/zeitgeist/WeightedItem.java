// ============================================================================
//   Copyright 2009-2010 Daniel W. Dyer
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ============================================================================
package org.uncommons.zeitgeist;

/**
 * Simple generic class for attach a weighting to an object of a particular type.
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
