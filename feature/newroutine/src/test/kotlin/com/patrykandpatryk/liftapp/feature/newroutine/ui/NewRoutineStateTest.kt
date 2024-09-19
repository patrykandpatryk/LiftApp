package com.patrykandpatryk.liftapp.feature.newroutine.ui

import androidx.lifecycle.SavedStateHandle
import com.patrykandpatryk.liftapp.core.text.TextFieldStateManager
import com.patrykandpatryk.liftapp.core.validation.NonEmptyCollectionValidator
import com.patrykandpatryk.liftapp.domain.Constants.Database.ID_NOT_SET
import com.patrykandpatryk.liftapp.domain.exercise.ExerciseType
import com.patrykandpatryk.liftapp.domain.format.Formatter
import com.patrykandpatryk.liftapp.domain.goal.Goal
import com.patrykandpatryk.liftapp.domain.routine.Routine
import com.patrykandpatryk.liftapp.domain.routine.RoutineExerciseItem
import com.patrykandpatryk.liftapp.testing.TestStringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewRoutineStateTest {

    private val testScheduler = TestCoroutineScheduler()

    private val coroutineContext = UnconfinedTestDispatcher(testScheduler)

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val formatter = Formatter(TestStringProvider, flowOf(true))

    private val savedStateHandle = SavedStateHandle()

    private fun getSut(
        routineID: Long,
        getRoutine: () -> Pair<Routine, List<Long>>?,
        getExerciseItems: (List<Long>) -> Flow<List<RoutineExerciseItem>> = { flowOf(emptyList()) },
    ) = NewRoutineState(
        routineID = routineID,
        savedStateHandle = savedStateHandle,
        getRoutine = getRoutine,
        upsertRoutine = { _, _, _ -> },
        getExerciseItems = getExerciseItems,
        textFieldStateManager = TextFieldStateManager(TestStringProvider, formatter, savedStateHandle),
        validateExercises = NonEmptyCollectionValidator(TestStringProvider),
        coroutineScope = coroutineScope,
    )

    @Test
    fun `Given routineID is not set, routine is not found`() = runTest {
        getSut(routineID = ID_NOT_SET, getRoutine = { error("Must not be executed.") })
    }

    @Test
    fun `Given routineID is set, routine data is loaded`() = runTest {
        val routineExerciseIds = exerciseItems.keys.toList()
        val sut = getSut(
            routineID = routine.id,
            getRoutine = { routine to routineExerciseIds },
            getExerciseItems = { ids -> flowOf(ids.mapNotNull { exerciseItems[it] }) },
        )

        assertEquals(routine.name, sut.name.value)
        assertEquals(routineExerciseIds, sut.exercises.value.value.map { it.id })
        assertTrue(sut.exercises.value.isValid)
    }

    @Test
    fun `Given exercise is picked, the exercise is added to the list of exercise items`() {
        val sut = getSut(
            routineID = routine.id,
            getRoutine = { routine to emptyList() },
            getExerciseItems = { ids -> flowOf(ids.mapNotNull { exerciseItems[it] }) },
        )

        assertTrue(sut.exercises.value.value.isEmpty())
        sut.addPickedExercises(exerciseItems.keys.toList())
        assertEquals(exerciseItems.keys.toList(), sut.exercises.value.value.map { it.id })
    }

    @Test
    fun `Given exercise is removed, the exercise is removed from the list of exercise items`() {
        val sut = getSut(
            routineID = routine.id,
            getRoutine = { routine to exerciseItems.keys.toList() },
            getExerciseItems = { ids -> flowOf(ids.mapNotNull { exerciseItems[it] }) },
        )

        assertEquals(exerciseItems.keys.toList(), sut.exercises.value.value.map { it.id })
        sut.removePickedExercise(exerciseItems.keys.first())
        assertEquals(exerciseItems.keys.drop(1), sut.exercises.value.value.map { it.id })
    }

    @Test
    fun `Given routine without a name is about to be saved, validation error is shown`() {
        val sut = getSut(
            routineID = ID_NOT_SET,
            getRoutine = { null },
        )

        sut.save()
        assertTrue(sut.name.hasError)
        assertEquals(TestStringProvider.fieldCannotBeEmpty(), sut.name.errorMessage)
    }

    @Test
    fun `Given routine without exercises is about to be saved, validation error is shown`() {
        val sut = getSut(
            routineID = ID_NOT_SET,
            getRoutine = { null },
        )

        sut.save()
        assertTrue(sut.exercises.value.isInvalid)
        assertEquals(TestStringProvider.getErrorCannotBeEmpty(TestStringProvider.list), sut.exercises.value.errorMessage)
    }

    @Test
    fun `Given routine with a name and exercises is about to be saved, routine is saved`() {
        val sut = getSut(
            routineID = ID_NOT_SET,
            getRoutine = { null },
            getExerciseItems = { flowOf(exerciseItems.values.toList()) },
        )

        sut.name.updateText("Routine")
        sut.addPickedExercises(exerciseItems.keys.toList())
        sut.save()
        assertTrue(sut.routineSaved.value)
    }

    companion object {
        val routine = Routine(name = "Routine", id = 1L)

        private val exerciseItems = listOf(
            RoutineExerciseItem(1L, "name1", "description1", ExerciseType.Weight, Goal(), ""),
            RoutineExerciseItem(2L, "name2", "description2", ExerciseType.Reps, Goal(), ""),
        ).associateBy { it.id }
    }
}