package com.droidx.trivianest.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import com.droidx.gameapi.server.RetrofitClient.BASE_URL
import com.droidx.trivianest.databinding.DialogAvatarBinding
import com.droidx.trivianest.eventbus.AvatarEvent
import com.droidx.trivianest.ui.adapter.AvatarAdapter
import com.droidx.trivianest.ui.viewmodel.AvatarViewModel
import com.droidx.trivianest.ui.viewmodel.BaseViewModelFactory
import com.droidx.trivianest.util.RecyclerTouchListener
import org.greenrobot.eventbus.EventBus
import com.droidx.gameapi.model.response.Result

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
                    object : RecyclerTouchListener.ClickListener {
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