package com.golfzon.partee.di

import com.golfzon.core_ui.navigation.DeeplinkProcessor
import com.golfzon.login.ui.LoginDeeplinkProcessor
import com.golfzon.team.TeamDeeplinkProcessor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
interface DeepLinkProcessorModule {
    @Binds
    @IntoSet
    fun bindLoginDeeplinkProcessors(loginDeeplinkProcessor: LoginDeeplinkProcessor): DeeplinkProcessor

    @Binds
    @IntoSet
    fun bindTeamDeeplinkProcessors(teamDeeplinkProcessor: TeamDeeplinkProcessor): DeeplinkProcessor

}