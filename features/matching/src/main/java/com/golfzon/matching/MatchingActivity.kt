package com.golfzon.matching

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.golfzon.matching.databinding.ActivityMatchingBinding

class MatchingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatchingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }
}