package com.golfzon.data.common

import com.google.firebase.firestore.FirebaseFirestore

object FireStoreHelper {

    fun getUserCollection(firestore: FirebaseFirestore) = firestore.collection("users")
    fun getUserDocument(firestore: FirebaseFirestore, userId: String) =
        getUserCollection(firestore).document(userId)

    fun getUserExtraInfoCollection(firestore: FirebaseFirestore, userId: String) =
        getUserDocument(firestore, userId).collection("extraInfo")
    fun getUserTeamInfoDocument(firestore: FirebaseFirestore, userId: String) =
        getUserExtraInfoCollection(firestore, userId).document("teamInfo")
    fun getUserRecruitInfoDocument(firestore: FirebaseFirestore, userId: String) =
        getUserExtraInfoCollection(firestore, userId).document("recruitsInfo")
    fun getUserGroupInfoDocument(firestore: FirebaseFirestore, userId: String) =
        getUserExtraInfoCollection(firestore, userId).document("groupsInfo")

    fun getGroupCollection(firestore: FirebaseFirestore) =
        firestore.collection("groups")
    fun getGroupDocument(firestore: FirebaseFirestore, groupUId: String) =
        getGroupCollection(firestore).document(groupUId)

    fun getTeamCollection(firestore: FirebaseFirestore) = firestore.collection("teams")
    fun getTeamLikeDocument(firestore: FirebaseFirestore, teamUId: String) =
        firestore.collection("likes").document(teamUId)
    fun getTeamDisLikeDocument(firestore: FirebaseFirestore, teamUId: String) =
        firestore.collection("likes").document(teamUId)

    fun getRecruitCollection(firestore: FirebaseFirestore) = firestore.collection("recruits")
    fun getRecruitDocument(firestore: FirebaseFirestore, recruitUId: String) =
        getRecruitCollection(firestore).document(recruitUId)
}