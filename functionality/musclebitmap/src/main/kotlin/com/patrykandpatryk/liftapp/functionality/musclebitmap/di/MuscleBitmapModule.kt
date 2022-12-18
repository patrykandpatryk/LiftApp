package com.patrykandpatryk.liftapp.functionality.musclebitmap.di

import android.content.Context
import com.patrykandpatryk.liftapp.domain.muscle.MuscleImageProvider
import com.patrykandpatryk.liftapp.functionality.musclebitmap.MuscleBitmapConfig
import com.patrykandpatryk.liftapp.functionality.musclebitmap.MuscleBitmapGenerator
import com.patrykandpatryk.liftapp.functionality.musclebitmap.MuscleBitmapGeneratorImpl
import com.patrykandpatryk.liftapp.functionality.musclebitmap.provider.MuscleImageProviderImpl
import com.patrykandpatryk.liftapp.functionality.musclebitmap.R
import com.patrykandpatryk.liftapp.functionality.musclebitmap.provider.ResourceBitmapProvider
import com.patrykandpatryk.liftapp.functionality.musclebitmap.provider.ResourceBitmapProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface MuscleBitmapModule {

    @Binds
    fun bindResourceBitmapProvider(provider: ResourceBitmapProviderImpl): ResourceBitmapProvider

    @Binds
    fun bindMuscleBitmapGenerator(generator: MuscleBitmapGeneratorImpl): MuscleBitmapGenerator

    @Binds
    fun bindMuscleImageProvider(provider: MuscleImageProviderImpl): MuscleImageProvider

    companion object {

        @Provides
        fun provideBitmapConfig(context: Context): MuscleBitmapConfig =
            MuscleBitmapConfig(
                borderColor = context.getColor(R.color.border),
                primaryColor = context.getColor(R.color.primary),
                secondaryColor = context.getColor(R.color.secondary),
                tertiaryColor = context.getColor(R.color.tertiary),
                bitmapMargin = context.resources.getDimensionPixelSize(R.dimen.bitmap_margin),
            )
    }
}
