package app.gaborbiro.freelancecalculator.util

import org.amshove.kluent.`should be equal to`
import org.junit.Test

class ArithmeticChainTest {

    @Test
    fun `GIVEN arithmetic chain with one number WHEN multiplying it with another number THEN should result in the two numbers multiplied`() {
        // Given
        val chain = ArithmeticChain(3.0)

        // When
        val newChain = chain * 5.0

        // Then
        newChain.resolve() `should be equal to` 15.0
    }

    @Test
    fun `GIVEN arithmetic chain with one number WHEN dividing it with another number THEN should result in the two numbers divided`() {
        // Given
        val chain = ArithmeticChain(3.0)

        // When
        val newChain = chain / 5.0

        // Then
        newChain.resolve() `should be equal to` .6
    }

    @Test
    fun `GIVEN arithmetic chain with one number WHEN dividing it with the same number THEN should result in 1`() {
        // Given
        val chain = ArithmeticChain(3.0)

        // When
        val newChain = chain / 3.0

        // Then
        newChain.resolve() `should be equal to` 1.0
    }

    @Test
    fun `GIVEN an arithmetic chain WHEN dividing it with a number already present THEN the two (and only the two) should cancel out`() {
        // Given
        val chain = ArithmeticChain(5.0) * 5.0 / 2.0 / 5.0

        // When
        val newChain = chain / 5.0

        // Then
        newChain.resolve() `should be equal to` .5
    }

    @Test
    fun `GIVEN an arithmetic chain WHEN multiplying it with a number already present THEN the two should not cancel out`() {
        // Given
        val chain = ArithmeticChain(5.0) * 5.0 / 2.0 / 5.0

        // When
        val newChain = chain * 5.0

        // Then
        newChain.resolve() `should be equal to` 12.5
    }
}