package com.dvach_2ch.a2ch.ui.help_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dvach_2ch.a2ch.databinding.HelpProjectFragmentBinding
import com.dvach_2ch.a2ch.util.NO_INTERNET
import com.dvach_2ch.a2ch.util.isNetworkAvailable
import com.dvach_2ch.a2ch.util.toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class HelpProjectFragment : Fragment() {
    private lateinit var binding: HelpProjectFragmentBinding
    private lateinit var viewModel: HelpProjectViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(HelpProjectViewModel::class.java)
        binding = HelpProjectFragmentBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        MobileAds.initialize(requireContext())
        initObservers()
        return binding.root
    }


    private fun initObservers() {
        viewModel.textCopied.observe(viewLifecycleOwner, Observer {
            requireContext().toast(it.peekContent())
        })
        viewModel.error.observe(viewLifecycleOwner, Observer {
            requireContext().toast(it.peekContent())
        })
        viewModel.openAd.observe(viewLifecycleOwner, Observer {
            if (isNetworkAvailable()) requireContext().toast("Загрузка...")
            else requireContext().toast(NO_INTERNET)
            openAd()
        })
    }

    private fun openAd() {
        val ad = RewardedAd(requireContext(), "ca-app-pub-2370326128002851/9314865313")
        ad.loadAd(AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                super.onRewardedAdLoaded()
                ad.show(requireActivity(), object : RewardedAdCallback() {
                    override fun onUserEarnedReward(p0: RewardItem) {
                        requireContext().toast("Спасибо за поддрежку :З")
                    }
                })
            }

            override fun onRewardedAdFailedToLoad(p0: Int) {
                requireContext().toast("Пока что нет рекламы")
                super.onRewardedAdFailedToLoad(p0)
            }
        })
    }

}
