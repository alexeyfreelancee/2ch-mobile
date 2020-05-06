package com.alexey_vena.a2ch.ui.boards

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alexey_vena.a2ch.adapters.BoardListAdapter
import com.alexey_vena.a2ch.databinding.BoardsFragmentBinding
import com.alexey_vena.a2ch.ui.threads.ThreadsActivity
import com.alexey_vena.a2ch.util.BOARD_NAME
import com.alexey_vena.a2ch.util.toast
import kotlinx.android.synthetic.main.boards_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class BoardsFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory by instance<BoardsViewModelFactory>()

    private var viewModel: BoardsViewModel? = null
    private lateinit var binding: BoardsFragmentBinding
     private var boardListAdapter: BoardListAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(BoardsViewModel::class.java)
        binding = BoardsFragmentBinding.inflate(
            inflater, container, false
        )

        initObservers()

        return binding.apply {
            viewmodel = viewModel
        }.root
    }


    private fun initObservers() {
        viewModel?.startCategory?.observe(viewLifecycleOwner, Observer {
            val name = it.peekContent()
            startActivity(
                Intent(requireContext(), ThreadsActivity::class.java).putExtra(
                    BOARD_NAME, name
                )
            )
        })
        viewModel?.error?.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                requireContext().toast(it.peekContent().msg)
            }

        })
        viewModel?.boards?.observe(viewLifecycleOwner, Observer {
            boardListAdapter?.updateList(it)
        })
    }



    private fun initList() {
        boardListAdapter = BoardListAdapter(viewModel)
        board_list.adapter = boardListAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        initList()
    }


    fun filter(text: String){
        boardListAdapter?.filter?.filter(text)
    }

}
