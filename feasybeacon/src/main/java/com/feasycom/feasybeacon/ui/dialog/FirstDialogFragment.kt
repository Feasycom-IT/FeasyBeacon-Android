package com.feasycom.feasybeacon.ui.dialog

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.PrivacyDialogBinding
import com.feasycom.feasybeacon.ui.about.AgreementActivity

class FirstDialogFragment : DialogFragment(){

    private var _binding: PrivacyDialogBinding? = null
    private val binding get() = _binding!!

    var onAgree: (()-> Unit)? = null
    var onRefuse: (()-> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            requireActivity().finish()
            keyCode == KeyEvent.KEYCODE_BACK
        }

        _binding = PrivacyDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scrollView.apply {
            isVerticalScrollBarEnabled = true
            isScrollbarFadingEnabled = false
        }

        val userSpan = SpannableString(getString(R.string.userAgreement)).apply {
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(requireContext(), AgreementActivity::class.java)
                    intent.putExtra("type", 1)
                    requireContext().startActivity(intent)
                }
            }, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            setSpan(
                ForegroundColorSpan(Color.parseColor("#D1894A")),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val privacySpan = SpannableString(getString(R.string.privacyAgreement)).apply {
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(requireContext(), AgreementActivity::class.java)
                    intent.putExtra("type", 2)
                    requireContext().startActivity(intent)
                }
            }, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            setSpan(
                ForegroundColorSpan(Color.parseColor("#D1894A")),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        val stringBuilder = SpannableStringBuilder(getString(R.string.userAndPrivacyAgreementPrefix)).apply {
            append("《").append(userSpan).append("》")
            append(getString(R.string.and))
            append("《").append(privacySpan).append("》")
            append(getString(R.string.userAndPrivacyAgreementSuffix))
        }
        with(binding.contentTextView){
            movementMethod = LinkMovementMethod.getInstance()
            text = stringBuilder
            highlightColor = ContextCompat.getColor(requireContext(), R.color.transparent)
        }

//        confirm_button.isEnabled = false

//        isCheckedCB.setOnCheckedChangeListener { _, isChecked ->
//            if(isChecked){
//                agreeTextView.isEnabled = true
//                agreeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
//            }else{
//                agreeTextView.isEnabled = false
//                agreeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
//            }
//        }

        binding.confirmButton.setOnClickListener {
            onAgree?.invoke()
        }

        binding.disagreeButton.setOnClickListener {
            onRefuse?.invoke()
        }
    }

    override fun onResume() {
        val params: ViewGroup.LayoutParams = dialog?.window!!.attributes
        /**
         * remove the system dialog title and border background
         */
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
        super.onResume()
    }

    companion object{
        const val TAG = "FirstDialogFragment"
    }
}