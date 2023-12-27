package com.golfzon.partee.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.golfzon.data.repository.ChatRepositoryImpl
import com.golfzon.data.repository.GroupRepositoryImpl
import com.golfzon.data.repository.MatchRepositoryImpl
import com.golfzon.data.repository.MemberRepositoryImpl
import com.golfzon.data.repository.RecruitRepositoryImpl
import com.golfzon.data.repository.TeamRepositoryImpl
import com.golfzon.domain.repository.ChatRepository
import com.golfzon.domain.repository.GroupRepository
import com.golfzon.domain.repository.MatchRepository
import com.golfzon.domain.repository.MemberRepository
import com.golfzon.domain.repository.RecruitRepository
import com.golfzon.domain.repository.TeamRepository
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
    fun provideMemberRepository(firestore: FirebaseFirestore, firebaseStorage: FirebaseStorage, dataStore: DataStore<Preferences>) : MemberRepository =
        MemberRepositoryImpl(firestore = firestore, firebaseStorage = firebaseStorage, dataStore = dataStore)

    @Singleton
    @Provides
    fun provideTeamRepository(firestore: FirebaseFirestore, firebaseStorage: FirebaseStorage, dataStore: DataStore<Preferences>) : TeamRepository =
        TeamRepositoryImpl(firestore = firestore, firebaseStorage = firebaseStorage, dataStore = dataStore)

    @Singleton
    @Provides
    fun provideMatchRepository(firestore: FirebaseFirestore, dataStore: DataStore<Preferences>) : MatchRepository =
        MatchRepositoryImpl(firestore = firestore, dataStore = dataStore)

    @Singleton
    @Provides
    fun provideGroupRepository(firestore: FirebaseFirestore, dataStore: DataStore<Preferences>) : GroupRepository =
        GroupRepositoryImpl(firestore = firestore, dataStore = dataStore)

    @Singleton
    @Provides
    fun provideRecruitRepository(firestore: FirebaseFirestore, dataStore: DataStore<Preferences>) : RecruitRepository =
        RecruitRepositoryImpl(firestore = firestore, dataStore = dataStore)

    @Singleton
    @Provides
    fun provideChatRepository(firestore: FirebaseFirestore, firebaseStorage: FirebaseStorage, dataStore: DataStore<Preferences>) : ChatRepository =
        ChatRepositoryImpl(firestore = firestore, firebaseStorage = firebaseStorage, dataStore = dataStore)
}