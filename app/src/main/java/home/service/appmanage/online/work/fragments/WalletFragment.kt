package home.service.appmanage.online.work.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.fragment_wallet.view.*


class WalletFragment : BaseFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_wallet, container, false)
        val fare = SharedPrefUtils.getStringData(requireContext(), "totalFare")
   /*     if (fare.isNullOrEmpty()) {
            root!!.totalBalance.text = "0"

        } else {*/
            root!!.totalBalance.text = fare
//        }
        return root
    }


}