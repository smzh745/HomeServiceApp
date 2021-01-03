package home.service.appmanage.online.work.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import home.service.appmanage.online.work.R
import kotlinx.android.synthetic.main.fragment_vechicle_category.view.*


class VechicleCategoryFragment : BaseFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_vechicle_category, container, false)

        root!!.bikee.setOnClickListener {
            navigateFragmentbyType(
                R.id.bookDriverFragment,
                getString(R.string.bike)
            )
        }
        return root
    }


}