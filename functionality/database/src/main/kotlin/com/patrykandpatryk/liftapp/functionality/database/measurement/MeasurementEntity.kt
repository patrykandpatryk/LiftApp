package com.patrykandpatryk.liftapp.functionality.database.measurement

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.patrykandpatryk.liftapp.domain.measurement.MeasurementType
import com.patrykandpatryk.liftapp.domain.model.Name

@Entity(
    tableName = "measurement",
)
class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: Name,
    val type: MeasurementType,
)
