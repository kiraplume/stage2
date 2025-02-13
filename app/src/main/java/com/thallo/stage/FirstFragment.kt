package com.thallo.stage

import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.thallo.stage.componets.HomeLivedata
import com.thallo.stage.databinding.FragmentFirstBinding
import com.thallo.stage.fxa.Fxa
import com.thallo.stage.session.*
import com.thallo.stage.utils.getSizeName
import kotlinx.coroutines.launch
import mozilla.components.concept.sync.Profile
import mozilla.components.service.fxa.FxaAuthData
import mozilla.components.service.fxa.manager.FxaAccountManager
import mozilla.components.service.fxa.toAuthType
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var geckoViewModel: GeckoViewModel
    private lateinit var fxaAccountManager: FxaAccountManager
    private  var fxa=Fxa()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        geckoViewModel = activity?.let { ViewModelProvider(it)[GeckoViewModel::class.java] }!!
        fxaAccountManager= context?.let { fxa.init(it) }!!
        fxa.profileUpdated= object : Fxa.ProfileUpdated {
            override fun onProfileUpdated(profile: Profile) {
                profile.avatar?.url?.let { binding.signinButton?.let { it1 ->
                    Glide.with(context!!).load(it).circleCrop().into(
                        it1
                    )
                } }

            }

        }
        binding.signinButton?.setOnClickListener {
            lifecycleScope.launch {
                fxaAccountManager.beginAuthentication()?.let {
                    createSession(it,requireActivity())

                }
            }
        }

        binding.HomeSearchText?.setOnKeyListener(View.OnKeyListener { view, i, keyEvent ->
            if (KeyEvent.KEYCODE_ENTER == i && keyEvent.action == KeyEvent.ACTION_DOWN) {
                var value= binding.HomeSearchText!!.text.toString()
                if (Patterns.WEB_URL.matcher(value).matches() || URLUtil.isValidUrl(value)) {
                        createSession(value, requireActivity())

                } else {
                    createSession("https://www.baidu.com/s?wd=$value",requireActivity())
                }

            }
            false
        })

        DelegateLivedata.getInstance().observe(viewLifecycleOwner){
            it.login=object : SessionDelegate.Login{
                override fun onLogin(code: String, state: String, action: String) {
                    lifecycleScope.launch {
                        fxaAccountManager.finishAuthentication(
                            FxaAuthData(action.toAuthType(), code = code, state = state),
                        )

                    }

                }
            }
        }

        val calendar = Calendar.getInstance()

        if (calendar[Calendar.HOUR_OF_DAY] in 6..11) {
            binding.tips?.text = "Good\nMorning"
        }
        if (calendar[Calendar.HOUR_OF_DAY] in 12..13) {
            binding.tips?.text = "Good\n" + "Noon"
        }
        if (calendar[Calendar.HOUR_OF_DAY] in 14..19) {
            binding.tips?.text = "Good\n" + "Afternoon"
        }
        if (calendar[Calendar.HOUR_OF_DAY] in 20..22) {
            binding.tips?.text = "Good\n" + "Night"
        }
        if (22 < calendar[Calendar.HOUR_OF_DAY]) {
            binding.tips?.text = "Good\nDream"
        }
        if (calendar[Calendar.HOUR_OF_DAY] in 0..4) {
            binding.tips?.text = "Good\nDream"
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}