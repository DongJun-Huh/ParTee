package com.golfzon.partee.di

import com.golfzon.data.repository.MemberRepositoryImpl
import com.golfzon.domain.repository.MemberRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun provideMemberRepository(firestore: FirebaseFirestore) : MemberRepository =
        MemberRepositoryImpl(firestore = firestore)
}