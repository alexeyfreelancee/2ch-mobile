package com.example.a2ch.ui.boards

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.a2ch.databinding.BoardsFragmentBinding
import com.example.a2ch.ui.category_list.CategoriesActivity
import com.example.a2ch.util.BOARD_NAME

class BoardsFragment : Fragment(){
    private lateinit var viewModel: BoardsViewModel
    private lateinit var binding: BoardsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(BoardsViewModel::class.java)
        binding = BoardsFragmentBinding.inflate(
            inflater, container, false
        )
        initObservers()
        return binding.apply {
            viewmodel = viewModel
        }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun initObservers() {
        viewModel.startEvent.observe(viewLifecycleOwner, Observer {
            val name = it.peekContent()
            startActivity(
                Intent(requireContext(), CategoriesActivity::class.java)
                    .putExtra(BOARD_NAME, name)
            )
        })
    }

}
