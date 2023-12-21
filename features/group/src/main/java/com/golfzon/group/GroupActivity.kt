package com.golfzon.group

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.golfzon.group.databinding.ActivityGroupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        when (intent.getStringExtra("destination")) {
            "reservation" -> {
                findNavController().navigate(
                    GroupHomeFragmentDirections.actionGroupHomeFragmentToGroupCreateRoomScreenFragment(
                        intent.extras?.getString("groupUId") ?: ""
                    )
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        removeActivityChangeAnimation()
    }

    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }
    private fun removeActivityChangeAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,0, 0)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE,0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
    }

    fun navigateToMatching() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("partee://multi.module.app/matching"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finishAffinity()
    }

    fun navigateToRecruit() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("partee://multi.module.app/recruit"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finishAffinity()
    }
}