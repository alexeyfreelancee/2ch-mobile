package com.example.a2ch.ui.thread_list

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider

import com.example.a2ch.R
import com.example.a2ch.databinding.ThreadsFragmentBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ThreadsFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory: ThreadsViewModelFactory by instance()
    private lateinit var viewModel: ThreadsViewModel
    private lateinit var binding: ThreadsFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(ThreadsViewModel::class.java)
        binding = ThreadsFragmentBinding.inflate(
            inflater, container,false
        )
        return binding.apply {
            viewmodel = viewModel
        }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

       binding.lifecycleOwner = viewLifecycleOwner
    }

}
