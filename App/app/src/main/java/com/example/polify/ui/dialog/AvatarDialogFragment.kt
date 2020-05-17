package com.example.polify.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.game.model.response.Result
import com.andruid.magic.game.server.RetrofitClient.BASE_URL
import com.example.polify.databinding.DialogAvatarBinding
import com.example.polify.eventbus.AvatarEvent
import com.example.polify.ui.adapter.AvatarAdapter
import com.example.polify.ui.viewmodel.AvatarViewModel
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.util.RecyclerTouchListener
import org.greenrobot.eventbus.EventBus

class AvatarDialogFragment : DialogFragment() {
    companion object {
        fun getInstance() = AvatarDialogFragment()
    }

    private val avatarAdapter = AvatarAdapter()
    private val avatarViewModel by viewModels<AvatarViewModel> {
        BaseViewModelFactory { AvatarViewModel() }
    }

    private lateinit var binding: DialogAvatarBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogAvatarBinding.inflate(inflater, container, false)

        binding.avatarRV.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = avatarAdapter
            addOnItemTouchListener(RecyclerTouchListener(requireContext(), this,
                    object: RecyclerTouchListener.ClickListener {
                override fun onClick(view: View?, position: Int) {
                    EventBus.getDefault().post(AvatarEvent(avatarAdapter.currentList[position]))
                    dismiss()
                }

                override fun onLongClick(view: View?, position: Int) {}
            }))
        }

        avatarViewModel.avatars.observe(this, Observer { response ->
            if (response.status == Result.Status.SUCCESS)
                avatarAdapter.submitList(response.data?.avatars?.map { avatar -> BASE_URL.plus(avatar) })
        })

        return binding.root
    }
}