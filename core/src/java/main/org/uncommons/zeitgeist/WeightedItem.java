// ============================================================================
//   Copyright 2009-2012 Daniel W. Dyer
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
        return Double.compare(weight, other.weight);
    }


    /**
     * Over-ridden to be consistent with {@link #compareTo(WeightedItem)}.
     * Two WeightedItems are considered equal if their weights are equal.  The item objects do not
     * have to be identical.
     * @param other The object to check for equality with.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || getClass() != other.getClass())
        {
            return false;
        }
        WeightedItem<?> that = (WeightedItem<?>) other;
        return Double.compare(that.weight, weight) == 0;
    }


    /**
     * Over-ridden to be consistent with {@link #equals(Object)}.
     * @return A hash code for this object (equal objects will have identical hash codes).
     */
    @Override
    public int hashCode()
    {
        long temp = weight != +0.0d ? Double.doubleToLongBits(weight) : 0L;
        return (int) (temp ^ (temp >>> 32));
    }


    @Override
    public String toString()
    {
        return weight + ": " + item;
    }
}
