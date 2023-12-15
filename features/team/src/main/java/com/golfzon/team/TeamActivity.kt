package com.golfzon.team

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.team.databinding.ActivityTeamBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeamBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setNextClickListener()
    }

    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    fun navigateToMatching() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("partee://multi.module.app/matching}"))
        startActivity(intent)
        finishAffinity()
    }

    // ImageCrop이 보이는 경우에만 확인 버튼이 보이도록 처리
    private fun setNextClickListener() {
        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.ImageCropFragment -> {
                    with(binding.btnComplete) {
                        visibility = View.VISIBLE
                        setOnDebounceClickListener {
                            findNavController().navigateUp()
                        }
                    }
                }
                else -> {
                    binding.btnComplete.visibility = View.GONE
                }
            }
        }
    }
}