package com.owen.primrosemotorcycles.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StockItemTest {

    @Test
    fun isLowStock_returnsTrueWhenQuantityEqualsOrBelowMin() {
        val lowItem = StockItem(name = "Spark Plug", quantityOnHand = 2, minQuantity = 2)
        val veryLowItem = StockItem(name = "Spark Plug", quantityOnHand = 1, minQuantity = 2)

        assertTrue(lowItem.isLowStock)
        assertTrue(veryLowItem.isLowStock)
    }

    @Test
    fun isLowStock_returnsFalseWhenQuantityAboveMin() {
        val normalItem = StockItem(name = "Spark Plug", quantityOnHand = 5, minQuantity = 2)

        assertFalse(normalItem.isLowStock)
    }

    @Test
    fun isOutOfStock_returnsTrueWhenZeroOrNegative() {
        val zeroItem = StockItem(name = "Oil Filter", quantityOnHand = 0)
        val negativeItem = StockItem(name = "Oil Filter", quantityOnHand = -1)

        assertTrue(zeroItem.isOutOfStock)
        assertTrue(negativeItem.isOutOfStock)
    }

    @Test
    fun isOutOfStock_returnsFalseWhenPositive() {
        val availableItem = StockItem(name = "Oil Filter", quantityOnHand = 1)

        assertFalse(availableItem.isOutOfStock)
    }
}
