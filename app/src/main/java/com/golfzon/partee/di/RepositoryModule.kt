package com.golfzon.partee.di

import com.golfzon.data.repository.MemberRepositoryImpl
import com.golfzon.domain.repository.MemberRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    fun provideMemberRepository(firestore: FirebaseFirestore, firebaseStorage: FirebaseStorage) : MemberRepository =
        MemberRepositoryImpl(firestore = firestore, firebaseStorage = firebaseStorage)
}