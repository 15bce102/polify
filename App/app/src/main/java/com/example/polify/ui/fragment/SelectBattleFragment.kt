package com.example.polify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.polify.R
import com.example.polify.data.EXTRA_BATTLE_SELECT
import com.example.polify.databinding.FragmentSelectBattleBinding
import com.example.polify.eventbus.BattleSelectEvent
import com.example.polify.model.BattleSelect
import org.greenrobot.eventbus.EventBus

class SelectBattleFragment : Fragment() {
    companion object {
        fun getInstance(battleSelect: BattleSelect): SelectBattleFragment {
            return SelectBattleFragment().apply {
                arguments = bundleOf(EXTRA_BATTLE_SELECT to battleSelect)
            }
        }
    }

    private lateinit var binding: FragmentSelectBattleBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_battle, container, false)

        binding.apply {
            val b = requireArguments()[EXTRA_BATTLE_SELECT] as BattleSelect
            battle = b

            button.setOnClickListener {
                EventBus.getDefault().post(BattleSelectEvent(b))
            }
            executePendingBindings()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}