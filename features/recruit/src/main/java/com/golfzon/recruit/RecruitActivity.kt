package com.golfzon.recruit

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.golfzon.recruit.databinding.ActivityRecruitBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecruitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecruitBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecruitBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    fun navigateToMatching() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("partee://multi.module.app/matching"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    fun navigateToGroup() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("partee://multi.module.app/group"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }
}