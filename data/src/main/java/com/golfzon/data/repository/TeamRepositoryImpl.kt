package com.golfzon.data.repository

import com.golfzon.domain.model.Team
import com.golfzon.domain.repository.TeamRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : TeamRepository {
    override suspend fun getTeamInfo(): Team {
        TODO("Not yet implemented")
    }
}