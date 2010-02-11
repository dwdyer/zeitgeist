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

import org.testng.annotations.Test;

/**
 * Unit test for the {@link WeightedItem} class.
 * @author Daniel Dyer
 */
public class WeightedItemTest
{
    @Test
    public void testEquality()
    {
        WeightedItem<String> item1 = new WeightedItem<String>(1, "A");
        WeightedItem<String> item2 = new WeightedItem<String>(1, "B");
        WeightedItem<String> item3 = new WeightedItem<String>(2, "B");

        assert item1.equals(item1) : "Equality should be reflexive.";

        assert item1.equals(item2) : "Items with equivalent weights should be considered equal.";
        assert item2.equals(item1) : "Equality should be symmetric.";
        assert item1.hashCode() == item2.hashCode() : "Equal objects must have identical hash codes.";

        assert !item2.equals(item3) : "Items with different weights should be considered unequal.";

        assert !item1.equals("A") : "Objects of different types should be considered unequal.";
        assert !item1.equals(null) : "A non-null reference should not be considered equal to null.";
    }


    @Test
    public void testComparisons()
    {
        WeightedItem<String> item1 = new WeightedItem<String>(1, "A");
        WeightedItem<String> item2 = new WeightedItem<String>(1, "B");
        WeightedItem<String> item3 = new WeightedItem<String>(2, "B");

        assert item1.compareTo(item1) == 0 : "Equality should be reflexive.";

        assert item1.compareTo(item2) == 0 : "Items with same weight should compare as equal.";
        assert item2.compareTo(item1) == 0 : "Equality should be symmetric.";

        assert item2.compareTo(item3) < 0 : "Lower weighted item should be considered lower.";
        assert item3.compareTo(item2) > 0 : "Higher weighted item should be considered higher.";
    }
}
