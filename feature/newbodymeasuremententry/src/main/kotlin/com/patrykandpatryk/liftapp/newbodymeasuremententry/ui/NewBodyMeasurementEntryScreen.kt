package com.patrykandpatryk.liftapp.newbodymeasuremententry.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatryk.liftapp.core.R
import com.patrykandpatryk.liftapp.core.extension.stringResourceId
import com.patrykandpatryk.liftapp.core.preview.MultiDevicePreview
import com.patrykandpatryk.liftapp.core.state.onClick
import com.patrykandpatryk.liftapp.core.text.StringProviderImpl
import com.patrykandpatryk.liftapp.core.text.TextFieldState
import com.patrykandpatryk.liftapp.core.text.TextFieldStateManager
import com.patrykandpatryk.liftapp.core.ui.DialogTopBar
import com.patrykandpatryk.liftapp.core.ui.dimens.LocalDimens
import com.patrykandpatryk.liftapp.core.ui.input.DatePicker
import com.patrykandpatryk.liftapp.core.ui.input.NumberInput
import com.patrykandpatryk.liftapp.core.ui.input.TimePicker
import com.patrykandpatryk.liftapp.core.ui.input.rememberDatePickerState
import com.patrykandpatryk.liftapp.core.ui.input.rememberTimePickerState
import com.patrykandpatryk.liftapp.core.ui.theme.BottomSheetShape
import com.patrykandpatryk.liftapp.core.ui.theme.LiftAppTheme
import com.patrykandpatryk.liftapp.domain.Constants.Input.INCREMENT_LONG
import com.patrykandpatryk.liftapp.domain.Constants.Input.INCREMENT_SHORT
import com.patrykandpatryk.liftapp.domain.bodymeasurement.BodyMeasurementEntry
import com.patrykandpatryk.liftapp.domain.bodymeasurement.BodyMeasurementType
import com.patrykandpatryk.liftapp.domain.bodymeasurement.BodyMeasurementValue
import com.patrykandpatryk.liftapp.domain.bodymeasurement.BodyMeasurementWithLatestEntry
import com.patrykandpatryk.liftapp.domain.format.Formatter
import com.patrykandpatryk.liftapp.domain.unit.MassUnit
import com.patrykandpatryk.liftapp.domain.unit.ValueUnit
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

@Composable
fun NewBodyMeasurementEntryBottomSheet(
    bodyMeasurementId: Long,
    bodyMeasurementEntryId: Long?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<NewBodyMeasurementEntryViewModel, NewBodyMeasurementEntryViewModel.Factory>(
        creationCallback = { factory -> factory.create(bodyMeasurementId, bodyMeasurementEntryId) },
    )
    val entrySaved = viewModel.state.entrySaved.value

    BackHandler(onBack = onDismissRequest)

    LaunchedEffect(entrySaved) {
        if (entrySaved) onDismissRequest()
    }

    NewBodyMeasurementEntryBottomSheetContent(
        state = viewModel.state,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    )
}

@Composable
private fun NewBodyMeasurementEntryBottomSheetContent(
    state: NewBodyMeasurementState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalDimens.current
    val formattedDate = state.formattedDate.collectAsStateWithLifecycle().value
    val data = state.inputData.value

    val timePickerState = rememberTimePickerState(
        is24h = state.is24H.value,
        hour = formattedDate.hour,
        minute = formattedDate.minute,
    )

    val datePickerState = rememberDatePickerState(
        time = formattedDate.localDateTime,
    )

    TimePicker(state = timePickerState, onTimePicked = state::setTime)

    DatePicker(state = datePickerState, onTimePicked = state::setDate)

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(vertical = dimens.padding.contentVertical),
    ) {
        DialogTopBar(
            title = state.name.value,
            onCloseClick = onDismissRequest,
        )

        Column(
            modifier = modifier
                .navigationBarsPadding()
                .padding(horizontal = dimens.padding.contentHorizontal)
                .padding(top = dimens.padding.itemVertical),
            verticalArrangement = Arrangement.spacedBy(dimens.padding.itemVertical),
        ) {
            if (data != null) {
                data.forEachTextField { textFieldState, isLast ->
                    NumberInput(
                        textFieldState = textFieldState,
                        unit = data.unit,
                        isLast = isLast,
                        onSave = { state.save(data) },
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = dimens.padding.itemHorizontal),
                ) {
                    val dateInteractionSource = remember { MutableInteractionSource() }
                        .onClick(datePickerState::show)

                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        value = formattedDate.dateShort,
                        onValueChange = {},
                        label = { Text(text = stringResource(id = R.string.picker_date)) },
                        interactionSource = dateInteractionSource,
                    )

                    val timeInteractionSource = remember { MutableInteractionSource() }
                        .onClick { timePickerState.isShowing = true }

                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        value = formattedDate.timeShort,
                        onValueChange = {},
                        label = { Text(text = stringResource(id = R.string.picker_time)) },
                        interactionSource = timeInteractionSource,
                    )
                }

                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { state.save(data) },
                ) {
                    Text(
                        text = stringResource(id = R.string.action_save),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberInput(
    textFieldState: TextFieldState<Float>,
    unit: ValueUnit,
    isLast: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NumberInput(
        modifier = modifier,
        value = textFieldState.text,
        onValueChange = textFieldState::updateText,
        hint = stringResource(id = R.string.value),
        suffix = stringResource(id = unit.stringResourceId),
        onMinusClick = { long ->
            val updatedValue = textFieldState.value - getIncrement(long)
            if (updatedValue >= 0) {
                textFieldState.updateValue(updatedValue)
            }
        },
        onPlusClick = { long ->
            textFieldState.updateValue(textFieldState.value + getIncrement(long))
        },
        keyboardActions = KeyboardActions(
            onDone = { onSave() },
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
        ),
        isError = textFieldState.hasError,
        errorText = textFieldState.errorMessage?.let(::AnnotatedString),
    )
}

private fun getIncrement(long: Boolean) = if (long) INCREMENT_LONG else INCREMENT_SHORT

@MultiDevicePreview
@Composable
fun NewBodyMeasurementEntryBottomSheetContentPreview() {
    LiftAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                shape = BottomSheetShape,
                shadowElevation = 8.dp,
            ) {
                val context = LocalContext.current
                val stringProvider = StringProviderImpl(context)
                val formatter = Formatter(stringProvider, flowOf(true))
                val savedStateHandle = SavedStateHandle()
                val coroutineScope = rememberCoroutineScope()

                NewBodyMeasurementEntryBottomSheetContent(
                    state = remember {
                        NewBodyMeasurementState(
                            getFormattedDate = { formatter.getFormattedDate(it) },
                            getBodyMeasurementWithLatestEntry = {
                                BodyMeasurementWithLatestEntry(
                                    0,
                                    "Weight",
                                    BodyMeasurementType.Weight,
                                    BodyMeasurementEntry(
                                        id = 0,
                                        value = BodyMeasurementValue.Single(85f, MassUnit.Kilograms),
                                        formattedDate = formatter.getFormattedDate(LocalDateTime.now()),
                                    )
                                )
                            },
                            getBodyMeasurementEntry = { null },
                            upsertBodyMeasurementEntry = { _, _ -> },
                            textFieldStateManager = TextFieldStateManager(stringProvider, formatter, savedStateHandle),
                            getUnitForBodyMeasurementType = { MassUnit.Kilograms },
                            coroutineScope = coroutineScope,
                            savedStateHandle = savedStateHandle,
                        )
                    },
                    onDismissRequest = {},
                )
            }
        }
    }
}
